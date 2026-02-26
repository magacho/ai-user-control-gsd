package com.bemobi.aiusercontrol.web;

import com.bemobi.aiusercontrol.dto.response.SyncResultResponse;
import com.bemobi.aiusercontrol.service.SyncOrchestrator;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final SyncOrchestrator syncOrchestrator;

    public SyncController(SyncOrchestrator syncOrchestrator) {
        this.syncOrchestrator = syncOrchestrator;
    }

    @PostMapping("/sync")
    @HxRequest
    public String syncHtmx(Model model) {
        try {
            SyncResultResponse result = syncOrchestrator.executeFullSync();
            model.addAttribute("syncResult", result);
            model.addAttribute("syncSuccess", result.getErrors() == null || result.getErrors().isEmpty());
        } catch (Exception e) {
            log.error("Full sync failed with unexpected error", e);
            SyncResultResponse errorResult = new SyncResultResponse();
            errorResult.addError("Sync failed: " + e.getMessage());
            model.addAttribute("syncResult", errorResult);
            model.addAttribute("syncSuccess", false);
        }
        return "fragments/sync-result :: syncResult";
    }

    @PostMapping("/sync")
    public String syncFallback(RedirectAttributes redirectAttributes) {
        try {
            SyncResultResponse result = syncOrchestrator.executeFullSync();
            if (result.getErrors() != null && !result.getErrors().isEmpty()) {
                redirectAttributes.addFlashAttribute("syncError", String.join("; ", result.getErrors()));
            }
            redirectAttributes.addFlashAttribute("syncMessage",
                    String.format("Sync completo: %d novos, %d atualizados, %d validados GWS. Contas: %d vinculadas, %d sem correspondencia, %d externas.",
                            result.getNewUsers(), result.getUpdatedUsers(), result.getGwsValidatedUsers(),
                            result.getLinkedAccounts(), result.getUnmatchedAccounts(), result.getExternalAccounts()));
        } catch (Exception e) {
            log.error("Full sync failed with unexpected error", e);
            redirectAttributes.addFlashAttribute("syncError", "Sync failed: " + e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
