package com.bemobi.aiusercontrol.user.service;

import com.bemobi.aiusercontrol.dto.response.UserAccountResponse;
import com.bemobi.aiusercontrol.dto.response.UserDetailResponse;
import com.bemobi.aiusercontrol.dto.response.UserResponse;
import com.bemobi.aiusercontrol.exception.ResourceNotFoundException;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserAIToolAccountRepository userAIToolAccountRepository;

    public UserService(UserRepository userRepository,
                       UserAIToolAccountRepository userAIToolAccountRepository) {
        this.userRepository = userRepository;
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse);
    }

    public long count() {
        return userRepository.count();
    }

    public UserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<UserAIToolAccount> accounts = userAIToolAccountRepository.findByUserId(userId);

        List<UserAccountResponse> accountResponses = accounts.stream()
                .map(this::toAccountResponse)
                .collect(Collectors.toList());

        String statusLabel = getStatusLabel(user.getStatus() != null ? user.getStatus().name() : null);

        return UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .department(user.getDepartment())
                .avatarUrl(user.getAvatarUrl())
                .githubUsername(user.getGithubUsername())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .statusLabel(statusLabel)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .accounts(accountResponses)
                .build();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .department(user.getDepartment())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private UserAccountResponse toAccountResponse(UserAIToolAccount account) {
        String accountStatusLabel = getAccountStatusLabel(
                account.getStatus() != null ? account.getStatus().name() : null);

        return UserAccountResponse.builder()
                .id(account.getId())
                .toolName(account.getAiTool() != null ? account.getAiTool().getName() : "Unknown")
                .toolType(account.getAiTool() != null ? account.getAiTool().getToolType().name() : "UNKNOWN")
                .accountIdentifier(account.getAccountIdentifier())
                .accountEmail(account.getAccountEmail())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .statusLabel(accountStatusLabel)
                .lastSeenAt(account.getLastSeenAt())
                .firstSeenAt(account.getFirstSeenAt())
                .build();
    }

    private String getStatusLabel(String status) {
        if (status == null) {
            return "Unknown";
        }
        switch (status) {
            case "ACTIVE": return "Active";
            case "INACTIVE": return "Inactive";
            case "OFFBOARDED": return "Offboarded";
            default: return status;
        }
    }

    private String getAccountStatusLabel(String status) {
        if (status == null) {
            return "Unknown";
        }
        switch (status) {
            case "ACTIVE": return "Active";
            case "SUSPENDED": return "Suspended";
            case "REVOKED": return "Revoked";
            default: return status;
        }
    }
}
