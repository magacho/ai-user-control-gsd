package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SyncResultResponse {

    private int newUsers;
    private int updatedUsers;
    private int offboardedUsers;
    private int linkedAccounts;
    private int unmatchedAccounts;
    private int suspendedAccounts;
    private int revokedAccounts;
    private List<String> errors;
    private Instant syncedAt;

    public SyncResultResponse() {
        this.errors = new ArrayList<>();
        this.syncedAt = Instant.now();
    }

    private SyncResultResponse(Builder builder) {
        this.newUsers = builder.newUsers;
        this.updatedUsers = builder.updatedUsers;
        this.offboardedUsers = builder.offboardedUsers;
        this.linkedAccounts = builder.linkedAccounts;
        this.unmatchedAccounts = builder.unmatchedAccounts;
        this.suspendedAccounts = builder.suspendedAccounts;
        this.revokedAccounts = builder.revokedAccounts;
        this.errors = builder.errors;
        this.syncedAt = builder.syncedAt;
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

    public int getOffboardedUsers() {
        return offboardedUsers;
    }

    public void setOffboardedUsers(int offboardedUsers) {
        this.offboardedUsers = offboardedUsers;
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
                && offboardedUsers == that.offboardedUsers
                && linkedAccounts == that.linkedAccounts
                && unmatchedAccounts == that.unmatchedAccounts
                && suspendedAccounts == that.suspendedAccounts
                && revokedAccounts == that.revokedAccounts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(newUsers, updatedUsers, offboardedUsers, linkedAccounts,
                unmatchedAccounts, suspendedAccounts, revokedAccounts);
    }

    @Override
    public String toString() {
        return "SyncResultResponse{" +
                "newUsers=" + newUsers +
                ", updatedUsers=" + updatedUsers +
                ", offboardedUsers=" + offboardedUsers +
                ", linkedAccounts=" + linkedAccounts +
                ", unmatchedAccounts=" + unmatchedAccounts +
                ", suspendedAccounts=" + suspendedAccounts +
                ", revokedAccounts=" + revokedAccounts +
                ", errors=" + errors +
                ", syncedAt=" + syncedAt +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int newUsers;
        private int updatedUsers;
        private int offboardedUsers;
        private int linkedAccounts;
        private int unmatchedAccounts;
        private int suspendedAccounts;
        private int revokedAccounts;
        private List<String> errors = new ArrayList<>();
        private Instant syncedAt = Instant.now();

        public Builder newUsers(int newUsers) {
            this.newUsers = newUsers;
            return this;
        }

        public Builder updatedUsers(int updatedUsers) {
            this.updatedUsers = updatedUsers;
            return this;
        }

        public Builder offboardedUsers(int offboardedUsers) {
            this.offboardedUsers = offboardedUsers;
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

        public SyncResultResponse build() {
            return new SyncResultResponse(this);
        }
    }
}
