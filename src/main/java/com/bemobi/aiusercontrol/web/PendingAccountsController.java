package com.bemobi.aiusercontrol.web;

import com.bemobi.aiusercontrol.dto.response.PendingAccountResponse;
import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PendingAccountsController {

    private final UserAIToolAccountRepository userAIToolAccountRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private static final long INACTIVITY_THRESHOLD_DAYS = 60;

    public PendingAccountsController(UserAIToolAccountRepository userAIToolAccountRepository) {
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    @GetMapping("/pending-accounts")
    public String list(@RequestParam(required = false) String toolFilter,
                       @RequestParam(required = false) String sort,
                       Model model) {
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

        // Apply server-side sorting by date columns
        if (sort != null && !sort.isBlank()) {
            boolean descending = sort.startsWith("-");
            String field = descending ? sort.substring(1) : sort;
            Comparator<UserAIToolAccount> comparator = null;

            if ("createdAtSource".equals(field)) {
                comparator = Comparator.comparing(UserAIToolAccount::getCreatedAtSource,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            } else if ("lastActivityAt".equals(field)) {
                comparator = Comparator.comparing(UserAIToolAccount::getLastActivityAt,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            }

            if (comparator != null) {
                if (descending) {
                    comparator = comparator.reversed();
                }
                toRemove.sort(comparator);
                external.sort(comparator);
            }
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
        model.addAttribute("currentSort", sort);
        // Provide available tool types for filter dropdown
        model.addAttribute("toolTypes", AIToolType.values());

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
            suggestedAction = "Verificar email \u2014 n\u00e3o encontrado no Google Workspace";
        }

        // Format source dates (DD/MM/YYYY) and compute inactivity flag
        String createdAtSourceFormatted = account.getCreatedAtSource() != null
                ? DATE_ONLY_FORMATTER.format(account.getCreatedAtSource()) : null;
        String lastActivityAtFormatted = account.getLastActivityAt() != null
                ? DATE_ONLY_FORMATTER.format(account.getLastActivityAt()) : null;
        boolean inactive = account.getLastActivityAt() != null
                && account.getLastActivityAt().isBefore(Instant.now().minus(INACTIVITY_THRESHOLD_DAYS, ChronoUnit.DAYS));

        return PendingAccountResponse.builder()
                .id(account.getId())
                .toolName(account.getAiTool() != null ? account.getAiTool().getName() : "Unknown")
                .toolType(account.getAiTool() != null ? account.getAiTool().getToolType().getDisplayName() : "Unknown")
                .toolTypeIcon(account.getAiTool() != null ? account.getAiTool().getToolType().getIconPath() : null)
                .accountIdentifier(account.getAccountIdentifier())
                .accountEmail(account.getAccountEmail())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .section(section)
                .reason("TO_REMOVE".equals(section) ? "Seat cancelado" : "Sem correspond\u00eancia GWS")
                .suggestedAction(suggestedAction)
                .userName(userName)
                .userEmail(userEmail)
                .firstSeenAt(account.getFirstSeenAt() != null ? FORMATTER.format(account.getFirstSeenAt()) : null)
                .lastSeenAt(account.getLastSeenAt() != null ? FORMATTER.format(account.getLastSeenAt()) : null)
                .createdAtSource(createdAtSourceFormatted)
                .lastActivityAt(lastActivityAtFormatted)
                .inactive(inactive)
                .build();
    }
}
