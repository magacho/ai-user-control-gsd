package com.bemobi.aiusercontrol.usage.controller;

import com.bemobi.aiusercontrol.usage.service.MetricsCollectionOrchestrator;
import com.bemobi.aiusercontrol.usage.service.MetricsCollectionResult;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/metrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);

    private final MetricsCollectionOrchestrator orchestrator;

    public MetricsController(MetricsCollectionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/collect")
    @HxRequest
    public String collectMetricsHtmx(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate collectionDate = date != null ? date : LocalDate.now().minusDays(1);
        log.info("Admin triggered manual metrics collection for date: {}", collectionDate);

        try {
            List<MetricsCollectionResult> results = orchestrator.collectAll(collectionDate);
            model.addAttribute("collectionDate", collectionDate);
            model.addAttribute("collectionResults", results);
            model.addAttribute("collectionSuccess", results.stream().allMatch(MetricsCollectionResult::isSuccess));

            int totalMetrics = results.stream().mapToInt(MetricsCollectionResult::getMetricsCollected).sum();
            int totalAccounts = results.stream().mapToInt(MetricsCollectionResult::getAccountsProcessed).sum();
            long failureCount = results.stream().filter(r -> !r.isSuccess()).count();

            if (results.isEmpty()) {
                model.addAttribute("collectionMessage",
                        "Nenhuma ferramenta habilitada encontrada. Configure ferramentas AI primeiro.");
                model.addAttribute("collectionSuccess", true);
            } else if (failureCount == 0) {
                model.addAttribute("collectionMessage",
                        String.format("Coleta concluida: %d metricas de %d contas processadas.",
                                totalMetrics, totalAccounts));
                model.addAttribute("collectionSuccess", true);
            } else {
                model.addAttribute("collectionMessage",
                        String.format("Coleta parcial: %d metricas coletadas, %d falhas.",
                                totalMetrics, failureCount));
                model.addAttribute("collectionSuccess", false);
            }
        } catch (Exception e) {
            log.error("Manual metrics collection failed", e);
            model.addAttribute("collectionMessage", "Falha na coleta: " + e.getMessage());
            model.addAttribute("collectionSuccess", false);
        }

        return "fragments/metrics-result :: metricsResult";
    }
}
