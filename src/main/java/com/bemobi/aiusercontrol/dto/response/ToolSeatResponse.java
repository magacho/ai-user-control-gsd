package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ToolSeatResponse {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private Long id;
    private String accountIdentifier;
    private String accountEmail;
    private String status;

    // Linked user data (null if external/unmatched)
    private Long userId;
    private String userName;
    private String userEmail;
    private String userDepartment;
    private String userGithubUsername;
    private String userStatus;

    // All dates from the seat
    private Instant createdAtSource;
    private Instant lastActivityAt;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
    private Instant lastSyncedAt;
    private Instant createdAt;

    // Compliance flags
    private boolean external;
    private boolean inactive;
    private boolean userOffboarded;

    public ToolSeatResponse() {
    }

    private ToolSeatResponse(Builder builder) {
        this.id = builder.id;
        this.accountIdentifier = builder.accountIdentifier;
        this.accountEmail = builder.accountEmail;
        this.status = builder.status;
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.userEmail = builder.userEmail;
        this.userDepartment = builder.userDepartment;
        this.userGithubUsername = builder.userGithubUsername;
        this.userStatus = builder.userStatus;
        this.createdAtSource = builder.createdAtSource;
        this.lastActivityAt = builder.lastActivityAt;
        this.firstSeenAt = builder.firstSeenAt;
        this.lastSeenAt = builder.lastSeenAt;
        this.lastSyncedAt = builder.lastSyncedAt;
        this.createdAt = builder.createdAt;
        this.external = builder.external;
        this.inactive = builder.inactive;
        this.userOffboarded = builder.userOffboarded;
    }

    // Formatted date helpers for Thymeleaf
    public String getCreatedAtSourceFormatted() {
        return createdAtSource != null ? DATE_FORMATTER.format(createdAtSource) : "-";
    }

    public String getLastActivityAtFormatted() {
        return lastActivityAt != null ? DATE_FORMATTER.format(lastActivityAt) : "-";
    }

    public String getFirstSeenAtFormatted() {
        return DATETIME_FORMATTER.format(firstSeenAt) ;
    }

    public String getLastSeenAtFormatted() {
        return lastSeenAt != null ? DATETIME_FORMATTER.format(lastSeenAt) : "-";
    }

    public String getLastSyncedAtFormatted() {
        return lastSyncedAt != null ? DATETIME_FORMATTER.format(lastSyncedAt) : "-";
    }

    public String getCreatedAtFormatted() {
        return createdAt != null ? DATETIME_FORMATTER.format(createdAt) : "-";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserDepartment() {
        return userDepartment;
    }

    public void setUserDepartment(String userDepartment) {
        this.userDepartment = userDepartment;
    }

    public String getUserGithubUsername() {
        return userGithubUsername;
    }

    public void setUserGithubUsername(String userGithubUsername) {
        this.userGithubUsername = userGithubUsername;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public Instant getCreatedAtSource() {
        return createdAtSource;
    }

    public void setCreatedAtSource(Instant createdAtSource) {
        this.createdAtSource = createdAtSource;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public boolean isUserOffboarded() {
        return userOffboarded;
    }

    public void setUserOffboarded(boolean userOffboarded) {
        this.userOffboarded = userOffboarded;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String accountIdentifier;
        private String accountEmail;
        private String status;
        private Long userId;
        private String userName;
        private String userEmail;
        private String userDepartment;
        private String userGithubUsername;
        private String userStatus;
        private Instant createdAtSource;
        private Instant lastActivityAt;
        private Instant firstSeenAt;
        private Instant lastSeenAt;
        private Instant lastSyncedAt;
        private Instant createdAt;
        private boolean external;
        private boolean inactive;
        private boolean userOffboarded;

        public Builder id(Long id) {
            this.id = id;
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

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder userDepartment(String userDepartment) {
            this.userDepartment = userDepartment;
            return this;
        }

        public Builder userGithubUsername(String userGithubUsername) {
            this.userGithubUsername = userGithubUsername;
            return this;
        }

        public Builder userStatus(String userStatus) {
            this.userStatus = userStatus;
            return this;
        }

        public Builder createdAtSource(Instant createdAtSource) {
            this.createdAtSource = createdAtSource;
            return this;
        }

        public Builder lastActivityAt(Instant lastActivityAt) {
            this.lastActivityAt = lastActivityAt;
            return this;
        }

        public Builder firstSeenAt(Instant firstSeenAt) {
            this.firstSeenAt = firstSeenAt;
            return this;
        }

        public Builder lastSeenAt(Instant lastSeenAt) {
            this.lastSeenAt = lastSeenAt;
            return this;
        }

        public Builder lastSyncedAt(Instant lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder external(boolean external) {
            this.external = external;
            return this;
        }

        public Builder inactive(boolean inactive) {
            this.inactive = inactive;
            return this;
        }

        public Builder userOffboarded(boolean userOffboarded) {
            this.userOffboarded = userOffboarded;
            return this;
        }

        public ToolSeatResponse build() {
            return new ToolSeatResponse(this);
        }
    }
}
