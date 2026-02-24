package com.bemobi.aiusercontrol.user.controller;

import com.bemobi.aiusercontrol.dto.response.UserDetailResponse;
import com.bemobi.aiusercontrol.service.AccountLinkingService;
import com.bemobi.aiusercontrol.user.service.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AccountLinkingService accountLinkingService;

    public UserController(UserService userService, AccountLinkingService accountLinkingService) {
        this.userService = userService;
        this.accountLinkingService = accountLinkingService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("userCount", userService.count());
        return "users/list";
    }

    @GetMapping("/table")
    @HxRequest
    public String table(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users/fragments/table :: userTable";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        UserDetailResponse user = userService.getUserDetail(id);
        model.addAttribute("user", user);
        return "users/detail";
    }

    @DeleteMapping("/{userId}/accounts/{accountId}")
    @HxRequest
    public String unlinkAccount(@PathVariable Long userId, @PathVariable Long accountId, Model model) {
        accountLinkingService.unlinkAccount(accountId);
        UserDetailResponse user = userService.getUserDetail(userId);
        model.addAttribute("user", user);
        return "users/fragments/accounts-table :: accountsTable";
    }
}
