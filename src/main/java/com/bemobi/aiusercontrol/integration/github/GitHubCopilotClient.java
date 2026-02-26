package com.bemobi.aiusercontrol.integration.github;

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
@ConditionalOnProperty(prefix = "app.integrations.github-copilot", name = "enabled", havingValue = "true")
public class GitHubCopilotClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubCopilotClient.class);
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final int PER_PAGE = 100;

    private final WebClient.Builder webClientBuilder;

    public GitHubCopilotClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @SuppressWarnings("unchecked")
    public List<ToolAccountInfo> fetchSeats(String apiToken, String orgName) {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("GitHub API token is not configured, skipping seat fetch for org: {}", orgName);
            return Collections.emptyList();
        }

        if (orgName == null || orgName.isBlank()) {
            log.warn("GitHub organization name is not configured, skipping seat fetch");
            return Collections.emptyList();
        }

        log.info("Fetching Copilot seats from GitHub API for organization: {}", orgName);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(GITHUB_API_BASE_URL)
                    .defaultHeader("Authorization", "Bearer " + apiToken)
                    .defaultHeader("Accept", "application/vnd.github+json")
                    .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                    .build();

            List<ToolAccountInfo> allSeats = new ArrayList<>();
            int page = 1;
            int totalSeats = Integer.MAX_VALUE;

            while (allSeats.size() < totalSeats) {
                String uri = "/orgs/" + orgName + "/copilot/billing/seats?page=" + page + "&per_page=" + PER_PAGE;

                Map<String, Object> response = webClient.get()
                        .uri(uri)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, clientResponse -> {
                            log.error("GitHub API returned error status {} for org: {}",
                                    clientResponse.statusCode(), orgName);
                            return clientResponse.createException();
                        })
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();

                if (response == null) {
                    break;
                }

                Object totalSeatsObj = response.get("total_seats");
                if (totalSeatsObj instanceof Number) {
                    totalSeats = ((Number) totalSeatsObj).intValue();
                }

                List<Map<String, Object>> seats = (List<Map<String, Object>>) response.get("seats");
                if (seats == null || seats.isEmpty()) {
                    break;
                }

                for (Map<String, Object> seat : seats) {
                    Map<String, Object> assignee = (Map<String, Object>) seat.get("assignee");
                    if (assignee != null) {
                        String login = (String) assignee.get("login");
                        if (login != null && !login.isBlank()) {
                            allSeats.add(new ToolAccountInfo(login, null));
                        }
                    }
                }

                log.debug("Fetched page {} — {} seats so far out of {} total", page, allSeats.size(), totalSeats);
                page++;
            }

            log.info("Successfully fetched {} Copilot seats from GitHub API for organization: {}", allSeats.size(), orgName);
            return allSeats;

        } catch (Exception e) {
            log.error("Failed to fetch Copilot seats from GitHub API for organization {}: {}", orgName, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
