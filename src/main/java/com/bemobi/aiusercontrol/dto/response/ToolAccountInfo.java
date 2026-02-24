package com.bemobi.aiusercontrol.dto.response;

import java.util.Objects;

public class ToolAccountInfo {

    private final String identifier;
    private final String email;

    public ToolAccountInfo(String identifier, String email) {
        this.identifier = identifier;
        this.email = email;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ToolAccountInfo that = (ToolAccountInfo) o;
        return Objects.equals(identifier, that.identifier) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, email);
    }

    @Override
    public String toString() {
        return "ToolAccountInfo{" +
                "identifier='" + identifier + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
