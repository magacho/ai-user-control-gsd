package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.util.Objects;

public class ToolAccountInfo {

    private final String identifier;
    private final String email;
    private final Instant createdAtSource;
    private final Instant lastActivityAt;

    public ToolAccountInfo(String identifier, String email) {
        this(identifier, email, null, null);
    }

    public ToolAccountInfo(String identifier, String email, Instant createdAtSource, Instant lastActivityAt) {
        this.identifier = identifier;
        this.email = email;
        this.createdAtSource = createdAtSource;
        this.lastActivityAt = lastActivityAt;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAtSource() {
        return createdAtSource;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolAccountInfo that = (ToolAccountInfo) o;
        return Objects.equals(identifier, that.identifier)
                && Objects.equals(email, that.email)
                && Objects.equals(createdAtSource, that.createdAtSource)
                && Objects.equals(lastActivityAt, that.lastActivityAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, email, createdAtSource, lastActivityAt);
    }

    @Override
    public String toString() {
        return "ToolAccountInfo{" +
                "identifier='" + identifier + '\'' +
                ", email='" + email + '\'' +
                ", createdAtSource=" + createdAtSource +
                ", lastActivityAt=" + lastActivityAt +
                '}';
    }
}
