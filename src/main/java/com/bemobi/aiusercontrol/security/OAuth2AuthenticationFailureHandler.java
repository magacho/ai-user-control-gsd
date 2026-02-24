package com.bemobi.aiusercontrol.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorCode = extractErrorCode(exception);
        String redirectUrl = mapErrorCodeToUrl(errorCode);

        log.warn("OAuth2 authentication failure: errorCode={}, message={}", errorCode, exception.getMessage());

        response.sendRedirect(redirectUrl);
    }

    private String extractErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauthException) {
            return oauthException.getError().getErrorCode();
        }
        return "unknown";
    }

    private String mapErrorCodeToUrl(String errorCode) {
        return switch (errorCode) {
            case "invalid_domain" -> "/login?error=domain";
            case "user_not_registered" -> "/login?error=unregistered";
            case "user_deactivated" -> "/login?error=deactivated";
            default -> "/login?error=true";
        };
    }
}
