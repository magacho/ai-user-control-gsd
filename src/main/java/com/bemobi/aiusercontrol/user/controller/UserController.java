package com.bemobi.aiusercontrol.user.controller;

import com.bemobi.aiusercontrol.user.service.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}
