package com.bemobi.aiusercontrol.integration.cursor;

import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "app.integrations.cursor", name = "enabled", havingValue = "true")
public class CursorApiClient {

    private static final Logger log = LoggerFactory.getLogger(CursorApiClient.class);

    private final WebClient.Builder webClientBuilder;

    public CursorApiClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @SuppressWarnings("unchecked")
    public List<ToolAccountInfo> fetchUsers(String apiKey, String apiBaseUrl, String orgId) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Cursor API key is not configured, skipping user fetch for org: {}", orgId);
            return Collections.emptyList();
        }

        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            log.warn("Cursor API base URL is not configured, skipping user fetch for org: {}", orgId);
            return Collections.emptyList();
        }

        log.info("Fetching users from Cursor API for organization: {}", orgId);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(apiBaseUrl)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();

            List<ToolAccountInfo> allUsers = new ArrayList<>();
            String nextPageUrl = "/api/v1/organizations/" + orgId + "/members";
            boolean hasMore = true;

            while (hasMore && nextPageUrl != null) {
                String currentUrl = nextPageUrl;
                Map<String, Object> response = webClient.get()
                        .uri(currentUrl)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, clientResponse -> {
                            log.error("Cursor API returned error status {} for org: {}",
                                    clientResponse.statusCode(), orgId);
                            return clientResponse.createException();
                        })
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();

                if (response == null) {
                    break;
                }

                List<Map<String, Object>> members = (List<Map<String, Object>>) response.get("data");
                if (members != null) {
                    for (Map<String, Object> member : members) {
                        String id = String.valueOf(member.get("id"));
                        String email = (String) member.get("email");
                        if (email != null && !email.isBlank()) {
                            allUsers.add(new ToolAccountInfo(id, email));
                        }
                    }
                }

                String nextCursor = (String) response.get("next_cursor");
                if (nextCursor != null && !nextCursor.isBlank()) {
                    nextPageUrl = "/api/v1/organizations/" + orgId + "/members?cursor=" + nextCursor;
                } else {
                    hasMore = false;
                }
            }

            log.info("Successfully fetched {} users from Cursor API for organization: {}", allUsers.size(), orgId);
            return allUsers;

        } catch (Exception e) {
            log.error("Failed to fetch users from Cursor API for organization {}: {}", orgId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
