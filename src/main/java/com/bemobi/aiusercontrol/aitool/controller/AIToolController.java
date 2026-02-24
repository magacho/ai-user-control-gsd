package com.bemobi.aiusercontrol.aitool.controller;

import com.bemobi.aiusercontrol.aitool.service.AIToolService;
import com.bemobi.aiusercontrol.dto.request.AIToolRequest;
import com.bemobi.aiusercontrol.dto.response.AIToolResponse;
import com.bemobi.aiusercontrol.enums.AIToolType;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/ai-tools")
public class AIToolController {

    private final AIToolService aiToolService;

    public AIToolController(AIToolService aiToolService) {
        this.aiToolService = aiToolService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tools", aiToolService.findAll());
        model.addAttribute("toolCount", aiToolService.count());
        return "ai-tools/list";
    }

    @GetMapping("/table")
    @HxRequest
    public String table(Model model) {
        model.addAttribute("tools", aiToolService.findAll());
        return "ai-tools/fragments/table :: toolTable";
    }

    @GetMapping("/new")
    @HxRequest
    public String newForm(Model model) {
        model.addAttribute("aiToolRequest", new AIToolRequest());
        model.addAttribute("toolTypes", AIToolType.values());
        model.addAttribute("editMode", false);
        return "ai-tools/fragments/form-modal :: toolForm";
    }

    @GetMapping("/{id}/edit")
    @HxRequest
    public String editForm(@PathVariable Long id, Model model) {
        Optional<AIToolResponse> tool = aiToolService.findById(id);
        if (tool.isEmpty()) {
            return "ai-tools/fragments/table :: toolTable";
        }

        AIToolResponse response = tool.get();
        AIToolRequest request = new AIToolRequest();
        request.setName(response.getName());
        request.setToolType(response.getToolType() != null ? AIToolType.valueOf(response.getToolType()) : null);
        request.setDescription(response.getDescription());
        request.setApiBaseUrl(response.getApiBaseUrl());
        request.setEnabled(response.isEnabled());
        request.setApiOrgId(response.getApiOrgId());
        request.setIconUrl(response.getIconUrl());

        model.addAttribute("aiToolRequest", request);
        model.addAttribute("toolTypes", AIToolType.values());
        model.addAttribute("editMode", true);
        model.addAttribute("toolId", id);
        model.addAttribute("hasApiKey", response.isHasApiKey());
        return "ai-tools/fragments/form-modal :: toolForm";
    }

    @PostMapping
    @HxRequest
    public String create(@Valid AIToolRequest request, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("aiToolRequest", request);
            model.addAttribute("toolTypes", AIToolType.values());
            model.addAttribute("editMode", false);
            return "ai-tools/fragments/form-modal :: toolForm";
        }

        try {
            aiToolService.create(request);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("aiToolRequest", request);
            model.addAttribute("toolTypes", AIToolType.values());
            model.addAttribute("editMode", false);
            return "ai-tools/fragments/form-modal :: toolForm";
        }

        model.addAttribute("tools", aiToolService.findAll());
        return "ai-tools/fragments/table :: toolTable";
    }

    @PutMapping("/{id}")
    @HxRequest
    public String update(@PathVariable Long id, @Valid AIToolRequest request,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("aiToolRequest", request);
            model.addAttribute("toolTypes", AIToolType.values());
            model.addAttribute("editMode", true);
            model.addAttribute("toolId", id);
            return "ai-tools/fragments/form-modal :: toolForm";
        }

        try {
            aiToolService.update(id, request);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            model.addAttribute("aiToolRequest", request);
            model.addAttribute("toolTypes", AIToolType.values());
            model.addAttribute("editMode", true);
            model.addAttribute("toolId", id);
            return "ai-tools/fragments/form-modal :: toolForm";
        }

        model.addAttribute("tools", aiToolService.findAll());
        return "ai-tools/fragments/table :: toolTable";
    }

    @DeleteMapping("/{id}")
    @HxRequest
    public String delete(@PathVariable Long id, Model model) {
        aiToolService.delete(id);
        model.addAttribute("tools", aiToolService.findAll());
        return "ai-tools/fragments/table :: toolTable";
    }
}
