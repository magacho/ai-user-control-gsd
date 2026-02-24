package com.bemobi.aiusercontrol.service;

import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.enums.AccountStatus;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.exception.ResourceNotFoundException;
import com.bemobi.aiusercontrol.model.entity.AITool;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountLinkingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAIToolAccountRepository userAIToolAccountRepository;

    private AccountLinkingService accountLinkingService;

    private AITool claudeTool;

    @BeforeEach
    void setUp() {
        accountLinkingService = new AccountLinkingService(userRepository, userAIToolAccountRepository);

        claudeTool = AITool.builder()
                .id(1L)
                .name("Claude")
                .toolType(AIToolType.CLAUDE)
                .enabled(true)
                .build();
    }

    @Test
    void testLinkAccounts_createsNewLinkedAccount() {
        // Given: tool account email matches a corporate user
        ToolAccountInfo accountInfo = new ToolAccountInfo("user-abc", "john@bemobi.com");
        User corporateUser = User.builder()
                .id(1L)
                .email("john@bemobi.com")
                .name("John Doe")
                .status(UserStatus.ACTIVE)
                .build();

        when(userAIToolAccountRepository.findByAiToolAndAccountIdentifier(claudeTool, "user-abc"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@bemobi.com")).thenReturn(Optional.of(corporateUser));
        when(userAIToolAccountRepository.findByAiTool(claudeTool)).thenReturn(Collections.emptyList());

        // When
        AccountLinkingService.LinkResult result = accountLinkingService.linkAccounts(
                claudeTool, List.of(accountInfo));

        // Then
        assertEquals(1, result.getLinked());
        assertEquals(0, result.getUnmatched());

        ArgumentCaptor<UserAIToolAccount> captor = ArgumentCaptor.forClass(UserAIToolAccount.class);
        verify(userAIToolAccountRepository).save(captor.capture());
        UserAIToolAccount saved = captor.getValue();
        assertNotNull(saved.getUser());
        assertEquals("john@bemobi.com", saved.getUser().getEmail());
        assertEquals("user-abc", saved.getAccountIdentifier());
        assertEquals("john@bemobi.com", saved.getAccountEmail());
        assertEquals(AccountStatus.ACTIVE, saved.getStatus());
    }

    @Test
    void testLinkAccounts_createsUnmatchedAccount() {
        // Given: tool account email does NOT match any corporate user
        ToolAccountInfo accountInfo = new ToolAccountInfo("ext-user", "external@gmail.com");

        when(userAIToolAccountRepository.findByAiToolAndAccountIdentifier(claudeTool, "ext-user"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("external@gmail.com")).thenReturn(Optional.empty());
        when(userAIToolAccountRepository.findByAiTool(claudeTool)).thenReturn(Collections.emptyList());

        // When
        AccountLinkingService.LinkResult result = accountLinkingService.linkAccounts(
                claudeTool, List.of(accountInfo));

        // Then
        assertEquals(0, result.getLinked());
        assertEquals(1, result.getUnmatched());

        ArgumentCaptor<UserAIToolAccount> captor = ArgumentCaptor.forClass(UserAIToolAccount.class);
        verify(userAIToolAccountRepository).save(captor.capture());
        UserAIToolAccount saved = captor.getValue();
        assertNull(saved.getUser());
        assertEquals("ext-user", saved.getAccountIdentifier());
        assertEquals("external@gmail.com", saved.getAccountEmail());
    }

    @Test
    void testLinkAccounts_updatesExistingAccountLastSeen() {
        // Given: account already exists in the database
        ToolAccountInfo accountInfo = new ToolAccountInfo("existing-user", "john@bemobi.com");
        Instant previousLastSeen = Instant.parse("2026-01-01T00:00:00Z");

        UserAIToolAccount existingAccount = UserAIToolAccount.builder()
                .id(10L)
                .aiTool(claudeTool)
                .accountIdentifier("existing-user")
                .accountEmail("john@bemobi.com")
                .status(AccountStatus.ACTIVE)
                .firstSeenAt(Instant.parse("2025-12-01T00:00:00Z"))
                .lastSeenAt(previousLastSeen)
                .build();

        when(userAIToolAccountRepository.findByAiToolAndAccountIdentifier(claudeTool, "existing-user"))
                .thenReturn(Optional.of(existingAccount));
        when(userAIToolAccountRepository.findByAiTool(claudeTool)).thenReturn(List.of(existingAccount));

        // When
        AccountLinkingService.LinkResult result = accountLinkingService.linkAccounts(
                claudeTool, List.of(accountInfo));

        // Then: lastSeenAt should be updated (not equal to previous value)
        assertEquals(0, result.getLinked());
        assertEquals(0, result.getUnmatched());
        assertNotNull(existingAccount.getLastSeenAt());
        assertEquals(AccountStatus.ACTIVE, existingAccount.getStatus());
    }

    @Test
    void testLinkAccounts_suspendsDisappearedAccount() {
        // Given: account in DB was ACTIVE, but not in current fetch
        UserAIToolAccount activeAccount = UserAIToolAccount.builder()
                .id(20L)
                .aiTool(claudeTool)
                .accountIdentifier("disappeared-user")
                .accountEmail("gone@bemobi.com")
                .status(AccountStatus.ACTIVE)
                .firstSeenAt(Instant.parse("2025-12-01T00:00:00Z"))
                .lastSeenAt(Instant.parse("2026-01-15T00:00:00Z"))
                .build();

        when(userAIToolAccountRepository.findByAiTool(claudeTool)).thenReturn(List.of(activeAccount));

        // When: fetch returns empty list (no accounts currently)
        AccountLinkingService.LinkResult result = accountLinkingService.linkAccounts(
                claudeTool, Collections.emptyList());

        // Then: ACTIVE -> SUSPENDED
        assertEquals(1, result.getSuspended());
        assertEquals(0, result.getRevoked());
        assertEquals(AccountStatus.SUSPENDED, activeAccount.getStatus());
    }

    @Test
    void testLinkAccounts_revokesAlreadySuspendedAccount() {
        // Given: account was already SUSPENDED and still not in fetch
        UserAIToolAccount suspendedAccount = UserAIToolAccount.builder()
                .id(30L)
                .aiTool(claudeTool)
                .accountIdentifier("long-gone-user")
                .accountEmail("revoked@bemobi.com")
                .status(AccountStatus.SUSPENDED)
                .firstSeenAt(Instant.parse("2025-12-01T00:00:00Z"))
                .lastSeenAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        when(userAIToolAccountRepository.findByAiTool(claudeTool)).thenReturn(List.of(suspendedAccount));

        // When: fetch returns empty list (still not present)
        AccountLinkingService.LinkResult result = accountLinkingService.linkAccounts(
                claudeTool, Collections.emptyList());

        // Then: SUSPENDED -> REVOKED
        assertEquals(0, result.getSuspended());
        assertEquals(1, result.getRevoked());
        assertEquals(AccountStatus.REVOKED, suspendedAccount.getStatus());
    }

    @Test
    void testUnlinkAccount_deletesRecord() {
        // Given: account exists
        UserAIToolAccount account = UserAIToolAccount.builder()
                .id(40L)
                .aiTool(claudeTool)
                .accountIdentifier("to-unlink")
                .accountEmail("unlink@bemobi.com")
                .status(AccountStatus.ACTIVE)
                .build();

        when(userAIToolAccountRepository.findById(40L)).thenReturn(Optional.of(account));

        // When
        accountLinkingService.unlinkAccount(40L);

        // Then
        verify(userAIToolAccountRepository).delete(account);
    }

    @Test
    void testUnlinkAccount_throwsNotFound() {
        // Given: account does not exist
        when(userAIToolAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> accountLinkingService.unlinkAccount(999L));
        verify(userAIToolAccountRepository, never()).delete(any());
    }
}
