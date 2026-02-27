package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserAccountResponse {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm").withZone(ZoneId.systemDefault());

    private Long id;
    private String toolName;
    private String toolType;
    private String toolTypeIcon;
    private String accountIdentifier;
    private String accountEmail;
    private String status;
    private String statusLabel;
    private Instant lastSeenAt;
    private Instant firstSeenAt;

    public UserAccountResponse() {
    }

    private UserAccountResponse(Builder builder) {
        this.id = builder.id;
        this.toolName = builder.toolName;
        this.toolType = builder.toolType;
        this.toolTypeIcon = builder.toolTypeIcon;
        this.accountIdentifier = builder.accountIdentifier;
        this.accountEmail = builder.accountEmail;
        this.status = builder.status;
        this.statusLabel = builder.statusLabel;
        this.lastSeenAt = builder.lastSeenAt;
        this.firstSeenAt = builder.firstSeenAt;
    }

    public String getLastSeenFormatted() {
        return lastSeenAt != null ? DISPLAY_FORMAT.format(lastSeenAt) : "-";
    }

    public String getFirstSeenFormatted() {
        return firstSeenAt != null ? DISPLAY_FORMAT.format(firstSeenAt) : "-";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
    }

    public String getToolTypeIcon() {
        return toolTypeIcon;
    }

    public void setToolTypeIcon(String toolTypeIcon) {
        this.toolTypeIcon = toolTypeIcon;
    }

    public String getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(String accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
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

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String toolName;
        private String toolType;
        private String toolTypeIcon;
        private String accountIdentifier;
        private String accountEmail;
        private String status;
        private String statusLabel;
        private Instant lastSeenAt;
        private Instant firstSeenAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder toolName(String toolName) {
            this.toolName = toolName;
            return this;
        }

        public Builder toolType(String toolType) {
            this.toolType = toolType;
            return this;
        }

        public Builder toolTypeIcon(String toolTypeIcon) {
            this.toolTypeIcon = toolTypeIcon;
            return this;
        }

        public Builder accountIdentifier(String accountIdentifier) {
            this.accountIdentifier = accountIdentifier;
            return this;
        }

        public Builder accountEmail(String accountEmail) {
            this.accountEmail = accountEmail;
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

        public Builder lastSeenAt(Instant lastSeenAt) {
            this.lastSeenAt = lastSeenAt;
            return this;
        }

        public Builder firstSeenAt(Instant firstSeenAt) {
            this.firstSeenAt = firstSeenAt;
            return this;
        }

        public UserAccountResponse build() {
            return new UserAccountResponse(this);
        }
    }
}
