package com.bemobi.aiusercontrol.service;

import com.bemobi.aiusercontrol.dto.response.SyncResultResponse;
import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.integration.claude.ClaudeApiClient;
import com.bemobi.aiusercontrol.integration.cursor.CursorApiClient;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class SyncOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SyncOrchestrator.class);

    private final UserRepository userRepository;
    private final AIToolRepository aiToolRepository;
    private final AccountLinkingService accountLinkingService;

    @Autowired(required = false)
    private GoogleWorkspaceService googleWorkspaceService;

    @Autowired(required = false)
    private ClaudeApiClient claudeApiClient;

    @Autowired(required = false)
    private CursorApiClient cursorApiClient;

    public SyncOrchestrator(UserRepository userRepository,
                            AIToolRepository aiToolRepository,
                            AccountLinkingService accountLinkingService) {
        this.userRepository = userRepository;
        this.aiToolRepository = aiToolRepository;
        this.accountLinkingService = accountLinkingService;
    }

    public SyncResultResponse executeFullSync() {
        log.info("Starting full sync...");

        SyncResultResponse.Builder resultBuilder = SyncResultResponse.builder();

        // Phase 1: Google Workspace Sync
        syncGoogleWorkspace(resultBuilder);

        // Phase 2: Tool Account Linking
        syncToolAccounts(resultBuilder);

        SyncResultResponse result = resultBuilder.build();
        log.info("Full sync completed: {}", result);
        return result;
    }

    private void syncGoogleWorkspace(SyncResultResponse.Builder resultBuilder) {
        if (googleWorkspaceService == null) {
            log.warn("Google Workspace integration is not enabled, skipping GWS sync");
            resultBuilder.addError("Google Workspace integration is not enabled");
            return;
        }

        log.info("Starting Google Workspace sync phase...");

        try {
            List<GoogleWorkspaceService.GwsUser> gwsUsers = googleWorkspaceService.fetchAllUsers();
            Set<String> gwsEmails = new HashSet<>();

            int newUsers = 0;
            int updatedUsers = 0;

            for (GoogleWorkspaceService.GwsUser gwsUser : gwsUsers) {
                gwsEmails.add(gwsUser.getEmail().toLowerCase());

                Optional<User> existingUser = userRepository.findByEmail(gwsUser.getEmail().toLowerCase());

                if (existingUser.isPresent()) {
                    // Update existing user - GWS is source of truth
                    User user = existingUser.get();
                    user.setName(gwsUser.getName());
                    user.setAvatarUrl(gwsUser.getAvatarUrl());
                    user.setGithubUsername(gwsUser.getGithubUsername());
                    if (user.getStatus() != UserStatus.OFFBOARDED) {
                        user.setStatus(UserStatus.ACTIVE);
                    }
                    userRepository.save(user);
                    updatedUsers++;
                    log.debug("Updated existing user: {}", gwsUser.getEmail());
                } else {
                    // Create new user
                    User newUser = User.builder()
                            .email(gwsUser.getEmail().toLowerCase())
                            .name(gwsUser.getName())
                            .avatarUrl(gwsUser.getAvatarUrl())
                            .githubUsername(gwsUser.getGithubUsername())
                            .status(UserStatus.ACTIVE)
                            .build();
                    userRepository.save(newUser);
                    newUsers++;
                    log.debug("Created new user from GWS: {}", gwsUser.getEmail());
                }
            }

            // Mark users not in GWS as OFFBOARDED
            int offboardedUsers = 0;
            List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);
            for (User user : activeUsers) {
                if (!gwsEmails.contains(user.getEmail().toLowerCase())) {
                    user.setStatus(UserStatus.OFFBOARDED);
                    userRepository.save(user);
                    offboardedUsers++;
                    log.debug("Offboarded user not in GWS: {}", user.getEmail());
                }
            }

            resultBuilder.newUsers(newUsers);
            resultBuilder.updatedUsers(updatedUsers);
            resultBuilder.offboardedUsers(offboardedUsers);

            log.info("GWS sync completed: {} new, {} updated, {} offboarded",
                    newUsers, updatedUsers, offboardedUsers);

        } catch (Exception e) {
            String error = "Google Workspace sync failed: " + e.getMessage();
            log.error(error, e);
            resultBuilder.addError(error);
        }
    }

    private void syncToolAccounts(SyncResultResponse.Builder resultBuilder) {
        log.info("Starting tool account linking phase...");

        List<AITool> enabledTools = aiToolRepository.findByEnabled(true);
        int totalLinked = 0;
        int totalUnmatched = 0;
        int totalSuspended = 0;
        int totalRevoked = 0;

        for (AITool tool : enabledTools) {
            try {
                List<ToolAccountInfo> fetchedUsers = fetchToolUsers(tool);

                if (fetchedUsers == null) {
                    // Tool type not supported or skipped (e.g., GITHUB_COPILOT)
                    continue;
                }

                if (fetchedUsers.isEmpty()) {
                    log.debug("No users fetched for tool: {} ({})", tool.getName(), tool.getToolType());
                    continue;
                }

                AccountLinkingService.LinkResult linkResult =
                        accountLinkingService.linkAccounts(tool, fetchedUsers);

                totalLinked += linkResult.getLinked();
                totalUnmatched += linkResult.getUnmatched();
                totalSuspended += linkResult.getSuspended();
                totalRevoked += linkResult.getRevoked();

            } catch (Exception e) {
                String error = "Failed to sync accounts for tool " + tool.getName() + ": " + e.getMessage();
                log.error(error, e);
                resultBuilder.addError(error);
            }
        }

        resultBuilder.linkedAccounts(totalLinked);
        resultBuilder.unmatchedAccounts(totalUnmatched);
        resultBuilder.suspendedAccounts(totalSuspended);
        resultBuilder.revokedAccounts(totalRevoked);

        log.info("Tool linking completed: {} linked, {} unmatched, {} suspended, {} revoked",
                totalLinked, totalUnmatched, totalSuspended, totalRevoked);
    }

    private List<ToolAccountInfo> fetchToolUsers(AITool tool) {
        AIToolType toolType = tool.getToolType();

        if (toolType == AIToolType.GITHUB_COPILOT) {
            log.debug("Skipping GitHub Copilot tool: {} (deferred to Phase 4)", tool.getName());
            return null;
        }

        if (tool.getApiKey() == null || tool.getApiKey().isBlank()) {
            log.warn("Tool {} ({}) has no API key configured, skipping", tool.getName(), toolType);
            return null;
        }

        if (toolType == AIToolType.CLAUDE) {
            if (claudeApiClient == null) {
                log.warn("Claude API client is not available, skipping tool: {}", tool.getName());
                return null;
            }
            return claudeApiClient.fetchUsers(tool.getApiKey(), tool.getApiBaseUrl(), tool.getApiOrgId());
        }

        if (toolType == AIToolType.CURSOR) {
            if (cursorApiClient == null) {
                log.warn("Cursor API client is not available, skipping tool: {}", tool.getName());
                return null;
            }
            return cursorApiClient.fetchUsers(tool.getApiKey(), tool.getApiBaseUrl(), tool.getApiOrgId());
        }

        log.debug("Unsupported tool type for sync: {} ({})", tool.getName(), toolType);
        return null;
    }

    // Setter methods for optional dependencies (used in tests)
    void setGoogleWorkspaceService(GoogleWorkspaceService googleWorkspaceService) {
        this.googleWorkspaceService = googleWorkspaceService;
    }

    void setClaudeApiClient(ClaudeApiClient claudeApiClient) {
        this.claudeApiClient = claudeApiClient;
    }

    void setCursorApiClient(CursorApiClient cursorApiClient) {
        this.cursorApiClient = cursorApiClient;
    }
}
