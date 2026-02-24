package com.bemobi.aiusercontrol.model.entity;

import com.bemobi.aiusercontrol.enums.UsageMetricType;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "usage_metrics")
public class UsageMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_ai_tool_account_id", nullable = false)
    private UserAIToolAccount account;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private UsageMetricType metricType;

    @Column(nullable = false, precision = 20, scale = 4)
    private BigDecimal value = BigDecimal.ZERO;

    @Column(name = "raw_data", columnDefinition = "jsonb")
    private String rawData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UsageMetric() {
    }

    public UsageMetric(Long id, UserAIToolAccount account, LocalDate metricDate, UsageMetricType metricType,
                       BigDecimal value, String rawData, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.account = account;
        this.metricDate = metricDate;
        this.metricType = metricType;
        this.value = value;
        this.rawData = rawData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UserAIToolAccount getAccount() {
        return account;
    }

    public void setAccount(UserAIToolAccount account) {
        this.account = account;
    }

    public LocalDate getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(LocalDate metricDate) {
        this.metricDate = metricDate;
    }

    public UsageMetricType getMetricType() {
        return metricType;
    }

    public void setMetricType(UsageMetricType metricType) {
        this.metricType = metricType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
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
        UsageMetric that = (UsageMetric) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UsageMetric{" +
                "id=" + id +
                ", metricDate=" + metricDate +
                ", metricType=" + metricType +
                ", value=" + value +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private UserAIToolAccount account;
        private LocalDate metricDate;
        private UsageMetricType metricType;
        private BigDecimal value = BigDecimal.ZERO;
        private String rawData;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder account(UserAIToolAccount account) {
            this.account = account;
            return this;
        }

        public Builder metricDate(LocalDate metricDate) {
            this.metricDate = metricDate;
            return this;
        }

        public Builder metricType(UsageMetricType metricType) {
            this.metricType = metricType;
            return this;
        }

        public Builder value(BigDecimal value) {
            this.value = value;
            return this;
        }

        public Builder rawData(String rawData) {
            this.rawData = rawData;
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

        public UsageMetric build() {
            return new UsageMetric(id, account, metricDate, metricType, value, rawData, createdAt, updatedAt);
        }
    }
}
