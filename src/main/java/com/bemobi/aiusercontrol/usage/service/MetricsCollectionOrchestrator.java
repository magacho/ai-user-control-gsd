package com.bemobi.aiusercontrol.usage.service;

import com.bemobi.aiusercontrol.aitool.repository.AIToolRepository;
import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.model.entity.AITool;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsCollectionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollectionOrchestrator.class);

    private final AIToolRepository aiToolRepository;
    private final Map<AIToolType, MetricsCollectionService> collectors;

    @Autowired
    public MetricsCollectionOrchestrator(AIToolRepository aiToolRepository,
                                          List<MetricsCollectionService> collectorList) {
        this.aiToolRepository = aiToolRepository;
        this.collectors = new HashMap<>();
        for (MetricsCollectionService collector : collectorList) {
            this.collectors.put(collector.getToolType(), collector);
        }
        log.info("MetricsCollectionOrchestrator initialized with {} collectors: {}",
                collectors.size(), collectors.keySet());
    }

    @Scheduled(cron = "${app.scheduler.usage-metrics-collection.cron}")
    @SchedulerLock(name = "metricsCollection", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    public void scheduledCollection() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Starting scheduled metrics collection for date: {}", yesterday);
        List<MetricsCollectionResult> results = collectAll(yesterday);
        logResults(results);
    }

    public List<MetricsCollectionResult> collectAll(LocalDate date) {
        List<MetricsCollectionResult> results = new ArrayList<>();
        List<AITool> enabledTools = aiToolRepository.findByEnabled(true);

        if (enabledTools.isEmpty()) {
            log.info("No enabled AI tools found. Skipping metrics collection for date: {}", date);
            return results;
        }

        for (AITool tool : enabledTools) {
            MetricsCollectionService collector = collectors.get(tool.getToolType());
            if (collector == null) {
                log.warn("No metrics collector registered for tool type: {} (tool: {}). Skipping.",
                        tool.getToolType(), tool.getName());
                results.add(MetricsCollectionResult.failure(
                        tool.getToolType(), date,
                        "No collector registered for tool type: " + tool.getToolType()));
                continue;
            }

            try {
                log.info("Collecting metrics for tool: {} (type: {}) for date: {}",
                        tool.getName(), tool.getToolType(), date);
                MetricsCollectionResult result = collector.collectMetrics(tool, date);
                results.add(result);
                log.info("Collection result for {}: success={}, metrics={}, accounts={}",
                        tool.getName(), result.isSuccess(), result.getMetricsCollected(),
                        result.getAccountsProcessed());
            } catch (Exception e) {
                log.error("Unexpected error collecting metrics for tool: {} (type: {})",
                        tool.getName(), tool.getToolType(), e);
                results.add(MetricsCollectionResult.failure(
                        tool.getToolType(), date,
                        "Unexpected error: " + e.getMessage()));
            }
        }

        return results;
    }

    private void logResults(List<MetricsCollectionResult> results) {
        int totalMetrics = 0;
        int totalAccounts = 0;
        int successCount = 0;
        int failureCount = 0;

        for (MetricsCollectionResult result : results) {
            if (result.isSuccess()) {
                successCount++;
                totalMetrics += result.getMetricsCollected();
                totalAccounts += result.getAccountsProcessed();
            } else {
                failureCount++;
            }
        }

        log.info("Metrics collection complete: {} tools succeeded, {} failed, {} total metrics, {} accounts processed",
                successCount, failureCount, totalMetrics, totalAccounts);
    }
}
