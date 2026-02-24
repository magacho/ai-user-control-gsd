package com.bemobi.aiusercontrol.dto.response;

public class PendingAccountResponse {

    private Long id;
    private String toolName;
    private String toolType;
    private String accountIdentifier;
    private String accountEmail;
    private String status;
    private String reason;
    private String userName;
    private String userEmail;

    public PendingAccountResponse() {
    }

    private PendingAccountResponse(Builder builder) {
        this.id = builder.id;
        this.toolName = builder.toolName;
        this.toolType = builder.toolType;
        this.accountIdentifier = builder.accountIdentifier;
        this.accountEmail = builder.accountEmail;
        this.status = builder.status;
        this.reason = builder.reason;
        this.userName = builder.userName;
        this.userEmail = builder.userEmail;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String toolName;
        private String toolType;
        private String accountIdentifier;
        private String accountEmail;
        private String status;
        private String reason;
        private String userName;
        private String userEmail;

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

        public Builder reason(String reason) {
            this.reason = reason;
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

        public PendingAccountResponse build() {
            return new PendingAccountResponse(this);
        }
    }
}
