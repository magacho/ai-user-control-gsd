package com.bemobi.aiusercontrol.security;

import com.bemobi.aiusercontrol.config.AppProperties;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class CustomOidcUserService extends OidcUserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOidcUserService.class);

    private static final String BEMOBI_DOMAIN = "bemobi.com";
    private static final String ERROR_INVALID_DOMAIN = "invalid_domain";
    private static final String ERROR_USER_NOT_REGISTERED = "user_not_registered";
    private static final String ERROR_USER_DEACTIVATED = "user_deactivated";

    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public CustomOidcUserService(UserRepository userRepository, AppProperties appProperties) {
        this.userRepository = userRepository;
        this.appProperties = appProperties;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = loadOidcUser(userRequest);

        String email = oidcUser.getEmail();

        // Domain validation (USER-05)
        validateDomain(email, oidcUser);

        // Pre-registration check
        Optional<User> existingUser = userRepository.findByEmail(email);
        boolean isConfigAdmin = appProperties.getAdminEmails().contains(email);

        if (existingUser.isEmpty() && !isConfigAdmin) {
            log.warn("Non-registered, non-admin user attempted login: {}", email);
            throw new OAuth2AuthenticationException(
                new OAuth2Error(ERROR_USER_NOT_REGISTERED,
                    "Your account has not been registered. Contact an administrator.", null)
            );
        }

        User user;
        if (existingUser.isEmpty()) {
            // Auto-create config admin
            log.info("Auto-creating config admin user: {}", email);
            user = User.builder()
                .email(email)
                .name(oidcUser.getFullName())
                .avatarUrl(oidcUser.getPicture())
                .status(UserStatus.ACTIVE)
                .build();
            user = userRepository.save(user);
        } else {
            user = existingUser.get();
        }

        // Deactivated check
        if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.OFFBOARDED) {
            log.warn("Deactivated user attempted login: {} (status: {})", email, user.getStatus());
            throw new OAuth2AuthenticationException(
                new OAuth2Error(ERROR_USER_DEACTIVATED,
                    "Your account has been deactivated. Contact an administrator.", null)
            );
        }

        // Profile sync on every login
        user.setName(oidcUser.getFullName());
        user.setAvatarUrl(oidcUser.getPicture());
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        log.info("Successful login for user: {}", email);

        // Assign ADMIN authority
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    /**
     * Delegates to the parent OidcUserService to load the user from the OIDC provider.
     * Extracted as a protected method to allow test overriding.
     */
    protected OidcUser loadOidcUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        return super.loadUser(userRequest);
    }

    private void validateDomain(String email, OidcUser oidcUser) {
        if (email == null || !email.endsWith("@" + BEMOBI_DOMAIN)) {
            log.warn("Invalid domain login attempt: {}", email);
            throw new OAuth2AuthenticationException(
                new OAuth2Error(ERROR_INVALID_DOMAIN,
                    "Access is restricted to @bemobi.com accounts.", null)
            );
        }

        String hostedDomain = oidcUser.getClaimAsString("hd");
        if (!BEMOBI_DOMAIN.equals(hostedDomain)) {
            log.warn("Invalid hosted domain login attempt: email={}, hd={}", email, hostedDomain);
            throw new OAuth2AuthenticationException(
                new OAuth2Error(ERROR_INVALID_DOMAIN,
                    "Access is restricted to @bemobi.com accounts.", null)
            );
        }
    }
}
