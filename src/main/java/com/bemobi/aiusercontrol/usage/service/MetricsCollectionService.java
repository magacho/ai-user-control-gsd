package com.bemobi.aiusercontrol.usage.service;

import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.model.entity.AITool;

import java.time.LocalDate;

/**
 * Interface for provider-specific metrics collection implementations.
 * Each AI tool provider (Cursor, Claude, GitHub) implements this to collect
 * usage metrics from their respective APIs.
 */
public interface MetricsCollectionService {

    /**
     * Collect metrics for a specific AI tool on a given date.
     *
     * @param tool the AI tool to collect metrics for
     * @param date the date to collect metrics for
     * @return result summary of the collection
     */
    MetricsCollectionResult collectMetrics(AITool tool, LocalDate date);

    /**
     * Returns the AI tool type this collector handles.
     */
    AIToolType getToolType();
}
