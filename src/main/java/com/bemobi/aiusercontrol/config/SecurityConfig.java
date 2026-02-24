package com.bemobi.aiusercontrol.config;

import com.bemobi.aiusercontrol.security.CustomOidcUserService;
import com.bemobi.aiusercontrol.security.OAuth2AuthenticationFailureHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(CustomOidcUserService customOidcUserService,
                          OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.customOidcUserService = customOidcUserService;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login/**",
                    "/oauth2/**",
                    "/error/**",
                    "/webjars/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(authorizationRequestResolver())
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    private OAuth2AuthorizationRequestResolver authorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver resolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(selectAccountPrompt());
        return resolver;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder> selectAccountPrompt() {
        return builder -> {
            Map<String, Object> additionalParams = new HashMap<>(builder.build().getAdditionalParameters());
            additionalParams.put("prompt", "select_account");
            builder.additionalParameters(additionalParams);
        };
    }
}
