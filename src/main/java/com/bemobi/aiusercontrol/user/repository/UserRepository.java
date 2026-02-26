package com.bemobi.aiusercontrol.user.repository;

import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGithubUsername(String githubUsername);

    List<User> findByStatus(UserStatus status);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u.department FROM User u WHERE u.department IS NOT NULL ORDER BY u.department")
    List<String> findDistinctDepartments();

    @Query("SELECT u FROM User u WHERE NOT EXISTS (SELECT a FROM UserAIToolAccount a WHERE a.user = u)")
    List<User> findUsersWithoutAIToolAccounts();
}
