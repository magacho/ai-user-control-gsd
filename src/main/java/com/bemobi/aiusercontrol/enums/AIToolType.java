package com.bemobi.aiusercontrol.enums;

public enum AIToolType {
    CLAUDE("Claude", "/images/claude.svg"),
    GITHUB_COPILOT("Copilot", "/images/copilot.svg"),
    CURSOR("Cursor", "/images/cursor.svg"),
    CUSTOM("Custom", "/images/custom.svg");

    private final String displayName;
    private final String iconPath;

    AIToolType(String displayName, String iconPath) {
        this.displayName = displayName;
        this.iconPath = iconPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconPath() {
        return iconPath;
    }
}
