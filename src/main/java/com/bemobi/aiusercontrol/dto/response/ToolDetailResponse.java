package com.bemobi.aiusercontrol.dto.response;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ToolDetailResponse {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private Long id;
    private String name;
    private String toolType;
    private String toolTypeDisplay;
    private String toolTypeIcon;
    private String description;
    private boolean enabled;
    private Instant createdAt;

    // Stats
    private long totalSeats;
    private long activeSeats;
    private long suspendedSeats;
    private long revokedSeats;
    private long externalSeats;
    private long inactiveSeats;
    private long offboardedUserSeats;

    private List<ToolSeatResponse> seats;

    public ToolDetailResponse() {
        this.seats = new ArrayList<>();
    }

    private ToolDetailResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.toolType = builder.toolType;
        this.toolTypeDisplay = builder.toolTypeDisplay;
        this.toolTypeIcon = builder.toolTypeIcon;
        this.description = builder.description;
        this.enabled = builder.enabled;
        this.createdAt = builder.createdAt;
        this.totalSeats = builder.totalSeats;
        this.activeSeats = builder.activeSeats;
        this.suspendedSeats = builder.suspendedSeats;
        this.revokedSeats = builder.revokedSeats;
        this.externalSeats = builder.externalSeats;
        this.inactiveSeats = builder.inactiveSeats;
        this.offboardedUserSeats = builder.offboardedUserSeats;
        this.seats = builder.seats != null ? builder.seats : new ArrayList<>();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToolType() {
        return toolType;
    }

    public void setToolType(String toolType) {
        this.toolType = toolType;
    }

    public String getToolTypeDisplay() {
        return toolTypeDisplay;
    }

    public void setToolTypeDisplay(String toolTypeDisplay) {
        this.toolTypeDisplay = toolTypeDisplay;
    }

    public String getToolTypeIcon() {
        return toolTypeIcon;
    }

    public void setToolTypeIcon(String toolTypeIcon) {
        this.toolTypeIcon = toolTypeIcon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }

    public long getActiveSeats() {
        return activeSeats;
    }

    public void setActiveSeats(long activeSeats) {
        this.activeSeats = activeSeats;
    }

    public long getSuspendedSeats() {
        return suspendedSeats;
    }

    public void setSuspendedSeats(long suspendedSeats) {
        this.suspendedSeats = suspendedSeats;
    }

    public long getRevokedSeats() {
        return revokedSeats;
    }

    public void setRevokedSeats(long revokedSeats) {
        this.revokedSeats = revokedSeats;
    }

    public long getExternalSeats() {
        return externalSeats;
    }

    public void setExternalSeats(long externalSeats) {
        this.externalSeats = externalSeats;
    }

    public long getInactiveSeats() {
        return inactiveSeats;
    }

    public void setInactiveSeats(long inactiveSeats) {
        this.inactiveSeats = inactiveSeats;
    }

    public long getOffboardedUserSeats() {
        return offboardedUserSeats;
    }

    public void setOffboardedUserSeats(long offboardedUserSeats) {
        this.offboardedUserSeats = offboardedUserSeats;
    }

    public List<ToolSeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<ToolSeatResponse> seats) {
        this.seats = seats;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String toolType;
        private String toolTypeDisplay;
        private String toolTypeIcon;
        private String description;
        private boolean enabled;
        private Instant createdAt;
        private long totalSeats;
        private long activeSeats;
        private long suspendedSeats;
        private long revokedSeats;
        private long externalSeats;
        private long inactiveSeats;
        private long offboardedUserSeats;
        private List<ToolSeatResponse> seats;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder toolType(String toolType) {
            this.toolType = toolType;
            return this;
        }

        public Builder toolTypeDisplay(String toolTypeDisplay) {
            this.toolTypeDisplay = toolTypeDisplay;
            return this;
        }

        public Builder toolTypeIcon(String toolTypeIcon) {
            this.toolTypeIcon = toolTypeIcon;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder totalSeats(long totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public Builder activeSeats(long activeSeats) {
            this.activeSeats = activeSeats;
            return this;
        }

        public Builder suspendedSeats(long suspendedSeats) {
            this.suspendedSeats = suspendedSeats;
            return this;
        }

        public Builder revokedSeats(long revokedSeats) {
            this.revokedSeats = revokedSeats;
            return this;
        }

        public Builder externalSeats(long externalSeats) {
            this.externalSeats = externalSeats;
            return this;
        }

        public Builder inactiveSeats(long inactiveSeats) {
            this.inactiveSeats = inactiveSeats;
            return this;
        }

        public Builder offboardedUserSeats(long offboardedUserSeats) {
            this.offboardedUserSeats = offboardedUserSeats;
            return this;
        }

        public Builder seats(List<ToolSeatResponse> seats) {
            this.seats = seats;
            return this;
        }

        public ToolDetailResponse build() {
            return new ToolDetailResponse(this);
        }
    }
}
