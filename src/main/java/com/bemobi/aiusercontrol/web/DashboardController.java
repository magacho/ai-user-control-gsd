package com.bemobi.aiusercontrol.web;

import com.bemobi.aiusercontrol.aitool.service.AIToolService;
import com.bemobi.aiusercontrol.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final AIToolService aiToolService;

    public DashboardController(UserService userService, AIToolService aiToolService) {
        this.userService = userService;
        this.aiToolService = aiToolService;
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userService.count());
        model.addAttribute("toolCount", aiToolService.count());
        return "dashboard";
    }
}
