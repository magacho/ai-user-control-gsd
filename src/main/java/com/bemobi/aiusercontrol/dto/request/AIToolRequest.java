package com.bemobi.aiusercontrol.dto.request;

import com.bemobi.aiusercontrol.enums.AIToolType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AIToolRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Tool type is required")
    private AIToolType toolType;

    private String description;

    private String apiBaseUrl;

    private boolean enabled = true;

    private String iconUrl;

    public AIToolRequest() {
    }

    public AIToolRequest(String name, AIToolType toolType, String description, String apiBaseUrl,
                         boolean enabled, String iconUrl) {
        this.name = name;
        this.toolType = toolType;
        this.description = description;
        this.apiBaseUrl = apiBaseUrl;
        this.enabled = enabled;
        this.iconUrl = iconUrl;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
