package com.bemobi.aiusercontrol.web;

import com.bemobi.aiusercontrol.aitool.service.AIToolService;
import com.bemobi.aiusercontrol.enums.AccountStatus;
import com.bemobi.aiusercontrol.user.repository.UserAIToolAccountRepository;
import com.bemobi.aiusercontrol.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final AIToolService aiToolService;
    private final UserAIToolAccountRepository userAIToolAccountRepository;

    public DashboardController(UserService userService, AIToolService aiToolService,
                               UserAIToolAccountRepository userAIToolAccountRepository) {
        this.userService = userService;
        this.aiToolService = aiToolService;
        this.userAIToolAccountRepository = userAIToolAccountRepository;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userService.count());
        model.addAttribute("toolCount", aiToolService.count());
        model.addAttribute("activeAccountCount", userAIToolAccountRepository.countByStatus(AccountStatus.ACTIVE));
        return "dashboard";
    }
}
