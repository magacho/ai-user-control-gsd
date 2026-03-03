package com.bemobi.aiusercontrol.aitool.service;

import com.bemobi.aiusercontrol.aitool.repository.AIToolRepository;
import com.bemobi.aiusercontrol.dto.request.AIToolRequest;
import com.bemobi.aiusercontrol.dto.response.AIToolResponse;
import com.bemobi.aiusercontrol.dto.response.ToolDetailResponse;
import com.bemobi.aiusercontrol.dto.response.ToolSeatResponse;
import com.bemobi.aiusercontrol.enums.AccountStatus;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.model.entity.AITool;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AIToolService {

    private static final long INACTIVITY_THRESHOLD_DAYS = 60;

    private final AIToolRepository aiToolRepository;
    private final UserAIToolAccountRepository userAIToolAccountRepository;

    public AIToolService(AIToolRepository aiToolRepository, UserAIToolAccountRepository userAIToolAccountRepository) {
        this.aiToolRepository = aiToolRepository;
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    @Transactional(readOnly = true)
    public List<AIToolResponse> findAll() {
        return aiToolRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AIToolResponse> findById(Long id) {
        return aiToolRepository.findById(id)
                .map(this::toResponse);
    }

    public AIToolResponse create(AIToolRequest request) {
        Optional<AITool> existing = aiToolRepository.findByName(request.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("An AI tool with the name '" + request.getName() + "' already exists");
        }

        AITool tool = new AITool();
        tool.setName(request.getName());
        tool.setToolType(request.getToolType());
        tool.setDescription(request.getDescription());
        tool.setApiBaseUrl(request.getApiBaseUrl());
        tool.setEnabled(request.isEnabled());
        tool.setApiKey(request.getApiKey());
        tool.setApiOrgId(request.getApiOrgId());
        tool.setIconUrl(request.getIconUrl());

        AITool saved = aiToolRepository.save(tool);
        return toResponse(saved);
    }

    public AIToolResponse update(Long id, AIToolRequest request) {
        AITool tool = aiToolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AI tool not found with id: " + id));

        Optional<AITool> existingWithName = aiToolRepository.findByName(request.getName());
        if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
            throw new IllegalArgumentException("An AI tool with the name '" + request.getName() + "' already exists");
        }

        tool.setName(request.getName());
        tool.setToolType(request.getToolType());
        tool.setDescription(request.getDescription());
        tool.setApiBaseUrl(request.getApiBaseUrl());
        tool.setEnabled(request.isEnabled());
        // Only update API key if a new value is provided (non-blank)
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            tool.setApiKey(request.getApiKey());
        }
        tool.setApiOrgId(request.getApiOrgId());
        tool.setIconUrl(request.getIconUrl());

        AITool saved = aiToolRepository.save(tool);
        return toResponse(saved);
    }

    public void delete(Long id) {
        AITool tool = aiToolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AI tool not found with id: " + id));
        aiToolRepository.delete(tool);
    }

    @Transactional(readOnly = true)
    public long count() {
        return aiToolRepository.count();
    }

    @Transactional(readOnly = true)
    public ToolDetailResponse getToolDetail(Long toolId) {
        AITool tool = aiToolRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("AI tool not found with id: " + toolId));

        List<UserAIToolAccount> accounts = userAIToolAccountRepository.findByAiToolId(toolId);
        Instant inactivityCutoff = Instant.now().minus(INACTIVITY_THRESHOLD_DAYS, ChronoUnit.DAYS);

        List<ToolSeatResponse> seats = accounts.stream()
                .map(account -> toSeatResponse(account, inactivityCutoff))
                .collect(Collectors.toList());

        long activeSeats = accounts.stream().filter(a -> a.getStatus() == AccountStatus.ACTIVE).count();
        long suspendedSeats = accounts.stream().filter(a -> a.getStatus() == AccountStatus.SUSPENDED).count();
        long revokedSeats = accounts.stream().filter(a -> a.getStatus() == AccountStatus.REVOKED).count();
        long externalSeats = accounts.stream().filter(a -> a.getUser() == null).count();
        long inactiveSeats = accounts.stream()
                .filter(a -> a.getLastActivityAt() != null && a.getLastActivityAt().isBefore(inactivityCutoff))
                .count();
        long offboardedUserSeats = accounts.stream()
                .filter(a -> a.getUser() != null && a.getUser().getStatus() == UserStatus.OFFBOARDED)
                .count();

        return ToolDetailResponse.builder()
                .id(tool.getId())
                .name(tool.getName())
                .toolType(tool.getToolType() != null ? tool.getToolType().name() : null)
                .toolTypeDisplay(tool.getToolType() != null ? tool.getToolType().getDisplayName() : null)
                .toolTypeIcon(tool.getToolType() != null ? tool.getToolType().getIconPath() : null)
                .description(tool.getDescription())
                .enabled(tool.isEnabled())
                .createdAt(tool.getCreatedAt())
                .totalSeats(accounts.size())
                .activeSeats(activeSeats)
                .suspendedSeats(suspendedSeats)
                .revokedSeats(revokedSeats)
                .externalSeats(externalSeats)
                .inactiveSeats(inactiveSeats)
                .offboardedUserSeats(offboardedUserSeats)
                .seats(seats)
                .build();
    }

    private ToolSeatResponse toSeatResponse(UserAIToolAccount account, Instant inactivityCutoff) {
        User user = account.getUser();
        boolean isExternal = user == null;
        boolean isInactive = account.getLastActivityAt() != null
                && account.getLastActivityAt().isBefore(inactivityCutoff);
        boolean isOffboarded = user != null && user.getStatus() == UserStatus.OFFBOARDED;

        ToolSeatResponse.Builder builder = ToolSeatResponse.builder()
                .id(account.getId())
                .accountIdentifier(account.getAccountIdentifier())
                .accountEmail(account.getAccountEmail())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .createdAtSource(account.getCreatedAtSource())
                .lastActivityAt(account.getLastActivityAt())
                .firstSeenAt(account.getFirstSeenAt())
                .lastSeenAt(account.getLastSeenAt())
                .lastSyncedAt(account.getLastSyncedAt())
                .createdAt(account.getCreatedAt())
                .external(isExternal)
                .inactive(isInactive)
                .userOffboarded(isOffboarded);

        if (user != null) {
            builder.userId(user.getId())
                    .userName(user.getName())
                    .userEmail(user.getEmail())
                    .userDepartment(user.getDepartment())
                    .userGithubUsername(user.getGithubUsername())
                    .userStatus(user.getStatus() != null ? user.getStatus().name() : null);
        }

        return builder.build();
    }

    private AIToolResponse toResponse(AITool tool) {
        boolean hasKey = tool.getApiKey() != null && !tool.getApiKey().isBlank();
        String maskedKey = maskApiKey(tool.getApiKey());

        return AIToolResponse.builder()
                .id(tool.getId())
                .name(tool.getName())
                .toolType(tool.getToolType() != null ? tool.getToolType().name() : null)
                .toolTypeDisplay(tool.getToolType() != null ? tool.getToolType().getDisplayName() : null)
                .toolTypeIcon(tool.getToolType() != null ? tool.getToolType().getIconPath() : null)
                .description(tool.getDescription())
                .apiBaseUrl(tool.getApiBaseUrl())
                .enabled(tool.isEnabled())
                .apiKey(maskedKey)
                .apiOrgId(tool.getApiOrgId())
                .hasApiKey(hasKey)
                .iconUrl(tool.getIconUrl())
                .createdAt(tool.getCreatedAt())
                .updatedAt(tool.getUpdatedAt())
                .build();
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "";
        }
        if (apiKey.length() <= 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }
}
