package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserResponse {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault());

    private Long id;
    private String email;
    private String name;
    private String department;
    private String avatarUrl;
    private String githubUsername;
    private String status;
    private Instant lastLoginAt;
    private Instant createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String email, String name, String department, String avatarUrl,
                        String githubUsername, String status, Instant lastLoginAt, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.department = department;
        this.avatarUrl = avatarUrl;
        this.githubUsername = githubUsername;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }

    public String getLastLoginFormatted() {
        return lastLoginAt != null ? DISPLAY_FORMAT.format(lastLoginAt) : "Never";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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
        private String status;
        private Instant lastLoginAt;
        private Instant createdAt;

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

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder lastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserResponse build() {
            return new UserResponse(id, email, name, department, avatarUrl, githubUsername, status, lastLoginAt, createdAt);
        }
    }
}
