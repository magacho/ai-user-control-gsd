package com.bemobi.aiusercontrol.model.entity;

import com.bemobi.aiusercontrol.enums.AIToolType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "ai_tools")
public class AITool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tool_type", nullable = false)
    private AIToolType toolType;

    @Column(length = 1024)
    private String description;

    @Column(name = "api_base_url", length = 1024)
    private String apiBaseUrl;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "api_key", length = 1024)
    private String apiKey;

    @Column(name = "api_org_id")
    private String apiOrgId;

    @Column(name = "icon_url", length = 1024)
    private String iconUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AITool() {
    }

    public AITool(Long id, String name, AIToolType toolType, String description, String apiBaseUrl,
                  boolean enabled, String apiKey, String apiOrgId, String iconUrl,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.toolType = toolType;
        this.description = description;
        this.apiBaseUrl = apiBaseUrl;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.apiOrgId = apiOrgId;
        this.iconUrl = iconUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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

    public AIToolType getToolType() {
        return toolType;
    }

    public void setToolType(AIToolType toolType) {
        this.toolType = toolType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AITool aiTool = (AITool) o;
        return Objects.equals(id, aiTool.id) && Objects.equals(name, aiTool.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "AITool{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", toolType=" + toolType +
                ", description='" + description + '\'' +
                ", apiBaseUrl='" + apiBaseUrl + '\'' +
                ", enabled=" + enabled +
                ", apiOrgId='" + apiOrgId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private AIToolType toolType;
        private String description;
        private String apiBaseUrl;
        private boolean enabled = true;
        private String apiKey;
        private String apiOrgId;
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

        public Builder toolType(AIToolType toolType) {
            this.toolType = toolType;
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

        public AITool build() {
            return new AITool(id, name, toolType, description, apiBaseUrl, enabled, apiKey, apiOrgId, iconUrl, createdAt, updatedAt);
        }
    }
}
