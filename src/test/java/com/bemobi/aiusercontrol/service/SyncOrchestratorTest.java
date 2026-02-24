package com.bemobi.aiusercontrol.service;

import com.bemobi.aiusercontrol.aitool.repository.AIToolRepository;
import com.bemobi.aiusercontrol.dto.response.SyncResultResponse;
import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.integration.claude.ClaudeApiClient;
import com.bemobi.aiusercontrol.integration.cursor.CursorApiClient;
import com.bemobi.aiusercontrol.integration.google.GoogleWorkspaceService;
import com.bemobi.aiusercontrol.model.entity.AITool;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncOrchestratorTest {

    @Mock
    private GoogleWorkspaceService googleWorkspaceService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIToolRepository aiToolRepository;

    @Mock
    private AccountLinkingService accountLinkingService;

    @Mock
    private ClaudeApiClient claudeApiClient;

    @Mock
    private CursorApiClient cursorApiClient;

    private SyncOrchestrator syncOrchestrator;

    @BeforeEach
    void setUp() {
        syncOrchestrator = new SyncOrchestrator(userRepository, aiToolRepository, accountLinkingService);
        syncOrchestrator.setGoogleWorkspaceService(googleWorkspaceService);
        syncOrchestrator.setClaudeApiClient(claudeApiClient);
        syncOrchestrator.setCursorApiClient(cursorApiClient);
    }

    @Test
    void testFullSync_createsNewUsersFromGWS() {
        // Given: GWS returns users not in DB
        GoogleWorkspaceService.GwsUser gwsUser = new GoogleWorkspaceService.GwsUser(
                "john@bemobi.com", "John Doe", "https://photo.url/john", "johndoe", "Engineering");

        when(googleWorkspaceService.fetchAllUsers()).thenReturn(List.of(gwsUser));
        when(userRepository.findByEmail("john@bemobi.com")).thenReturn(Optional.empty());
        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(Collections.emptyList());
        when(aiToolRepository.findByEnabled(true)).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then
        assertEquals(1, result.getNewUsers());
        assertEquals(0, result.getUpdatedUsers());
        assertEquals(0, result.getOffboardedUsers());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("john@bemobi.com", savedUser.getEmail());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("https://photo.url/john", savedUser.getAvatarUrl());
        assertEquals("johndoe", savedUser.getGithubUsername());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
    }

    @Test
    void testFullSync_updatesExistingUsersFromGWS() {
        // Given: GWS returns user already in DB
        GoogleWorkspaceService.GwsUser gwsUser = new GoogleWorkspaceService.GwsUser(
                "jane@bemobi.com", "Jane Updated", "https://photo.url/jane-new", "janegh", "Product");

        User existingUser = User.builder()
                .id(1L)
                .email("jane@bemobi.com")
                .name("Jane Old")
                .avatarUrl("https://photo.url/jane-old")
                .status(UserStatus.ACTIVE)
                .build();

        when(googleWorkspaceService.fetchAllUsers()).thenReturn(List.of(gwsUser));
        when(userRepository.findByEmail("jane@bemobi.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(List.of(existingUser));
        when(aiToolRepository.findByEnabled(true)).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then
        assertEquals(0, result.getNewUsers());
        assertEquals(1, result.getUpdatedUsers());
        assertEquals(0, result.getOffboardedUsers());

        // Verify user fields overwritten from GWS
        assertEquals("Jane Updated", existingUser.getName());
        assertEquals("https://photo.url/jane-new", existingUser.getAvatarUrl());
        assertEquals("janegh", existingUser.getGithubUsername());
    }

    @Test
    void testFullSync_offboardsRemovedUsers() {
        // Given: No users in GWS, but user exists in DB
        User existingUser = User.builder()
                .id(1L)
                .email("removed@bemobi.com")
                .name("Removed User")
                .status(UserStatus.ACTIVE)
                .build();

        when(googleWorkspaceService.fetchAllUsers()).thenReturn(Collections.emptyList());
        when(userRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(List.of(existingUser));
        when(aiToolRepository.findByEnabled(true)).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then
        assertEquals(0, result.getNewUsers());
        assertEquals(0, result.getUpdatedUsers());
        assertEquals(1, result.getOffboardedUsers());
        assertEquals(UserStatus.OFFBOARDED, existingUser.getStatus());
    }

    @Test
    void testFullSync_linksClaudeAccountsByEmail() {
        // Given: GWS disabled, Claude tool with accounts
        syncOrchestrator.setGoogleWorkspaceService(null);

        AITool claudeTool = AITool.builder()
                .id(1L)
                .name("Claude")
                .toolType(AIToolType.CLAUDE)
                .apiKey("sk-ant-test-key")
                .apiBaseUrl("https://api.anthropic.com")
                .apiOrgId("org-123")
                .enabled(true)
                .build();

        List<ToolAccountInfo> claudeUsers = List.of(
                new ToolAccountInfo("user-1", "john@bemobi.com"),
                new ToolAccountInfo("user-2", "external@gmail.com"));

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(claudeTool));
        when(claudeApiClient.fetchUsers("sk-ant-test-key", "https://api.anthropic.com", "org-123"))
                .thenReturn(claudeUsers);
        when(accountLinkingService.linkAccounts(eq(claudeTool), anyList()))
                .thenReturn(new AccountLinkingService.LinkResult(1, 1, 0, 0));

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then
        verify(accountLinkingService).linkAccounts(eq(claudeTool), eq(claudeUsers));
        assertEquals(1, result.getLinkedAccounts());
        assertEquals(1, result.getUnmatchedAccounts());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Google Workspace")));
    }

    @Test
    void testFullSync_skipsGitHubCopilot() {
        // Given: GWS disabled, GitHub Copilot tool exists
        syncOrchestrator.setGoogleWorkspaceService(null);

        AITool copilotTool = AITool.builder()
                .id(2L)
                .name("GitHub Copilot")
                .toolType(AIToolType.GITHUB_COPILOT)
                .apiKey("ghp-test-key")
                .apiOrgId("bemobi-org")
                .enabled(true)
                .build();

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(copilotTool));

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: AccountLinkingService should NOT be called for GitHub Copilot
        verify(accountLinkingService, never()).linkAccounts(any(), anyList());
        assertEquals(0, result.getLinkedAccounts());
    }

    @Test
    void testFullSync_handlesClaudeApiFailure() {
        // Given: GWS disabled, Claude API throws exception
        syncOrchestrator.setGoogleWorkspaceService(null);

        AITool claudeTool = AITool.builder()
                .id(1L)
                .name("Claude")
                .toolType(AIToolType.CLAUDE)
                .apiKey("sk-ant-test-key")
                .apiBaseUrl("https://api.anthropic.com")
                .apiOrgId("org-123")
                .enabled(true)
                .build();

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(claudeTool));
        when(claudeApiClient.fetchUsers("sk-ant-test-key", "https://api.anthropic.com", "org-123"))
                .thenThrow(new RuntimeException("Connection refused"));

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: Sync completes with errors, does not crash
        assertNotNull(result);
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Failed to sync accounts for tool Claude")));
    }

    @Test
    void testFullSync_handlesGWSDisabled() {
        // Given: GoogleWorkspaceService is null (disabled)
        syncOrchestrator.setGoogleWorkspaceService(null);
        when(aiToolRepository.findByEnabled(true)).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: GWS phase skipped, error message added
        assertEquals(0, result.getNewUsers());
        assertEquals(0, result.getUpdatedUsers());
        assertEquals(0, result.getOffboardedUsers());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Google Workspace integration is not enabled")));
        verify(googleWorkspaceService, never()).fetchAllUsers();
    }
}
