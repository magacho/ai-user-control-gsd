package com.bemobi.aiusercontrol.service;

import com.bemobi.aiusercontrol.aitool.repository.AIToolRepository;
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
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncOrchestratorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIToolRepository aiToolRepository;

    @Mock
    private AccountLinkingService accountLinkingService;

    @Mock
    private GoogleWorkspaceService googleWorkspaceService;

    @Mock
    private ClaudeApiClient claudeApiClient;

    @Mock
    private CursorApiClient cursorApiClient;

    @Mock
    private GitHubCopilotClient gitHubCopilotClient;

    private SyncOrchestrator syncOrchestrator;

    @BeforeEach
    void setUp() {
        syncOrchestrator = new SyncOrchestrator(userRepository, aiToolRepository, accountLinkingService,
                claudeApiClient, cursorApiClient, gitHubCopilotClient);
        syncOrchestrator.setGoogleWorkspaceService(googleWorkspaceService);
    }

    @Test
    void testAIFirstSync_withClaudeAndCursorSeats_createsUsersFromGWSValidation() {
        // Given: Two enabled tools (Claude, Cursor) return seats. GWS validates some emails.
        AITool claudeTool = AITool.builder()
                .id(1L).name("Claude").toolType(AIToolType.CLAUDE)
                .apiKey("sk-ant-key").apiBaseUrl("https://api.anthropic.com").apiOrgId("org-123")
                .enabled(true).build();
        AITool cursorTool = AITool.builder()
                .id(2L).name("Cursor").toolType(AIToolType.CURSOR)
                .apiKey("cursor-key").apiBaseUrl("https://api.cursor.com").apiOrgId("org-456")
                .enabled(true).build();

        List<ToolAccountInfo> claudeSeats = List.of(
                new ToolAccountInfo("user-1", "alice@bemobi.com"),
                new ToolAccountInfo("user-2", "external@gmail.com"));
        List<ToolAccountInfo> cursorSeats = List.of(
                new ToolAccountInfo("cursor-1", "alice@bemobi.com"),
                new ToolAccountInfo("cursor-2", "bob@bemobi.com"));

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(claudeTool, cursorTool));
        when(claudeApiClient.fetchUsers("sk-ant-key", "https://api.anthropic.com", "org-123"))
                .thenReturn(claudeSeats);
        when(cursorApiClient.fetchUsers("cursor-key", "https://api.cursor.com", "org-456"))
                .thenReturn(cursorSeats);

        // GWS validates alice and bob, external@gmail.com not found
        GoogleWorkspaceService.GwsUser aliceGws = new GoogleWorkspaceService.GwsUser(
                "alice@bemobi.com", "Alice Smith", "https://photo/alice", "alicegh", "Engineering");
        GoogleWorkspaceService.GwsUser bobGws = new GoogleWorkspaceService.GwsUser(
                "bob@bemobi.com", "Bob Jones", "https://photo/bob", "bobgh", "Product");

        when(googleWorkspaceService.lookupUserByEmail("alice@bemobi.com")).thenReturn(Optional.of(aliceGws));
        when(googleWorkspaceService.lookupUserByEmail("bob@bemobi.com")).thenReturn(Optional.of(bobGws));
        when(googleWorkspaceService.lookupUserByEmail("external@gmail.com")).thenReturn(Optional.empty());

        // No existing users in DB
        when(userRepository.findByEmail("alice@bemobi.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("bob@bemobi.com")).thenReturn(Optional.empty());

        // AccountLinkingService returns results
        when(accountLinkingService.linkAccounts(eq(claudeTool), anyList()))
                .thenReturn(new AccountLinkingService.LinkResult(1, 1, 0, 0));
        when(accountLinkingService.linkAccounts(eq(cursorTool), anyList()))
                .thenReturn(new AccountLinkingService.LinkResult(2, 0, 0, 0));

        // No legacy users
        when(userRepository.findUsersWithoutAIToolAccounts()).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: 2 new users created (alice, bob), 1 external (external@gmail.com), 2 GWS-validated
        assertEquals(2, result.getNewUsers());
        assertEquals(0, result.getUpdatedUsers());
        assertEquals(1, result.getExternalAccounts());
        assertEquals(2, result.getGwsValidatedUsers());
        assertEquals(3, result.getLinkedAccounts()); // 1 from Claude + 2 from Cursor
        assertEquals(1, result.getUnmatchedAccounts()); // 1 from Claude (external)

        // Verify users were saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, org.mockito.Mockito.atLeast(2)).save(userCaptor.capture());

        List<User> savedUsers = userCaptor.getAllValues();
        assertTrue(savedUsers.stream().anyMatch(u ->
                "alice@bemobi.com".equals(u.getEmail()) && "AI_SEAT_GWS_VALIDATED".equals(u.getValidationSource())));
        assertTrue(savedUsers.stream().anyMatch(u ->
                "bob@bemobi.com".equals(u.getEmail()) && "AI_SEAT_GWS_VALIDATED".equals(u.getValidationSource())));
    }

    @Test
    void testParallelSeatFetching_oneToolFails_othersSucceed() {
        // Given: Claude returns seats, Cursor throws exception
        AITool claudeTool = AITool.builder()
                .id(1L).name("Claude").toolType(AIToolType.CLAUDE)
                .apiKey("sk-ant-key").apiBaseUrl("https://api.anthropic.com").apiOrgId("org-123")
                .enabled(true).build();
        AITool cursorTool = AITool.builder()
                .id(2L).name("Cursor").toolType(AIToolType.CURSOR)
                .apiKey("cursor-key").apiBaseUrl("https://api.cursor.com").apiOrgId("org-456")
                .enabled(true).build();

        List<ToolAccountInfo> claudeSeats = List.of(
                new ToolAccountInfo("user-1", "john@bemobi.com"));

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(claudeTool, cursorTool));
        when(claudeApiClient.fetchUsers("sk-ant-key", "https://api.anthropic.com", "org-123"))
                .thenReturn(claudeSeats);
        when(cursorApiClient.fetchUsers("cursor-key", "https://api.cursor.com", "org-456"))
                .thenThrow(new RuntimeException("Connection refused"));

        GoogleWorkspaceService.GwsUser johnGws = new GoogleWorkspaceService.GwsUser(
                "john@bemobi.com", "John Doe", "https://photo/john", "johndoe", "Engineering");
        when(googleWorkspaceService.lookupUserByEmail("john@bemobi.com")).thenReturn(Optional.of(johnGws));
        when(userRepository.findByEmail("john@bemobi.com")).thenReturn(Optional.empty());

        when(accountLinkingService.linkAccounts(eq(claudeTool), anyList()))
                .thenReturn(new AccountLinkingService.LinkResult(1, 0, 0, 0));

        when(userRepository.findUsersWithoutAIToolAccounts()).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: sync continues with Claude seats, Cursor failure is logged but not fatal
        assertNotNull(result);
        assertEquals(1, result.getNewUsers());
        assertEquals(1, result.getGwsValidatedUsers());
        assertEquals(1, result.getLinkedAccounts());
        // No error in result because the failure is caught in CompletableFuture, not in linkAccounts
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testGWSValidation_emailNotFound_noUserCreated() {
        // Given: Seat email not in GWS
        AITool claudeTool = AITool.builder()
                .id(1L).name("Claude").toolType(AIToolType.CLAUDE)
                .apiKey("sk-ant-key").apiBaseUrl("https://api.anthropic.com").apiOrgId("org-123")
                .enabled(true).build();

        List<ToolAccountInfo> seats = List.of(
                new ToolAccountInfo("user-1", "unknown@external.com"));

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(claudeTool));
        when(claudeApiClient.fetchUsers("sk-ant-key", "https://api.anthropic.com", "org-123"))
                .thenReturn(seats);
        when(googleWorkspaceService.lookupUserByEmail("unknown@external.com")).thenReturn(Optional.empty());

        when(accountLinkingService.linkAccounts(eq(claudeTool), anyList()))
                .thenReturn(new AccountLinkingService.LinkResult(0, 1, 0, 0));

        when(userRepository.findUsersWithoutAIToolAccounts()).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: No User record created, externalAccounts incremented
        assertEquals(0, result.getNewUsers());
        assertEquals(0, result.getUpdatedUsers());
        assertEquals(1, result.getExternalAccounts());
        assertEquals(0, result.getGwsValidatedUsers());

        // User.save should not be called for user creation (only for legacy archival, which is empty)
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGWSValidation_gwsUnavailable_skipsValidation() {
        // Given: GoogleWorkspaceService is null (disabled)
        syncOrchestrator.setGoogleWorkspaceService(null);

        AITool claudeTool = AITool.builder()
                .id(1L).name("Claude").toolType(AIToolType.CLAUDE)
                .apiKey("sk-ant-key").apiBaseUrl("https://api.anthropic.com").apiOrgId("org-123")
                .enabled(true).build();

        List<ToolAccountInfo> seats = List.of(
                new ToolAccountInfo("user-1", "alice@bemobi.com"));

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(claudeTool));
        when(claudeApiClient.fetchUsers("sk-ant-key", "https://api.anthropic.com", "org-123"))
                .thenReturn(seats);

        when(accountLinkingService.linkAccounts(eq(claudeTool), anyList()))
                .thenReturn(new AccountLinkingService.LinkResult(0, 1, 0, 0));

        when(userRepository.findUsersWithoutAIToolAccounts()).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: Sync still processes seats but creates no users
        assertNotNull(result);
        assertEquals(0, result.getNewUsers());
        assertEquals(0, result.getUpdatedUsers());
        // All emails are "external" because GWS lookup was skipped
        assertEquals(1, result.getExternalAccounts());
        assertEquals(0, result.getGwsValidatedUsers());

        // GWS should never be called
        verify(googleWorkspaceService, never()).lookupUserByEmail(anyString());
    }

    @Test
    void testDisappearedSeats_markedSuspendedThenRevoked() {
        // Given: AccountLinkingService reports suspended/revoked accounts for disappeared seats
        AITool claudeTool = AITool.builder()
                .id(1L).name("Claude").toolType(AIToolType.CLAUDE)
                .apiKey("sk-ant-key").apiBaseUrl("https://api.anthropic.com").apiOrgId("org-123")
                .enabled(true).build();

        // Current sync only returns user-2 (user-1 has disappeared)
        List<ToolAccountInfo> seats = List.of(
                new ToolAccountInfo("user-2", "bob@bemobi.com"));

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(claudeTool));
        when(claudeApiClient.fetchUsers("sk-ant-key", "https://api.anthropic.com", "org-123"))
                .thenReturn(seats);

        GoogleWorkspaceService.GwsUser bobGws = new GoogleWorkspaceService.GwsUser(
                "bob@bemobi.com", "Bob Jones", "https://photo/bob", "bobgh", "Product");
        when(googleWorkspaceService.lookupUserByEmail("bob@bemobi.com")).thenReturn(Optional.of(bobGws));
        when(userRepository.findByEmail("bob@bemobi.com")).thenReturn(Optional.empty());

        // AccountLinkingService detects disappeared seat user-1 and suspends it
        when(accountLinkingService.linkAccounts(eq(claudeTool), anyList()))
                .thenReturn(new AccountLinkingService.LinkResult(0, 0, 1, 0));

        when(userRepository.findUsersWithoutAIToolAccounts()).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: suspended/revoked counters come from AccountLinkingService
        assertEquals(1, result.getSuspendedAccounts());
        assertEquals(0, result.getRevokedAccounts());

        // Verify linkAccounts was called with the current seats
        verify(accountLinkingService).linkAccounts(eq(claudeTool), anyList());
    }

    @Test
    void testLegacyUsersWithoutAISeats_archivedAsInactive() {
        // Given: Users in DB with no AI tool accounts and validationSource = GWS_LEGACY
        when(aiToolRepository.findByEnabled(true)).thenReturn(Collections.emptyList());

        User legacyUser1 = User.builder()
                .id(1L).email("legacy1@bemobi.com").name("Legacy One")
                .status(UserStatus.ACTIVE).validationSource("GWS_LEGACY").build();
        User legacyUser2 = User.builder()
                .id(2L).email("legacy2@bemobi.com").name("Legacy Two")
                .status(UserStatus.ACTIVE).validationSource("GWS_LEGACY").build();
        User nonLegacyUser = User.builder()
                .id(3L).email("active@bemobi.com").name("Active AI User")
                .status(UserStatus.ACTIVE).validationSource("AI_SEAT_GWS_VALIDATED").build();

        when(userRepository.findUsersWithoutAIToolAccounts())
                .thenReturn(List.of(legacyUser1, legacyUser2, nonLegacyUser));

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: Only GWS_LEGACY users are archived (not AI_SEAT_GWS_VALIDATED ones)
        assertEquals(2, result.getArchivedLegacyUsers());
        assertEquals(UserStatus.INACTIVE, legacyUser1.getStatus());
        assertEquals(UserStatus.INACTIVE, legacyUser2.getStatus());
        assertEquals(UserStatus.ACTIVE, nonLegacyUser.getStatus()); // Not archived

        // Verify save called for legacy users but NOT for the AI-validated user
        verify(userRepository).save(legacyUser1);
        verify(userRepository).save(legacyUser2);
        verify(userRepository, never()).save(nonLegacyUser);
    }

    @Test
    void testGitHubCopilotSeats_matchedByGithubUsername() {
        // Given: GitHub Copilot returns seats with login identifiers, users in DB have matching githubUsername
        AITool copilotTool = AITool.builder()
                .id(3L).name("GitHub Copilot").toolType(AIToolType.GITHUB_COPILOT)
                .apiKey("ghp-key").apiOrgId("bemobi-org")
                .enabled(true).build();

        List<ToolAccountInfo> copilotSeats = List.of(
                new ToolAccountInfo("alicegh", null),
                new ToolAccountInfo("unknowngh", null));

        when(aiToolRepository.findByEnabled(true)).thenReturn(List.of(copilotTool));
        when(gitHubCopilotClient.fetchSeats("ghp-key", "bemobi-org")).thenReturn(copilotSeats);

        // Alice exists in DB with matching githubUsername
        User aliceUser = User.builder()
                .id(1L).email("alice@bemobi.com").name("Alice Smith")
                .githubUsername("alicegh").status(UserStatus.ACTIVE)
                .validationSource("AI_SEAT_GWS_VALIDATED").build();

        when(userRepository.findByGithubUsername("alicegh")).thenReturn(Optional.of(aliceUser));
        when(userRepository.findByGithubUsername("unknowngh")).thenReturn(Optional.empty());

        // AccountLinkingService receives enriched seats (alice has email, unknown stays null)
        ArgumentCaptor<List<ToolAccountInfo>> seatsCaptor = ArgumentCaptor.forClass(List.class);
        when(accountLinkingService.linkAccounts(eq(copilotTool), seatsCaptor.capture()))
                .thenReturn(new AccountLinkingService.LinkResult(1, 1, 0, 0));

        when(userRepository.findUsersWithoutAIToolAccounts()).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: GitHub seats enriched, linking performed
        assertEquals(1, result.getLinkedAccounts());
        assertEquals(1, result.getUnmatchedAccounts());

        // Verify the enriched seats were passed to AccountLinkingService
        List<ToolAccountInfo> enrichedSeats = seatsCaptor.getValue();
        assertEquals(2, enrichedSeats.size());

        // Alice's seat should be enriched with her email
        ToolAccountInfo aliceSeat = enrichedSeats.stream()
                .filter(s -> "alicegh".equals(s.getIdentifier()))
                .findFirst().orElseThrow();
        assertEquals("alice@bemobi.com", aliceSeat.getEmail());

        // Unknown seat stays with null email
        ToolAccountInfo unknownSeat = enrichedSeats.stream()
                .filter(s -> "unknowngh".equals(s.getIdentifier()))
                .findFirst().orElseThrow();
        assertEquals(null, unknownSeat.getEmail());
    }

    @Test
    void testFullSync_emptyTools_noErrors() {
        // Given: No enabled tools
        when(aiToolRepository.findByEnabled(true)).thenReturn(Collections.emptyList());
        when(userRepository.findUsersWithoutAIToolAccounts()).thenReturn(Collections.emptyList());

        // When
        SyncResultResponse result = syncOrchestrator.executeFullSync();

        // Then: Sync completes successfully with zero counts
        assertNotNull(result);
        assertEquals(0, result.getNewUsers());
        assertEquals(0, result.getUpdatedUsers());
        assertEquals(0, result.getExternalAccounts());
        assertEquals(0, result.getArchivedLegacyUsers());
        assertEquals(0, result.getGwsValidatedUsers());
        assertEquals(0, result.getLinkedAccounts());
        assertEquals(0, result.getUnmatchedAccounts());
        assertEquals(0, result.getSuspendedAccounts());
        assertEquals(0, result.getRevokedAccounts());
        assertTrue(result.getErrors().isEmpty());

        // No interactions with external services
        verify(accountLinkingService, never()).linkAccounts(any(), anyList());
        verify(googleWorkspaceService, never()).lookupUserByEmail(anyString());
    }
}
