package com.bemobi.aiusercontrol.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Configuration
public class WebConfig {

    @ControllerAdvice
    static class GlobalModelAttributes {

        @ModelAttribute("currentPath")
        public String currentPath(HttpServletRequest request) {
            return request.getRequestURI();
        }
    }
}
