package com.bemobi.aiusercontrol.dto.response;

import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String department;
    private String avatarUrl;
    private String githubUsername;
    private List<UserToolIcon> toolIcons;

    public UserResponse() {
        this.toolIcons = new ArrayList<>();
    }

    public UserResponse(Long id, String email, String name, String department, String avatarUrl,
                        String githubUsername) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.department = department;
        this.avatarUrl = avatarUrl;
        this.githubUsername = githubUsername;
        this.toolIcons = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public List<UserToolIcon> getToolIcons() {
        return toolIcons;
    }

    public void setToolIcons(List<UserToolIcon> toolIcons) {
        this.toolIcons = toolIcons;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String name;
        private String department;
        private String avatarUrl;
        private String githubUsername;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder githubUsername(String githubUsername) {
            this.githubUsername = githubUsername;
            return this;
        }

        public UserResponse build() {
            return new UserResponse(id, email, name, department, avatarUrl, githubUsername);
        }
    }

    public static class UserToolIcon {
        private String toolType;
        private String iconPath;
        private String displayName;

        public UserToolIcon() {
        }

        public UserToolIcon(String toolType, String iconPath, String displayName) {
            this.toolType = toolType;
            this.iconPath = iconPath;
            this.displayName = displayName;
        }

        public String getToolType() {
            return toolType;
        }

        public void setToolType(String toolType) {
            this.toolType = toolType;
        }

        public String getIconPath() {
            return iconPath;
        }

        public void setIconPath(String iconPath) {
            this.iconPath = iconPath;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
