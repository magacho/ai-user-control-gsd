package com.bemobi.aiusercontrol.aitool.service;

import com.bemobi.aiusercontrol.aitool.repository.AIToolRepository;
import com.bemobi.aiusercontrol.dto.request.AIToolRequest;
import com.bemobi.aiusercontrol.dto.response.AIToolResponse;
import com.bemobi.aiusercontrol.model.entity.AITool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AIToolService {

    private final AIToolRepository aiToolRepository;

    public AIToolService(AIToolRepository aiToolRepository) {
        this.aiToolRepository = aiToolRepository;
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

    private AIToolResponse toResponse(AITool tool) {
        boolean hasKey = tool.getApiKey() != null && !tool.getApiKey().isBlank();
        String maskedKey = maskApiKey(tool.getApiKey());

        return AIToolResponse.builder()
                .id(tool.getId())
                .name(tool.getName())
                .toolType(tool.getToolType() != null ? tool.getToolType().name() : null)
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
