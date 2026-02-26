package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SyncResultResponse {

    private int newUsers;
    private int updatedUsers;
    private int linkedAccounts;
    private int unmatchedAccounts;
    private int suspendedAccounts;
    private int revokedAccounts;
    private int externalAccounts;
    private int archivedLegacyUsers;
    private int gwsValidatedUsers;
    private List<String> errors;
    private Instant syncedAt;
    private Map<String, ToolSyncDetail> toolDetails;

    public SyncResultResponse() {
        this.errors = new ArrayList<>();
        this.syncedAt = Instant.now();
        this.toolDetails = new LinkedHashMap<>();
    }

    private SyncResultResponse(Builder builder) {
        this.newUsers = builder.newUsers;
        this.updatedUsers = builder.updatedUsers;
        this.linkedAccounts = builder.linkedAccounts;
        this.unmatchedAccounts = builder.unmatchedAccounts;
        this.suspendedAccounts = builder.suspendedAccounts;
        this.revokedAccounts = builder.revokedAccounts;
        this.externalAccounts = builder.externalAccounts;
        this.archivedLegacyUsers = builder.archivedLegacyUsers;
        this.gwsValidatedUsers = builder.gwsValidatedUsers;
        this.errors = builder.errors;
        this.syncedAt = builder.syncedAt;
        this.toolDetails = new LinkedHashMap<>(builder.toolDetails);
    }

    public int getNewUsers() {
        return newUsers;
    }

    public void setNewUsers(int newUsers) {
        this.newUsers = newUsers;
    }

    public int getUpdatedUsers() {
        return updatedUsers;
    }

    public void setUpdatedUsers(int updatedUsers) {
        this.updatedUsers = updatedUsers;
    }

    public int getLinkedAccounts() {
        return linkedAccounts;
    }

    public void setLinkedAccounts(int linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }

    public int getUnmatchedAccounts() {
        return unmatchedAccounts;
    }

    public void setUnmatchedAccounts(int unmatchedAccounts) {
        this.unmatchedAccounts = unmatchedAccounts;
    }

    public int getSuspendedAccounts() {
        return suspendedAccounts;
    }

    public void setSuspendedAccounts(int suspendedAccounts) {
        this.suspendedAccounts = suspendedAccounts;
    }

    public int getRevokedAccounts() {
        return revokedAccounts;
    }

    public void setRevokedAccounts(int revokedAccounts) {
        this.revokedAccounts = revokedAccounts;
    }

    public int getExternalAccounts() {
        return externalAccounts;
    }

    public void setExternalAccounts(int externalAccounts) {
        this.externalAccounts = externalAccounts;
    }

    public int getArchivedLegacyUsers() {
        return archivedLegacyUsers;
    }

    public void setArchivedLegacyUsers(int archivedLegacyUsers) {
        this.archivedLegacyUsers = archivedLegacyUsers;
    }

    public int getGwsValidatedUsers() {
        return gwsValidatedUsers;
    }

    public void setGwsValidatedUsers(int gwsValidatedUsers) {
        this.gwsValidatedUsers = gwsValidatedUsers;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Instant syncedAt) {
        this.syncedAt = syncedAt;
    }

    public Map<String, ToolSyncDetail> getToolDetails() {
        return toolDetails;
    }

    public void setToolDetails(Map<String, ToolSyncDetail> toolDetails) {
        this.toolDetails = toolDetails;
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncResultResponse that = (SyncResultResponse) o;
        return newUsers == that.newUsers
                && updatedUsers == that.updatedUsers
                && linkedAccounts == that.linkedAccounts
                && unmatchedAccounts == that.unmatchedAccounts
                && suspendedAccounts == that.suspendedAccounts
                && revokedAccounts == that.revokedAccounts
                && externalAccounts == that.externalAccounts
                && archivedLegacyUsers == that.archivedLegacyUsers
                && gwsValidatedUsers == that.gwsValidatedUsers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newUsers, updatedUsers, linkedAccounts,
                unmatchedAccounts, suspendedAccounts, revokedAccounts,
                externalAccounts, archivedLegacyUsers, gwsValidatedUsers);
    }

    @Override
    public String toString() {
        return "SyncResultResponse{" +
                "newUsers=" + newUsers +
                ", updatedUsers=" + updatedUsers +
                ", linkedAccounts=" + linkedAccounts +
                ", unmatchedAccounts=" + unmatchedAccounts +
                ", suspendedAccounts=" + suspendedAccounts +
                ", revokedAccounts=" + revokedAccounts +
                ", externalAccounts=" + externalAccounts +
                ", archivedLegacyUsers=" + archivedLegacyUsers +
                ", gwsValidatedUsers=" + gwsValidatedUsers +
                ", errors=" + errors +
                ", syncedAt=" + syncedAt +
                ", toolDetails=" + toolDetails +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int newUsers;
        private int updatedUsers;
        private int linkedAccounts;
        private int unmatchedAccounts;
        private int suspendedAccounts;
        private int revokedAccounts;
        private int externalAccounts;
        private int archivedLegacyUsers;
        private int gwsValidatedUsers;
        private List<String> errors = new ArrayList<>();
        private Instant syncedAt = Instant.now();
        private Map<String, ToolSyncDetail> toolDetails = new LinkedHashMap<>();

        public Builder newUsers(int newUsers) {
            this.newUsers = newUsers;
            return this;
        }

        public Builder updatedUsers(int updatedUsers) {
            this.updatedUsers = updatedUsers;
            return this;
        }

        public Builder linkedAccounts(int linkedAccounts) {
            this.linkedAccounts = linkedAccounts;
            return this;
        }

        public Builder unmatchedAccounts(int unmatchedAccounts) {
            this.unmatchedAccounts = unmatchedAccounts;
            return this;
        }

        public Builder suspendedAccounts(int suspendedAccounts) {
            this.suspendedAccounts = suspendedAccounts;
            return this;
        }

        public Builder revokedAccounts(int revokedAccounts) {
            this.revokedAccounts = revokedAccounts;
            return this;
        }

        public Builder externalAccounts(int externalAccounts) {
            this.externalAccounts = externalAccounts;
            return this;
        }

        public Builder archivedLegacyUsers(int archivedLegacyUsers) {
            this.archivedLegacyUsers = archivedLegacyUsers;
            return this;
        }

        public Builder gwsValidatedUsers(int gwsValidatedUsers) {
            this.gwsValidatedUsers = gwsValidatedUsers;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder addError(String error) {
            this.errors.add(error);
            return this;
        }

        public Builder syncedAt(Instant syncedAt) {
            this.syncedAt = syncedAt;
            return this;
        }

        public Builder addToolDetail(String toolName, ToolSyncDetail detail) {
            this.toolDetails.put(toolName, detail);
            return this;
        }

        public ToolSyncDetail getToolDetail(String toolName) {
            return this.toolDetails.get(toolName);
        }

        public SyncResultResponse build() {
            return new SyncResultResponse(this);
        }
    }

    public static class ToolSyncDetail {
        private String toolName;
        private int seatsFound;
        private int linked;
        private int unmatched;
        private int suspended;
        private int revoked;
        private String error; // null if successful

        public ToolSyncDetail() {
        }

        public ToolSyncDetail(String toolName, int seatsFound, int linked, int unmatched,
                              int suspended, int revoked, String error) {
            this.toolName = toolName;
            this.seatsFound = seatsFound;
            this.linked = linked;
            this.unmatched = unmatched;
            this.suspended = suspended;
            this.revoked = revoked;
            this.error = error;
        }

        public String getToolName() {
            return toolName;
        }

        public void setToolName(String toolName) {
            this.toolName = toolName;
        }

        public int getSeatsFound() {
            return seatsFound;
        }

        public void setSeatsFound(int seatsFound) {
            this.seatsFound = seatsFound;
        }

        public int getLinked() {
            return linked;
        }

        public void setLinked(int linked) {
            this.linked = linked;
        }

        public int getUnmatched() {
            return unmatched;
        }

        public void setUnmatched(int unmatched) {
            this.unmatched = unmatched;
        }

        public int getSuspended() {
            return suspended;
        }

        public void setSuspended(int suspended) {
            this.suspended = suspended;
        }

        public int getRevoked() {
            return revoked;
        }

        public void setRevoked(int revoked) {
            this.revoked = revoked;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        @Override
        public String toString() {
            return "ToolSyncDetail{" +
                    "toolName='" + toolName + '\'' +
                    ", seatsFound=" + seatsFound +
                    ", linked=" + linked +
                    ", unmatched=" + unmatched +
                    ", suspended=" + suspended +
                    ", revoked=" + revoked +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}
