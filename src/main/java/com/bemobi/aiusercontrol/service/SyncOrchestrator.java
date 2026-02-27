package com.bemobi.aiusercontrol.service;

import com.bemobi.aiusercontrol.config.AppProperties;
import com.bemobi.aiusercontrol.dto.response.SyncResultResponse;
import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.integration.claude.ClaudeApiClient;
import com.bemobi.aiusercontrol.integration.cursor.CursorApiClient;
import com.bemobi.aiusercontrol.integration.github.GitHubCopilotClient;
import com.bemobi.aiusercontrol.integration.google.GoogleWorkspaceService;
import com.bemobi.aiusercontrol.model.entity.AITool;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.aitool.repository.AIToolRepository;
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
public class SyncOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SyncOrchestrator.class);

    private final UserRepository userRepository;
    private final AIToolRepository aiToolRepository;
    private final AccountLinkingService accountLinkingService;
    private final ClaudeApiClient claudeApiClient;
    private final CursorApiClient cursorApiClient;
    private final GitHubCopilotClient gitHubCopilotClient;
    private final AppProperties appProperties;

    @Autowired(required = false)
    private GoogleWorkspaceService googleWorkspaceService;

    public SyncOrchestrator(UserRepository userRepository,
                            AIToolRepository aiToolRepository,
                            AccountLinkingService accountLinkingService,
                            ClaudeApiClient claudeApiClient,
                            CursorApiClient cursorApiClient,
                            GitHubCopilotClient gitHubCopilotClient,
                            AppProperties appProperties) {
        this.userRepository = userRepository;
        this.aiToolRepository = aiToolRepository;
        this.accountLinkingService = accountLinkingService;
        this.claudeApiClient = claudeApiClient;
        this.cursorApiClient = cursorApiClient;
        this.gitHubCopilotClient = gitHubCopilotClient;
        this.appProperties = appProperties;
    }

    public SyncResultResponse executeFullSync() {
        log.info("Starting AI-first sync...");
        SyncResultResponse.Builder resultBuilder = SyncResultResponse.builder();

        // Step 1: Fetch all AI tool seats in parallel
        Map<AITool, List<ToolAccountInfo>> seatsByTool = fetchAllToolSeatsInParallel(resultBuilder);

        // Step 2: Consolidate unique emails across all tools
        Set<String> uniqueEmails = extractUniqueEmails(seatsByTool);

        // Step 3: GWS validation/enrichment per email (deduplicated, sequential, cached within run)
        Map<String, Optional<GoogleWorkspaceService.GwsUser>> gwsResults = validateEmailsAgainstGws(uniqueEmails);

        // Step 4: Create/update users for validated emails
        createOrUpdateValidatedUsers(gwsResults, uniqueEmails, resultBuilder);

        // Step 5: Link seats to users (reuse AccountLinkingService)
        linkSeatsToUsers(seatsByTool, resultBuilder);

        // Step 6: Archive legacy users with no AI seats
        archiveLegacyUsersWithoutSeats(resultBuilder);

        SyncResultResponse result = resultBuilder.build();
        log.info("AI-first sync completed: {}", result);
        return result;
    }

    private Map<AITool, List<ToolAccountInfo>> fetchAllToolSeatsInParallel(SyncResultResponse.Builder resultBuilder) {
        List<AITool> enabledTools = aiToolRepository.findByEnabled(true);
        Map<AITool, CompletableFuture<List<ToolAccountInfo>>> futures = new LinkedHashMap<>();

        for (AITool tool : enabledTools) {
            futures.put(tool, CompletableFuture.supplyAsync(() -> fetchToolUsers(tool)));
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null) // Don't throw if some futures failed
                .join();

        Map<AITool, List<ToolAccountInfo>> result = new LinkedHashMap<>();
        for (Map.Entry<AITool, CompletableFuture<List<ToolAccountInfo>>> entry : futures.entrySet()) {
            AITool tool = entry.getKey();
            CompletableFuture<List<ToolAccountInfo>> future = entry.getValue();

            try {
                List<ToolAccountInfo> seats = future.join();
                SyncResultResponse.ToolSyncDetail detail = new SyncResultResponse.ToolSyncDetail();
                detail.setToolName(tool.getName());
                detail.setSeatsFound(seats != null ? seats.size() : 0);
                resultBuilder.addToolDetail(tool.getName(), detail);

                if (seats != null && !seats.isEmpty()) {
                    result.put(tool, seats);
                }
            } catch (Exception e) {
                log.error("Failed to fetch seats for tool {}: {}", tool.getName(), e.getMessage());
                SyncResultResponse.ToolSyncDetail detail = new SyncResultResponse.ToolSyncDetail();
                detail.setToolName(tool.getName());
                detail.setSeatsFound(0);
                detail.setError(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                resultBuilder.addToolDetail(tool.getName(), detail);
            }
        }
        return result;
    }

    private Set<String> extractUniqueEmails(Map<AITool, List<ToolAccountInfo>> seatsByTool) {
        Set<String> emails = new HashSet<>();
        for (List<ToolAccountInfo> seats : seatsByTool.values()) {
            for (ToolAccountInfo seat : seats) {
                if (seat.getEmail() != null && !seat.getEmail().isBlank()) {
                    emails.add(seat.getEmail().toLowerCase().trim());
                }
            }
        }
        return emails;
    }

    private Map<String, Optional<GoogleWorkspaceService.GwsUser>> validateEmailsAgainstGws(Set<String> emails) {
        Map<String, Optional<GoogleWorkspaceService.GwsUser>> results = new HashMap<>();

        if (googleWorkspaceService == null) {
            log.warn("GWS not enabled, skipping email validation");
            return results;
        }

        for (String email : emails) {
            try {
                results.put(email, googleWorkspaceService.lookupUserByEmail(email));
            } catch (Exception e) {
                log.warn("GWS lookup failed for {}, skipping (may be transient): {}", email, e.getMessage());
                // Do NOT mark as invalid on GWS error -- could be transient. Skip silently.
            }
        }
        return results;
    }

    private void createOrUpdateValidatedUsers(Map<String, Optional<GoogleWorkspaceService.GwsUser>> gwsResults,
                                               Set<String> uniqueEmails,
                                               SyncResultResponse.Builder resultBuilder) {
        int newUsers = 0;
        int updatedUsers = 0;
        int externalAccounts = 0;
        int gwsValidatedUsers = 0;

        for (String email : uniqueEmails) {
            Optional<GoogleWorkspaceService.GwsUser> gwsOptional = gwsResults.get(email);

            if (gwsOptional != null && gwsOptional.isPresent()) {
                // Email validated in GWS
                GoogleWorkspaceService.GwsUser gwsUser = gwsOptional.get();
                gwsValidatedUsers++;

                Optional<User> existingUser = userRepository.findByEmail(email);

                if (existingUser.isPresent()) {
                    // Update existing user with GWS data
                    User user = existingUser.get();
                    user.setName(gwsUser.getName());
                    user.setDepartment(gwsUser.getDepartment());
                    user.setAvatarUrl(gwsUser.getAvatarUrl());
                    user.setGithubUsername(gwsUser.getGithubUsername());
                    user.setValidationSource("AI_SEAT_GWS_VALIDATED");
                    user.setGwsValidatedAt(Instant.now());
                    if (user.getStatus() != UserStatus.OFFBOARDED) {
                        user.setStatus(UserStatus.ACTIVE);
                    }
                    userRepository.save(user);
                    updatedUsers++;
                    log.debug("Updated existing user from AI seat + GWS validation: {}", email);
                } else {
                    // Create new user from GWS data
                    User newUser = User.builder()
                            .email(email)
                            .name(gwsUser.getName())
                            .department(gwsUser.getDepartment())
                            .avatarUrl(gwsUser.getAvatarUrl())
                            .githubUsername(gwsUser.getGithubUsername())
                            .validationSource("AI_SEAT_GWS_VALIDATED")
                            .gwsValidatedAt(Instant.now())
                            .status(UserStatus.ACTIVE)
                            .build();
                    userRepository.save(newUser);
                    newUsers++;
                    log.debug("Created new user from AI seat + GWS validation: {}", email);
                }
            } else {
                // Email not in GWS or GWS lookup failed/skipped -- external account
                externalAccounts++;
                log.debug("External account (no GWS match): {}", email);
            }
        }

        resultBuilder.newUsers(newUsers);
        resultBuilder.updatedUsers(updatedUsers);
        resultBuilder.externalAccounts(externalAccounts);
        resultBuilder.gwsValidatedUsers(gwsValidatedUsers);

        log.info("User creation/update: {} new, {} updated, {} external, {} GWS-validated",
                newUsers, updatedUsers, externalAccounts, gwsValidatedUsers);
    }

    private void linkSeatsToUsers(Map<AITool, List<ToolAccountInfo>> seatsByTool,
                                   SyncResultResponse.Builder resultBuilder) {
        int totalLinked = 0;
        int totalUnmatched = 0;
        int totalSuspended = 0;
        int totalRevoked = 0;

        for (Map.Entry<AITool, List<ToolAccountInfo>> entry : seatsByTool.entrySet()) {
            AITool tool = entry.getKey();
            List<ToolAccountInfo> seats = entry.getValue();

            try {
                // For GitHub Copilot, enrich seats with emails by matching github_username
                if (tool.getToolType() == AIToolType.GITHUB_COPILOT) {
                    seats = enrichGitHubSeatsWithEmails(seats);
                }

                AccountLinkingService.LinkResult linkResult = accountLinkingService.linkAccounts(tool, seats);

                totalLinked += linkResult.getLinked();
                totalUnmatched += linkResult.getUnmatched();
                totalSuspended += linkResult.getSuspended();
                totalRevoked += linkResult.getRevoked();

                // Enrich existing ToolSyncDetail with link results
                SyncResultResponse.ToolSyncDetail detail = resultBuilder.getToolDetail(tool.getName());
                if (detail != null) {
                    detail.setLinked(linkResult.getLinked());
                    detail.setUnmatched(linkResult.getUnmatched());
                    detail.setSuspended(linkResult.getSuspended());
                    detail.setRevoked(linkResult.getRevoked());
                }

            } catch (Exception e) {
                String error = "Failed to link accounts for tool " + tool.getName() + ": " + e.getMessage();
                log.error(error, e);
                resultBuilder.addError(error);

                // Update tool detail with error
                SyncResultResponse.ToolSyncDetail detail = resultBuilder.getToolDetail(tool.getName());
                if (detail != null) {
                    detail.setError(e.getMessage());
                }
            }
        }

        resultBuilder.linkedAccounts(totalLinked);
        resultBuilder.unmatchedAccounts(totalUnmatched);
        resultBuilder.suspendedAccounts(totalSuspended);
        resultBuilder.revokedAccounts(totalRevoked);

        log.info("Tool linking completed: {} linked, {} unmatched, {} suspended, {} revoked",
                totalLinked, totalUnmatched, totalSuspended, totalRevoked);
    }

    private List<ToolAccountInfo> enrichGitHubSeatsWithEmails(List<ToolAccountInfo> seats) {
        List<ToolAccountInfo> enriched = new ArrayList<>();
        for (ToolAccountInfo seat : seats) {
            // seat.identifier = GitHub login, seat.email = null
            String email = null;

            // Step 1: Primary lookup -- GWS git_name query (case-insensitive at GWS level)
            if (googleWorkspaceService != null) {
                try {
                    Optional<GoogleWorkspaceService.GwsUser> gwsUser =
                            googleWorkspaceService.lookupUserByGitName(seat.getIdentifier());
                    if (gwsUser.isPresent()) {
                        email = gwsUser.get().getEmail();
                        log.debug("GitHub Copilot seat {} matched to {} via GWS git_name", seat.getIdentifier(), email);
                    }
                } catch (Exception e) {
                    log.warn("GWS git_name lookup failed for {}, trying DB fallback: {}", seat.getIdentifier(), e.getMessage());
                }
            }

            // Step 2: Fallback -- email match (construct login@domain, lookup in DB by email)
            if (email == null) {
                String candidateEmail = seat.getIdentifier() + "@" + appProperties.getGoogleWorkspace().getDomain();
                Optional<User> user = userRepository.findByEmail(candidateEmail.toLowerCase());
                if (user.isPresent()) {
                    email = user.get().getEmail();
                    log.debug("GitHub Copilot seat {} matched to {} via email fallback", seat.getIdentifier(), email);
                }
            }

            if (email != null) {
                // Preserve the original date fields from the seat
                enriched.add(new ToolAccountInfo(seat.getIdentifier(), email, seat.getCreatedAtSource(), seat.getLastActivityAt()));
            } else {
                enriched.add(seat); // stays with email=null, will be unmatched
            }
        }
        return enriched;
    }

    private void archiveLegacyUsersWithoutSeats(SyncResultResponse.Builder resultBuilder) {
        List<User> legacyUsers = userRepository.findUsersWithoutAIToolAccounts();
        int archived = 0;
        for (User user : legacyUsers) {
            if (user.getStatus() == UserStatus.ACTIVE && "GWS_LEGACY".equals(user.getValidationSource())) {
                user.setStatus(UserStatus.INACTIVE);
                userRepository.save(user);
                archived++;
                log.debug("Archived legacy user without AI seats: {}", user.getEmail());
            }
        }
        resultBuilder.archivedLegacyUsers(archived);
        if (archived > 0) {
            log.info("Archived {} legacy users with no AI tool accounts", archived);
        }
    }

    private List<ToolAccountInfo> fetchToolUsers(AITool tool) {
        AIToolType toolType = tool.getToolType();

        if (tool.getApiKey() == null || tool.getApiKey().isBlank()) {
            log.warn("Tool {} ({}) has no API key configured, skipping", tool.getName(), toolType);
            return Collections.emptyList();
        }

        if (toolType == AIToolType.CLAUDE) {
            if (claudeApiClient == null) {
                log.warn("Claude API client is not available, skipping tool: {}", tool.getName());
                return Collections.emptyList();
            }
            return claudeApiClient.fetchUsers(tool.getApiKey(), tool.getApiBaseUrl(), tool.getApiOrgId());
        }

        if (toolType == AIToolType.CURSOR) {
            if (cursorApiClient == null) {
                log.warn("Cursor API client is not available, skipping tool: {}", tool.getName());
                return Collections.emptyList();
            }
            return cursorApiClient.fetchUsers(tool.getApiKey(), tool.getApiBaseUrl(), tool.getApiOrgId());
        }

        if (toolType == AIToolType.GITHUB_COPILOT) {
            if (gitHubCopilotClient == null) {
                log.warn("GitHub Copilot client is not available, skipping tool: {}", tool.getName());
                return Collections.emptyList();
            }
            return gitHubCopilotClient.fetchSeats(tool.getApiKey(), tool.getApiOrgId());
        }

        log.debug("Unsupported tool type for sync: {} ({})", tool.getName(), toolType);
        return Collections.emptyList();
    }

    // Setter for optional dependency (used in tests where GWS may be null)
    void setGoogleWorkspaceService(GoogleWorkspaceService googleWorkspaceService) {
        this.googleWorkspaceService = googleWorkspaceService;
    }
}
