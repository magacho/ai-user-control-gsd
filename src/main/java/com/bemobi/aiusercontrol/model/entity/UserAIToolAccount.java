package com.bemobi.aiusercontrol.model.entity;

import com.bemobi.aiusercontrol.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "user_ai_tool_accounts")
public class UserAIToolAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_tool_id", nullable = false)
    private AITool aiTool;

    @Column(name = "account_identifier", nullable = false)
    private String accountIdentifier;

    @Column(name = "account_email")
    private String accountEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserAIToolAccount() {
    }

    public UserAIToolAccount(Long id, User user, AITool aiTool, String accountIdentifier, String accountEmail,
                             AccountStatus status, Instant lastSyncedAt, Instant firstSeenAt, Instant lastSeenAt,
                             Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.aiTool = aiTool;
        this.accountIdentifier = accountIdentifier;
        this.accountEmail = accountEmail;
        this.status = status;
        this.lastSyncedAt = lastSyncedAt;
        this.firstSeenAt = firstSeenAt;
        this.lastSeenAt = lastSeenAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.firstSeenAt == null) {
            this.firstSeenAt = now;
        }
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AITool getAiTool() {
        return aiTool;
    }

    public void setAiTool(AITool aiTool) {
        this.aiTool = aiTool;
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

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Instant lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
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
        UserAIToolAccount that = (UserAIToolAccount) o;
        return Objects.equals(id, that.id) && Objects.equals(accountIdentifier, that.accountIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, accountIdentifier);
    }

    @Override
    public String toString() {
        return "UserAIToolAccount{" +
                "id=" + id +
                ", accountIdentifier='" + accountIdentifier + '\'' +
                ", accountEmail='" + accountEmail + '\'' +
                ", status=" + status +
                ", lastSyncedAt=" + lastSyncedAt +
                ", firstSeenAt=" + firstSeenAt +
                ", lastSeenAt=" + lastSeenAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User user;
        private AITool aiTool;
        private String accountIdentifier;
        private String accountEmail;
        private AccountStatus status = AccountStatus.ACTIVE;
        private Instant lastSyncedAt;
        private Instant firstSeenAt;
        private Instant lastSeenAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder aiTool(AITool aiTool) {
            this.aiTool = aiTool;
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

        public Builder status(AccountStatus status) {
            this.status = status;
            return this;
        }

        public Builder lastSyncedAt(Instant lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserAIToolAccount build() {
            return new UserAIToolAccount(id, user, aiTool, accountIdentifier, accountEmail,
                    status, lastSyncedAt, firstSeenAt, lastSeenAt, createdAt, updatedAt);
        }
    }
}
