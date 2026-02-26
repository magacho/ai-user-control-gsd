package com.bemobi.aiusercontrol.integration.cursor;

import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CursorApiClient {

    private static final Logger log = LoggerFactory.getLogger(CursorApiClient.class);
    private static final String DEFAULT_BASE_URL = "https://api.cursor.com";

    private final WebClient.Builder webClientBuilder;

    public CursorApiClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @SuppressWarnings("unchecked")
    public List<ToolAccountInfo> fetchUsers(String apiKey, String apiBaseUrl, String orgId) {
        // Cursor API key is team-scoped, orgId is not used in the request
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Cursor API key is not configured, skipping user fetch");
            return Collections.emptyList();
        }

        // Default to well-known Cursor API base URL if not provided
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            apiBaseUrl = DEFAULT_BASE_URL;
        }

        log.info("Fetching users from Cursor Admin API");

        try {
            // Basic Auth: base64(apiKey + ":")  -- format is username:password with empty password
            String basicAuth = Base64.getEncoder().encodeToString((apiKey + ":").getBytes(StandardCharsets.UTF_8));

            WebClient webClient = webClientBuilder
                    .baseUrl(apiBaseUrl)
                    .defaultHeader("Authorization", "Basic " + basicAuth)
                    .build();

            // TODO: Add pagination if team has >100 members (page/pageSize params)
            Map<String, Object> response = webClient.get()
                    .uri("/teams/members")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("Cursor Admin API returned error status {}",
                                clientResponse.statusCode());
                        return clientResponse.createException();
                    })
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null) {
                log.warn("Cursor Admin API returned null response");
                return Collections.emptyList();
            }

            List<Map<String, Object>> teamMembers = (List<Map<String, Object>>) response.get("teamMembers");
            if (teamMembers == null) {
                log.warn("Cursor Admin API response has no teamMembers field");
                return Collections.emptyList();
            }

            List<ToolAccountInfo> users = new ArrayList<>();
            for (Map<String, Object> member : teamMembers) {
                // Filter out removed members (kept in response for billing history)
                Boolean isRemoved = (Boolean) member.get("isRemoved");
                if (Boolean.TRUE.equals(isRemoved)) {
                    continue;
                }

                String email = (String) member.get("email");
                if (email != null && !email.isBlank()) {
                    // Use email as both identifier and email (Cursor has no stable user ID in members endpoint)
                    users.add(new ToolAccountInfo(email, email));
                }
            }

            log.info("Successfully fetched {} active users from Cursor Admin API", users.size());
            return users;

        } catch (Exception e) {
            log.error("Failed to fetch users from Cursor Admin API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
