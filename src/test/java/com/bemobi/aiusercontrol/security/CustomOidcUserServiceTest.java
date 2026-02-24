package com.bemobi.aiusercontrol.security;

import com.bemobi.aiusercontrol.config.AppProperties;
import com.bemobi.aiusercontrol.enums.UserStatus;
import com.bemobi.aiusercontrol.model.entity.User;
import com.bemobi.aiusercontrol.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOidcUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppProperties appProperties;

    private CustomOidcUserService customOidcUserService;

    @BeforeEach
    void setUp() {
        customOidcUserService = new CustomOidcUserService(userRepository, appProperties) {
            @Override
            protected OidcUser loadOidcUser(OidcUserRequest userRequest) {
                // This will be controlled per-test via the mock OidcUser approach
                // We return the OidcUser stored in the test context
                return null; // overridden per test
            }
        };
    }

    @Test
    void testValidAdminLogin_ExistingUser() {
        String email = "admin@bemobi.com";
        String name = "Admin User";
        String picture = "https://example.com/avatar.jpg";

        User existingUser = User.builder()
            .id(1L)
            .email(email)
            .name("Old Name")
            .avatarUrl("https://example.com/old-avatar.jpg")
            .status(UserStatus.ACTIVE)
            .build();

        OidcUser mockOidcUser = createMockOidcUser(email, name, picture, "bemobi.com");
        customOidcUserService = createServiceWithMockOidcUser(mockOidcUser);

        when(appProperties.getAdminEmails()).thenReturn(List.of(email));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OidcUserRequest mockRequest = mock(OidcUserRequest.class);
        OidcUser result = customOidcUserService.loadUser(mockRequest);

        assertNotNull(result);
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Verify profile sync
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(name, savedUser.getName());
        assertEquals(picture, savedUser.getAvatarUrl());
        assertNotNull(savedUser.getLastLoginAt());
    }

    @Test
    void testValidAdminLogin_AutoCreatesUser() {
        String email = "newadmin@bemobi.com";
        String name = "New Admin";
        String picture = "https://example.com/new-avatar.jpg";

        OidcUser mockOidcUser = createMockOidcUser(email, name, picture, "bemobi.com");
        customOidcUserService = createServiceWithMockOidcUser(mockOidcUser);

        when(appProperties.getAdminEmails()).thenReturn(List.of(email));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        OidcUserRequest mockRequest = mock(OidcUserRequest.class);
        OidcUser result = customOidcUserService.loadUser(mockRequest);

        assertNotNull(result);
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // Verify user was created (save called twice: once for creation, once for profile sync)
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(userCaptor.capture());
        User createdUser = userCaptor.getAllValues().get(0);
        assertEquals(email, createdUser.getEmail());
        assertEquals(name, createdUser.getName());
        assertEquals(picture, createdUser.getAvatarUrl());
        assertEquals(UserStatus.ACTIVE, createdUser.getStatus());
    }

    @Test
    void testRejectsNonBemobiDomain() {
        String email = "user@gmail.com";
        OidcUser mockOidcUser = createMockOidcUser(email, "User", null, "gmail.com");
        customOidcUserService = createServiceWithMockOidcUser(mockOidcUser);

        OidcUserRequest mockRequest = mock(OidcUserRequest.class);

        OAuth2AuthenticationException exception = assertThrows(
            OAuth2AuthenticationException.class,
            () -> customOidcUserService.loadUser(mockRequest)
        );

        assertEquals("invalid_domain", exception.getError().getErrorCode());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testRejectsNullEmail() {
        OidcUser mockOidcUser = createMockOidcUser(null, "User", null, null);
        customOidcUserService = createServiceWithMockOidcUser(mockOidcUser);

        OidcUserRequest mockRequest = mock(OidcUserRequest.class);

        OAuth2AuthenticationException exception = assertThrows(
            OAuth2AuthenticationException.class,
            () -> customOidcUserService.loadUser(mockRequest)
        );

        assertEquals("invalid_domain", exception.getError().getErrorCode());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void testRejectsNonRegisteredNonAdminUser() {
        String email = "user@bemobi.com";
        OidcUser mockOidcUser = createMockOidcUser(email, "Regular User", null, "bemobi.com");
        customOidcUserService = createServiceWithMockOidcUser(mockOidcUser);

        when(appProperties.getAdminEmails()).thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        OidcUserRequest mockRequest = mock(OidcUserRequest.class);

        OAuth2AuthenticationException exception = assertThrows(
            OAuth2AuthenticationException.class,
            () -> customOidcUserService.loadUser(mockRequest)
        );

        assertEquals("user_not_registered", exception.getError().getErrorCode());
    }

    @Test
    void testRejectsDeactivatedUser() {
        String email = "inactive@bemobi.com";
        User inactiveUser = User.builder()
            .id(1L)
            .email(email)
            .name("Inactive User")
            .status(UserStatus.INACTIVE)
            .build();

        OidcUser mockOidcUser = createMockOidcUser(email, "Inactive User", null, "bemobi.com");
        customOidcUserService = createServiceWithMockOidcUser(mockOidcUser);

        when(appProperties.getAdminEmails()).thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        OidcUserRequest mockRequest = mock(OidcUserRequest.class);

        OAuth2AuthenticationException exception = assertThrows(
            OAuth2AuthenticationException.class,
            () -> customOidcUserService.loadUser(mockRequest)
        );

        assertEquals("user_deactivated", exception.getError().getErrorCode());
    }

    @Test
    void testRejectsOffboardedUser() {
        String email = "offboarded@bemobi.com";
        User offboardedUser = User.builder()
            .id(2L)
            .email(email)
            .name("Offboarded User")
            .status(UserStatus.OFFBOARDED)
            .build();

        OidcUser mockOidcUser = createMockOidcUser(email, "Offboarded User", null, "bemobi.com");
        customOidcUserService = createServiceWithMockOidcUser(mockOidcUser);

        when(appProperties.getAdminEmails()).thenReturn(Collections.emptyList());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(offboardedUser));

        OidcUserRequest mockRequest = mock(OidcUserRequest.class);

        OAuth2AuthenticationException exception = assertThrows(
            OAuth2AuthenticationException.class,
            () -> customOidcUserService.loadUser(mockRequest)
        );

        assertEquals("user_deactivated", exception.getError().getErrorCode());
    }

    // --- Helper methods ---

    private CustomOidcUserService createServiceWithMockOidcUser(OidcUser oidcUser) {
        return new CustomOidcUserService(userRepository, appProperties) {
            @Override
            protected OidcUser loadOidcUser(OidcUserRequest userRequest) {
                return oidcUser;
            }
        };
    }

    private OidcUser createMockOidcUser(String email, String name, String picture, String hostedDomain) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google-id-12345");
        if (email != null) {
            claims.put("email", email);
        }
        if (name != null) {
            claims.put("name", name);
        }
        if (picture != null) {
            claims.put("picture", picture);
        }
        if (hostedDomain != null) {
            claims.put("hd", hostedDomain);
        }

        OidcIdToken idToken = new OidcIdToken(
            "mock-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            claims
        );

        OidcUserInfo userInfo = new OidcUserInfo(claims);

        return new DefaultOidcUser(
            Collections.emptyList(),
            idToken,
            userInfo
        );
    }
}
