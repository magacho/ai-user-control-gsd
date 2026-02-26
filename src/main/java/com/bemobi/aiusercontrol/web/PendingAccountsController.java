package com.bemobi.aiusercontrol.web;

import com.bemobi.aiusercontrol.dto.response.PendingAccountResponse;
import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PendingAccountsController {

    private final UserAIToolAccountRepository userAIToolAccountRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    public PendingAccountsController(UserAIToolAccountRepository userAIToolAccountRepository) {
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    @GetMapping("/pending-accounts")
    public String list(@RequestParam(required = false) String toolFilter, Model model) {
        // Section 1: Accounts to remove (SUSPENDED/REVOKED)
        List<UserAIToolAccount> toRemove = userAIToolAccountRepository.findAccountsToRemove();

        // Section 2: External/invalid accounts (user IS NULL)
        List<UserAIToolAccount> external = userAIToolAccountRepository.findExternalAccounts();

        // Apply tool filter if provided
        if (toolFilter != null && !toolFilter.isBlank()) {
            toRemove = toRemove.stream()
                    .filter(a -> a.getAiTool() != null && a.getAiTool().getToolType().name().equals(toolFilter))
                    .collect(Collectors.toList());
            external = external.stream()
                    .filter(a -> a.getAiTool() != null && a.getAiTool().getToolType().name().equals(toolFilter))
                    .collect(Collectors.toList());
        }

        List<PendingAccountResponse> accountsToRemove = toRemove.stream()
                .map(a -> toResponse(a, "TO_REMOVE"))
                .collect(Collectors.toList());

        List<PendingAccountResponse> externalAccounts = external.stream()
                .map(a -> toResponse(a, "EXTERNAL"))
                .collect(Collectors.toList());

        model.addAttribute("accountsToRemove", accountsToRemove);
        model.addAttribute("externalAccounts", externalAccounts);
        model.addAttribute("toRemoveCount", accountsToRemove.size());
        model.addAttribute("externalCount", externalAccounts.size());
        model.addAttribute("totalPendingCount", accountsToRemove.size() + externalAccounts.size());
        model.addAttribute("toolFilter", toolFilter);
        // Provide available tool types for filter dropdown
        model.addAttribute("toolTypes", Arrays.stream(AIToolType.values())
                .map(AIToolType::name)
                .collect(Collectors.toList()));

        return "pending-accounts/list";
    }

    private PendingAccountResponse toResponse(UserAIToolAccount account, String section) {
        String userName = null;
        String userEmail = null;
        String suggestedAction;

        if (account.getUser() != null) {
            userName = account.getUser().getName();
            userEmail = account.getUser().getEmail();
        }

        if ("TO_REMOVE".equals(section)) {
            suggestedAction = "Remover conta na plataforma "
                    + (account.getAiTool() != null ? account.getAiTool().getName() : "");
        } else {
            suggestedAction = "Verificar email — nao encontrado no Google Workspace";
        }

        return PendingAccountResponse.builder()
                .id(account.getId())
                .toolName(account.getAiTool() != null ? account.getAiTool().getName() : "Unknown")
                .toolType(account.getAiTool() != null ? account.getAiTool().getToolType().name() : "UNKNOWN")
                .accountIdentifier(account.getAccountIdentifier())
                .accountEmail(account.getAccountEmail())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .section(section)
                .reason("TO_REMOVE".equals(section) ? "Seat cancelado" : "Sem correspondencia GWS")
                .suggestedAction(suggestedAction)
                .userName(userName)
                .userEmail(userEmail)
                .firstSeenAt(account.getFirstSeenAt() != null ? FORMATTER.format(account.getFirstSeenAt()) : null)
                .lastSeenAt(account.getLastSeenAt() != null ? FORMATTER.format(account.getLastSeenAt()) : null)
                .build();
    }
}
