package com.bemobi.aiusercontrol.enums;

public enum AIToolType {
    CLAUDE("Claude"),
    GITHUB_COPILOT("Copilot"),
    CURSOR("Cursor"),
    CUSTOM("Custom");

    private final String displayName;

    AIToolType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
