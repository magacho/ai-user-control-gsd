package com.bemobi.aiusercontrol.web;

import com.bemobi.aiusercontrol.dto.response.PendingAccountResponse;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PendingAccountsController {

    private final UserAIToolAccountRepository userAIToolAccountRepository;

    public PendingAccountsController(UserAIToolAccountRepository userAIToolAccountRepository) {
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    @GetMapping("/pending-accounts")
    public String list(Model model) {
        List<UserAIToolAccount> pendingAccounts = userAIToolAccountRepository.findPendingAccounts();

        List<PendingAccountResponse> responses = pendingAccounts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        model.addAttribute("pendingAccounts", responses);
        model.addAttribute("pendingCount", responses.size());
        return "pending-accounts/list";
    }

    private PendingAccountResponse toResponse(UserAIToolAccount account) {
        String reason;
        String userName = null;
        String userEmail = null;

        if (account.getUser() == null) {
            reason = "Sem correspondencia";
        } else {
            reason = "Offboarded";
            userName = account.getUser().getName();
            userEmail = account.getUser().getEmail();
        }

        return PendingAccountResponse.builder()
                .id(account.getId())
                .toolName(account.getAiTool() != null ? account.getAiTool().getName() : "Unknown")
                .toolType(account.getAiTool() != null ? account.getAiTool().getToolType().name() : "UNKNOWN")
                .accountIdentifier(account.getAccountIdentifier())
                .accountEmail(account.getAccountEmail())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .reason(reason)
                .userName(userName)
                .userEmail(userEmail)
                .build();
    }
}
