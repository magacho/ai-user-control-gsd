package com.bemobi.aiusercontrol.user.controller;

import com.bemobi.aiusercontrol.dto.response.UserDetailResponse;
import com.bemobi.aiusercontrol.dto.response.UserResponse;
import com.bemobi.aiusercontrol.service.AccountLinkingService;
import com.bemobi.aiusercontrol.user.service.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserService userService;
    private final AccountLinkingService accountLinkingService;

    public UserController(UserService userService, AccountLinkingService accountLinkingService) {
        this.userService = userService;
        this.accountLinkingService = accountLinkingService;
    }

    @GetMapping
    public String list(Model model) {
        return "users/list";
    }

    @GetMapping("/table")
    @HxRequest
    public String table(@RequestParam(required = false) String name,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) String department,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<UserResponse> usersPage = userService.findFiltered(name, email, department, status, pageable);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", usersPage.getNumber());
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalElements", usersPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("departments", userService.findAllDepartments());

        model.addAttribute("filterName", name);
        model.addAttribute("filterEmail", email);
        model.addAttribute("filterDepartment", department);
        model.addAttribute("filterStatus", status);

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
