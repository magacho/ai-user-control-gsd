package com.bemobi.aiusercontrol.user.repository;

import com.bemobi.aiusercontrol.enums.AccountStatus;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.model.entity.AITool;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAIToolAccountRepository extends JpaRepository<UserAIToolAccount, Long> {

    List<UserAIToolAccount> findByUser(User user);

    List<UserAIToolAccount> findByUserId(Long userId);

    List<UserAIToolAccount> findByAiTool(AITool aiTool);

    List<UserAIToolAccount> findByAiToolId(Long aiToolId);

    Optional<UserAIToolAccount> findByAiToolAndAccountIdentifier(AITool aiTool, String accountIdentifier);

    List<UserAIToolAccount> findByStatus(AccountStatus status);

    List<UserAIToolAccount> findByUserIsNullOrUserStatusEquals(UserStatus status);

    long countByStatus(AccountStatus status);

    long countByUserIsNull();

    @Query("SELECT a FROM UserAIToolAccount a JOIN FETCH a.aiTool LEFT JOIN FETCH a.user u WHERE u IS NULL OR u.status = 'OFFBOARDED'")
    List<UserAIToolAccount> findPendingAccounts();

    @Query("SELECT a FROM UserAIToolAccount a JOIN FETCH a.aiTool LEFT JOIN FETCH a.user WHERE a.status IN ('SUSPENDED', 'REVOKED')")
    List<UserAIToolAccount> findAccountsToRemove();

    @Query("SELECT a FROM UserAIToolAccount a JOIN FETCH a.aiTool WHERE a.user IS NULL")
    List<UserAIToolAccount> findExternalAccounts();

    @Query("SELECT COUNT(a) FROM UserAIToolAccount a WHERE a.status IN ('SUSPENDED', 'REVOKED')")
    long countAccountsToRemove();

    @Query("SELECT COUNT(a) FROM UserAIToolAccount a WHERE a.user IS NULL")
    long countExternalAccounts();

    @Query("SELECT a FROM UserAIToolAccount a JOIN FETCH a.aiTool WHERE a.user.id IN :userIds")
    List<UserAIToolAccount> findByUserIdIn(List<Long> userIds);
}
