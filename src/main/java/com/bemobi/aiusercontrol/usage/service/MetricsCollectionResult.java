package com.bemobi.aiusercontrol.usage.service;

import com.bemobi.aiusercontrol.enums.AIToolType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MetricsCollectionResult {

    private AIToolType toolType;
    private LocalDate date;
    private int metricsCollected;
    private int accountsProcessed;
    private boolean success;
    private List<String> errors;

    public MetricsCollectionResult() {
        this.errors = new ArrayList<>();
    }

    public MetricsCollectionResult(AIToolType toolType, LocalDate date, int metricsCollected,
                                   int accountsProcessed, boolean success, List<String> errors) {
        this.toolType = toolType;
        this.date = date;
        this.metricsCollected = metricsCollected;
        this.accountsProcessed = accountsProcessed;
        this.success = success;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public static MetricsCollectionResult success(AIToolType toolType, LocalDate date,
                                                   int metricsCollected, int accountsProcessed) {
        return new MetricsCollectionResult(toolType, date, metricsCollected, accountsProcessed, true, new ArrayList<>());
    }

    public static MetricsCollectionResult failure(AIToolType toolType, LocalDate date, String error) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        return new MetricsCollectionResult(toolType, date, 0, 0, false, errors);
    }

    public AIToolType getToolType() {
        return toolType;
    }

    public void setToolType(AIToolType toolType) {
        this.toolType = toolType;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getMetricsCollected() {
        return metricsCollected;
    }

    public void setMetricsCollected(int metricsCollected) {
        this.metricsCollected = metricsCollected;
    }

    public int getAccountsProcessed() {
        return accountsProcessed;
    }

    public void setAccountsProcessed(int accountsProcessed) {
        this.accountsProcessed = accountsProcessed;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    @Override
    public String toString() {
        return "MetricsCollectionResult{" +
                "toolType=" + toolType +
                ", date=" + date +
                ", metricsCollected=" + metricsCollected +
                ", accountsProcessed=" + accountsProcessed +
                ", success=" + success +
                ", errors=" + errors +
                '}';
    }
}
