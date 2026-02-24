package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UserDetailResponse {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault());

    private Long id;
    private String email;
    private String name;
    private String department;
    private String avatarUrl;
    private String githubUsername;
    private String status;
    private String statusLabel;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<UserAccountResponse> accounts;

    public UserDetailResponse() {
        this.accounts = new ArrayList<>();
    }

    private UserDetailResponse(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.name = builder.name;
        this.department = builder.department;
        this.avatarUrl = builder.avatarUrl;
        this.githubUsername = builder.githubUsername;
        this.status = builder.status;
        this.statusLabel = builder.statusLabel;
        this.lastLoginAt = builder.lastLoginAt;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.accounts = builder.accounts != null ? builder.accounts : new ArrayList<>();
    }

    public String getLastLoginFormatted() {
        return lastLoginAt != null ? DISPLAY_FORMAT.format(lastLoginAt) : "Never";
    }

    public String getCreatedAtFormatted() {
        return createdAt != null ? DISPLAY_FORMAT.format(createdAt) : "-";
    }

    public String getUpdatedAtFormatted() {
        return updatedAt != null ? DISPLAY_FORMAT.format(updatedAt) : "-";
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

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<UserAccountResponse> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<UserAccountResponse> accounts) {
        this.accounts = accounts;
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
        private String statusLabel;
        private Instant lastLoginAt;
        private Instant createdAt;
        private Instant updatedAt;
        private List<UserAccountResponse> accounts;

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

        public Builder statusLabel(String statusLabel) {
            this.statusLabel = statusLabel;
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

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder accounts(List<UserAccountResponse> accounts) {
            this.accounts = accounts;
            return this;
        }

        public UserDetailResponse build() {
            return new UserDetailResponse(this);
        }
    }
}
