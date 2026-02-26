package com.bemobi.aiusercontrol.integration.claude;

import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ClaudeApiClient {

    private static final Logger log = LoggerFactory.getLogger(ClaudeApiClient.class);
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final WebClient.Builder webClientBuilder;

    public ClaudeApiClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @SuppressWarnings("unchecked")
    public List<ToolAccountInfo> fetchUsers(String apiKey, String apiBaseUrl, String orgId) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Claude API key is not configured, skipping user fetch for org: {}", orgId);
            return Collections.emptyList();
        }

        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            apiBaseUrl = "https://api.anthropic.com";
        }

        log.info("Fetching users from Claude API for organization: {}", orgId);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(apiBaseUrl)
                    .defaultHeader("x-api-key", apiKey)
                    .defaultHeader("anthropic-version", ANTHROPIC_VERSION)
                    .build();

            List<ToolAccountInfo> allUsers = new ArrayList<>();
            String nextPageUrl = "/v1/organizations/" + orgId + "/members";
            boolean hasMore = true;

            while (hasMore && nextPageUrl != null) {
                String currentUrl = nextPageUrl;
                Map<String, Object> response = webClient.get()
                        .uri(currentUrl)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, clientResponse -> {
                            log.error("Claude API returned error status {} for org: {}",
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

                Boolean hasMoreFlag = (Boolean) response.get("has_more");
                if (Boolean.TRUE.equals(hasMoreFlag)) {
                    String lastId = (String) response.get("last_id");
                    if (lastId != null) {
                        nextPageUrl = "/v1/organizations/" + orgId + "/members?after_id=" + lastId;
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }

            log.info("Successfully fetched {} users from Claude API for organization: {}", allUsers.size(), orgId);
            return allUsers;

        } catch (Exception e) {
            log.error("Failed to fetch users from Claude API for organization {}: {}", orgId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
