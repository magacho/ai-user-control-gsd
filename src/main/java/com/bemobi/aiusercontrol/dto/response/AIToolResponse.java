package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;

public class AIToolResponse {

    private Long id;
    private String name;
    private String toolType;
    private String toolTypeDisplay;
    private String toolTypeIcon;
    private String description;
    private String apiBaseUrl;
    private boolean enabled;
    private String apiKey;
    private String apiOrgId;
    private boolean hasApiKey;
    private String iconUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public AIToolResponse() {
    }

    public AIToolResponse(Long id, String name, String toolType, String toolTypeDisplay, String toolTypeIcon,
                          String description, String apiBaseUrl,
                          boolean enabled, String apiKey, String apiOrgId, boolean hasApiKey,
                          String iconUrl, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.toolType = toolType;
        this.toolTypeDisplay = toolTypeDisplay;
        this.toolTypeIcon = toolTypeIcon;
        this.description = description;
        this.apiBaseUrl = apiBaseUrl;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.apiOrgId = apiOrgId;
        this.hasApiKey = hasApiKey;
        this.iconUrl = iconUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
    }

    public String getToolTypeDisplay() {
        return toolTypeDisplay;
    }

    public void setToolTypeDisplay(String toolTypeDisplay) {
        this.toolTypeDisplay = toolTypeDisplay;
    }

    public String getToolTypeIcon() {
        return toolTypeIcon;
    }

    public void setToolTypeIcon(String toolTypeIcon) {
        this.toolTypeIcon = toolTypeIcon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiOrgId() {
        return apiOrgId;
    }

    public void setApiOrgId(String apiOrgId) {
        this.apiOrgId = apiOrgId;
    }

    public boolean isHasApiKey() {
        return hasApiKey;
    }

    public void setHasApiKey(boolean hasApiKey) {
        this.hasApiKey = hasApiKey;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String toolType;
        private String toolTypeDisplay;
        private String toolTypeIcon;
        private String description;
        private String apiBaseUrl;
        private boolean enabled;
        private String apiKey;
        private String apiOrgId;
        private boolean hasApiKey;
        private String iconUrl;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder toolType(String toolType) {
            this.toolType = toolType;
            return this;
        }

        public Builder toolTypeDisplay(String toolTypeDisplay) {
            this.toolTypeDisplay = toolTypeDisplay;
            return this;
        }

        public Builder toolTypeIcon(String toolTypeIcon) {
            this.toolTypeIcon = toolTypeIcon;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder apiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiOrgId(String apiOrgId) {
            this.apiOrgId = apiOrgId;
            return this;
        }

        public Builder hasApiKey(boolean hasApiKey) {
            this.hasApiKey = hasApiKey;
            return this;
        }

        public Builder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AIToolResponse build() {
            return new AIToolResponse(id, name, toolType, toolTypeDisplay, toolTypeIcon, description, apiBaseUrl, enabled, apiKey, apiOrgId, hasApiKey, iconUrl, createdAt, updatedAt);
        }
    }
}
