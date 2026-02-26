package com.bemobi.aiusercontrol.model.entity;

import com.bemobi.aiusercontrol.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String name;

    @Column
    private String department;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "github_username")
    private String githubUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "validation_source")
    private String validationSource = "GWS_LEGACY";

    @Column(name = "gws_validated_at")
    private Instant gwsValidatedAt;

    public User() {
    }

    public User(Long id, String email, String name, String department, String avatarUrl,
                String githubUsername, UserStatus status, Instant lastLoginAt, Instant createdAt,
                Instant updatedAt, String validationSource, Instant gwsValidatedAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.department = department;
        this.avatarUrl = avatarUrl;
        this.githubUsername = githubUsername;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.validationSource = validationSource;
        this.gwsValidatedAt = gwsValidatedAt;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getValidationSource() {
        return validationSource;
    }

    public void setValidationSource(String validationSource) {
        this.validationSource = validationSource;
    }

    public Instant getGwsValidatedAt() {
        return gwsValidatedAt;
    }

    public void setGwsValidatedAt(Instant gwsValidatedAt) {
        this.gwsValidatedAt = gwsValidatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", githubUsername='" + githubUsername + '\'' +
                ", status=" + status +
                ", lastLoginAt=" + lastLoginAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", validationSource='" + validationSource + '\'' +
                ", gwsValidatedAt=" + gwsValidatedAt +
                '}';
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
        private UserStatus status = UserStatus.ACTIVE;
        private Instant lastLoginAt;
        private Instant createdAt;
        private Instant updatedAt;
        private String validationSource = "GWS_LEGACY";
        private Instant gwsValidatedAt;

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

        public Builder status(UserStatus status) {
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

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder validationSource(String validationSource) {
            this.validationSource = validationSource;
            return this;
        }

        public Builder gwsValidatedAt(Instant gwsValidatedAt) {
            this.gwsValidatedAt = gwsValidatedAt;
            return this;
        }

        public User build() {
            return new User(id, email, name, department, avatarUrl, githubUsername, status, lastLoginAt, createdAt, updatedAt, validationSource, gwsValidatedAt);
        }
    }
}
