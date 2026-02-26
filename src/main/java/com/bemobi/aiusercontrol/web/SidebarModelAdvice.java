package com.bemobi.aiusercontrol.web;

import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SidebarModelAdvice {

    private final UserAIToolAccountRepository userAIToolAccountRepository;

    public SidebarModelAdvice(UserAIToolAccountRepository userAIToolAccountRepository) {
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    @ModelAttribute("pendingAccountsCount")
    public long pendingAccountsCount() {
        return userAIToolAccountRepository.countAccountsToRemove()
                + userAIToolAccountRepository.countExternalAccounts();
    }
}
