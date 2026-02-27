package com.bemobi.aiusercontrol.service;

import com.bemobi.aiusercontrol.dto.response.ToolAccountInfo;
import com.bemobi.aiusercontrol.enums.AccountStatus;
import com.bemobi.aiusercontrol.exception.ResourceNotFoundException;
import com.bemobi.aiusercontrol.model.entity.AITool;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class AccountLinkingService {

    private static final Logger log = LoggerFactory.getLogger(AccountLinkingService.class);

    private final UserRepository userRepository;
    private final UserAIToolAccountRepository userAIToolAccountRepository;

    public AccountLinkingService(UserRepository userRepository,
                                 UserAIToolAccountRepository userAIToolAccountRepository) {
        this.userRepository = userRepository;
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    public LinkResult linkAccounts(AITool tool, List<ToolAccountInfo> toolAccounts) {
        log.info("Linking {} tool accounts for tool: {} ({})", toolAccounts.size(), tool.getName(), tool.getToolType());

        int linked = 0;
        int unmatched = 0;
        int suspended = 0;
        int revoked = 0;

        Set<String> currentIdentifiers = new HashSet<>();

        for (ToolAccountInfo accountInfo : toolAccounts) {
            currentIdentifiers.add(accountInfo.getIdentifier());

            Optional<UserAIToolAccount> existingAccount =
                    userAIToolAccountRepository.findByAiToolAndAccountIdentifier(tool, accountInfo.getIdentifier());

            if (existingAccount.isPresent()) {
                // Account already exists - update lastSeenAt and ensure ACTIVE status
                UserAIToolAccount account = existingAccount.get();
                account.setLastSeenAt(Instant.now());
                account.setStatus(AccountStatus.ACTIVE);
                account.setAccountEmail(accountInfo.getEmail());

                // Re-link user if previously unmatched and email is now available
                if (account.getUser() == null && accountInfo.getEmail() != null) {
                    User matchedUser = findUserByEmail(accountInfo.getEmail());
                    if (matchedUser != null) {
                        account.setUser(matchedUser);
                        linked++;
                        log.debug("Re-linked previously unmatched account {} to user {} for tool: {}",
                                accountInfo.getIdentifier(), matchedUser.getEmail(), tool.getName());
                    }
                }

                // Persist source dates: createdAtSource is write-once, lastActivityAt always overwrites with non-null
                if (accountInfo.getCreatedAtSource() != null && account.getCreatedAtSource() == null) {
                    account.setCreatedAtSource(accountInfo.getCreatedAtSource());
                }
                if (accountInfo.getLastActivityAt() != null) {
                    account.setLastActivityAt(accountInfo.getLastActivityAt());
                }

                userAIToolAccountRepository.save(account);
                log.debug("Updated existing account: {} for tool: {}", accountInfo.getIdentifier(), tool.getName());
            } else {
                // New account - try to match by email
                User matchedUser = findUserByEmail(accountInfo.getEmail());

                UserAIToolAccount.Builder accountBuilder = UserAIToolAccount.builder()
                        .user(matchedUser)
                        .aiTool(tool)
                        .accountIdentifier(accountInfo.getIdentifier())
                        .accountEmail(accountInfo.getEmail())
                        .status(AccountStatus.ACTIVE)
                        .firstSeenAt(Instant.now())
                        .lastSeenAt(Instant.now());

                if (accountInfo.getCreatedAtSource() != null) {
                    accountBuilder.createdAtSource(accountInfo.getCreatedAtSource());
                }
                if (accountInfo.getLastActivityAt() != null) {
                    accountBuilder.lastActivityAt(accountInfo.getLastActivityAt());
                }

                UserAIToolAccount newAccount = accountBuilder.build();

                userAIToolAccountRepository.save(newAccount);

                if (matchedUser != null) {
                    linked++;
                    log.debug("Linked new account {} to user {} for tool: {}",
                            accountInfo.getIdentifier(), matchedUser.getEmail(), tool.getName());
                } else {
                    unmatched++;
                    log.debug("Unmatched account {} ({}) for tool: {} - no corporate user found",
                            accountInfo.getIdentifier(), accountInfo.getEmail(), tool.getName());
                }
            }
        }

        // Handle disappeared accounts: find accounts for this tool that are NOT in the current fetch
        List<UserAIToolAccount> existingAccounts = userAIToolAccountRepository.findByAiTool(tool);
        for (UserAIToolAccount account : existingAccounts) {
            if (!currentIdentifiers.contains(account.getAccountIdentifier())) {
                if (account.getStatus() == AccountStatus.ACTIVE) {
                    // First disappearance: ACTIVE -> SUSPENDED
                    account.setStatus(AccountStatus.SUSPENDED);
                    userAIToolAccountRepository.save(account);
                    suspended++;
                    log.debug("Suspended disappeared account: {} for tool: {}",
                            account.getAccountIdentifier(), tool.getName());
                } else if (account.getStatus() == AccountStatus.SUSPENDED) {
                    // Second consecutive disappearance: SUSPENDED -> REVOKED
                    account.setStatus(AccountStatus.REVOKED);
                    userAIToolAccountRepository.save(account);
                    revoked++;
                    log.debug("Revoked disappeared account: {} for tool: {}",
                            account.getAccountIdentifier(), tool.getName());
                }
                // REVOKED accounts stay REVOKED - no further action
            }
        }

        log.info("Link result for tool {}: linked={}, unmatched={}, suspended={}, revoked={}",
                tool.getName(), linked, unmatched, suspended, revoked);

        return new LinkResult(linked, unmatched, suspended, revoked);
    }

    public void unlinkAccount(Long accountId) {
        UserAIToolAccount account = userAIToolAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("UserAIToolAccount", accountId));

        log.info("Unlinking account: {} (tool: {}, user: {})",
                account.getAccountIdentifier(),
                account.getAiTool().getName(),
                account.getUser() != null ? account.getUser().getEmail() : "unmatched");

        userAIToolAccountRepository.delete(account);
    }

    private User findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email.toLowerCase().trim()).orElse(null);
    }

    public static class LinkResult {

        private final int linked;
        private final int unmatched;
        private final int suspended;
        private final int revoked;

        public LinkResult(int linked, int unmatched, int suspended, int revoked) {
            this.linked = linked;
            this.unmatched = unmatched;
            this.suspended = suspended;
            this.revoked = revoked;
        }

        public int getLinked() {
            return linked;
        }

        public int getUnmatched() {
            return unmatched;
        }

        public int getSuspended() {
            return suspended;
        }

        public int getRevoked() {
            return revoked;
        }

        @Override
        public String toString() {
            return "LinkResult{" +
                    "linked=" + linked +
                    ", unmatched=" + unmatched +
                    ", suspended=" + suspended +
                    ", revoked=" + revoked +
                    '}';
        }
    }
}
