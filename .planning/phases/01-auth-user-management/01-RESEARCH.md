# Phase 1: Auth & User Management - Research

**Researched:** 2026-02-24
**Domain:** Spring Security OAuth2 (Google SSO), Thymeleaf + HTMX UI, Data Modeling (JPA/Flyway)
**Confidence:** HIGH

## Summary

Phase 1 establishes admin-only authentication via Google SSO (OAuth2/OIDC), the core data models (User, AITool), and the navigation skeleton using Thymeleaf + HTMX with Tailwind CSS. The existing project scaffold has Spring Boot 3.4.2 with JPA, Security, Thymeleaf, Flyway, and PostgreSQL already declared in `pom.xml`, but all Java source files and SQL migrations are empty stubs requiring full implementation.

The key architectural shift from the existing scaffold: the project currently includes JJWT dependencies and JWT-based security configuration, but CONTEXT.md mandates Google SSO with server-side sessions (no JWT for authentication). The JWT dependencies and related classes (`JwtTokenProvider`, `JwtAuthenticationFilter`) should be removed and replaced with `spring-boot-starter-oauth2-client` and a custom `OidcUserService` that enforces @bemobi.com domain restriction, pre-registered user validation, and admin role assignment from configuration.

The UI layer uses Thymeleaf templates with HTMX for partial page updates and Tailwind CSS for styling. The `htmx-spring-boot-thymeleaf` 4.0.2 library provides Spring Boot auto-configuration and a Thymeleaf dialect for HTMX attributes. Tailwind CSS should be included via CDN for this internal admin tool, avoiding a Node.js build pipeline.

**Primary recommendation:** Replace the JWT-based authentication scaffold with Spring Security OAuth2 Client (`oauth2Login()`), implement a custom `OidcUserService` for domain + pre-registration validation, use HTTP sessions with 8-hour timeout, and build the sidebar layout with Thymeleaf Layout Dialect + HTMX for navigation.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- Google SSO (OAuth) only -- no password fallback
- Restricted to @bemobi.com Google Workspace domain
- Admin-only access -- developers never log in (Phase 8 dropped entirely)
- Pre-registered users only: admin must approve/import users before they can access the system
- Initial admin(s) bootstrapped via application config property (list of admin emails)
- Pull name and profile picture from Google Workspace on login
- Google avatar displayed in the UI (navbar)
- 8-hour session timeout (one workday), then re-authenticate via Google
- Deactivated/non-registered users get a friendly rejection message (not a generic error)
- Multiple admins supported, all with equal permissions (no super admin hierarchy)
- Admin emails defined in application config only -- adding a new admin requires config update + restart
- No admin management through the UI
- Left sidebar navigation skeleton -- ready for future phases to fill in sections
- Clean & minimal visual style (Linear/Vercel aesthetic)
- Tailwind CSS for styling
- Thymeleaf + HTMX (existing decision)
- Desktop only -- no responsive/mobile design
- Data table layout for user lists
- Users are NOT manually created by admins -- discovered from AI tool provider APIs
- AI tool catalog is extensible, stored in database, managed through dashboard UI (CRUD)
- Admin creates tools through the UI (no pre-seeded defaults)
- No setup wizard -- admin logs in and sees data immediately (if any exists)
- Provider APIs configured at deployment via application properties, not through UI

### Claude's Discretion
- Login page layout and design
- User table columns and presentation
- Primary user identifier choice (email vs Google Workspace ID)
- AI tool entity field design
- Empty state messaging and display
- Loading skeleton and error state design

### Deferred Ideas (OUT OF SCOPE)
- Phase 8 (Developer Self-Service) dropped entirely -- platform is admin-only forever
- AUTH-02 (Developer login) removed from requirements
- DEV-01 through DEV-05 (developer dashboard requirements) removed
- USER-02 (Admin creates users) reframed -- users come from provider APIs, not manual creation
- USER-03 (Admin updates user info) reframed -- read-only, no manual edits
- USER-04 (Admin deactivates user) reframed -- status is automatic from data, not manual action
- Email notification system for orphaned accounts -- implementation deferred to later phases (Phase 5+)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|-----------------|
| AUTH-01 | Admin can log in with JWT token and access all system features | **Reframed:** Admin logs in via Google SSO (not JWT). Custom `OidcUserService` checks @bemobi.com domain, validates pre-registration in DB, assigns ADMIN role from config property. Session-based auth replaces JWT tokens. |
| AUTH-02 | Developer can log in with JWT token and access only personal usage data | **DROPPED per CONTEXT.md.** Platform is admin-only. No developer login. |
| AUTH-03 | System enforces role-based access control (Admin vs Developer) | **Simplified:** Only ADMIN role exists. All authenticated users are admins (verified via config property). Spring Security `hasRole("ADMIN")` on all protected endpoints. |
| AUTH-04 | User session persists securely across requests | HTTP session with `HttpSessionSecurityContextRepository` (Spring Security default for OAuth2 login). 8-hour timeout via `server.servlet.session.timeout=8h`. JSESSIONID cookie with secure flags. |
| USER-01 | Admin can view list of all users with corporate email (@bemobi.com) | JPA `UserRepository` with Thymeleaf data table. Users come from provider API discovery (later phases), but the read-only list view is built now with empty state handling. |
| USER-02 | Admin can create new user with email, name, department, status | **Reframed per CONTEXT.md:** Users are NOT manually created. They are discovered from AI tool provider APIs. This phase builds the User entity/migration but no create UI. |
| USER-03 | Admin can update user information | **Reframed per CONTEXT.md:** Read-only platform. No manual user edits. |
| USER-04 | Admin can deactivate user | **Reframed per CONTEXT.md:** Status is determined automatically from data (provider APIs + Google Workspace). No manual deactivation UI. |
| USER-05 | System validates corporate email domain (@bemobi.com) | Enforced at two levels: (1) Google OAuth2 `hd` parameter restricts Google consent screen to @bemobi.com, (2) Custom `OidcUserService` validates email domain server-side. Also enforced on User entity via JPA validation. |
| USER-06 | System tracks user status (ACTIVE, INACTIVE, OFFBOARDED) | `UserStatus` enum in entity. ACTIVE = in Google Workspace + AI tools. INACTIVE = in AI tools but NOT in Google Workspace (orphaned). Status computed from data in later phases; enum and column established now. |
</phase_requirements>

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 3.4.2 | Application framework | Already in `pom.xml`. Provides auto-configuration for OAuth2, JPA, Thymeleaf |
| spring-boot-starter-oauth2-client | (managed by Spring Boot 3.4.2) | Google SSO via OAuth2/OIDC | **Must add.** Standard way to do OAuth2 login in Spring Security 6.x. Provides `oauth2Login()` DSL, `OidcUser`, `OidcUserService` |
| spring-boot-starter-security | (managed) | Authentication & authorization | Already in `pom.xml`. Provides `SecurityFilterChain`, session management, CSRF protection |
| spring-boot-starter-thymeleaf | (managed) | Server-side HTML templates | Already in `pom.xml`. Auto-configures Thymeleaf engine |
| thymeleaf-extras-springsecurity6 | (managed) | Security expressions in templates | Already in `pom.xml`. Enables `sec:authorize`, `sec:authentication` in templates |
| spring-boot-starter-data-jpa | (managed) | ORM / database access | Already in `pom.xml`. Hibernate + Spring Data repositories |
| Flyway | 10.21.0 | Database migrations | Already in `pom.xml`. Versioned SQL migrations in `db/migration/` |
| PostgreSQL | 16 | Production database | Already in `docker-compose.yml`. Driver already in `pom.xml` |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| htmx-spring-boot-thymeleaf | 4.0.2 | HTMX + Thymeleaf integration | **Must add.** Provides `@HxRequest` annotation, `HtmxResponse`, Thymeleaf dialect for hx-* attributes. Version 4.0.2 targets Spring Boot 3.4.x |
| org.webjars.npm:htmx.org | 2.0.4 | HTMX JavaScript library via WebJars | **Must add.** Serves htmx.js from classpath without CDN/npm. Use with `webjars-locator-lite` for version-agnostic paths |
| org.webjars:webjars-locator-lite | (managed by Spring Boot) | Version-agnostic WebJar path resolution | **Must add.** Enables `/webjars/htmx.org/dist/htmx.min.js` without version in URL. Note: `webjars-locator-core` is deprecated in Spring Boot 3.4+ |
| nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect | 3.4.0 | Template layout/decorator pattern | **Must add.** Enables shared layout template with sidebar + content area. Auto-configured by Spring Boot when on classpath |
| Tailwind CSS | 4.x | Utility-first CSS framework | Include via CDN (`<script src="https://cdn.tailwindcss.com">`) for this internal admin tool. Avoids Node.js build pipeline complexity |
| Lombok | (managed) | Boilerplate reduction | Already in `pom.xml`. Use for `@Data`, `@Builder`, `@NoArgsConstructor` on entities and DTOs |
| MapStruct | 1.6.3 | Object mapping | Already in `pom.xml`. Use for entity-to-DTO mapping if needed |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Tailwind CDN | Tailwind via npm + frontend-maven-plugin | Build pipeline adds complexity; CDN is fine for internal admin tool with desktop-only audience |
| thymeleaf-layout-dialect | Thymeleaf fragments only (`th:insert`/`th:replace`) | Layout dialect is cleaner for full-page decorator pattern with sidebar; fragments require more boilerplate |
| HTMX WebJars | HTMX CDN | WebJars are more reliable for internal deployment (no external CDN dependency) and align with Spring Boot conventions |
| Server-side sessions | JWT tokens (existing scaffold) | **JWT is wrong for this use case.** SSR with Thymeleaf needs server-side sessions. JWT is for stateless APIs. Google SSO naturally works with sessions. |

### Dependencies to Add (pom.xml)

```xml
<!-- OAuth2 Client for Google SSO -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- HTMX + Spring Boot + Thymeleaf integration -->
<dependency>
    <groupId>io.github.wimdeblauwe</groupId>
    <artifactId>htmx-spring-boot-thymeleaf</artifactId>
    <version>4.0.2</version>
</dependency>

<!-- HTMX JavaScript via WebJars -->
<dependency>
    <groupId>org.webjars.npm</groupId>
    <artifactId>htmx.org</artifactId>
    <version>2.0.4</version>
</dependency>

<!-- WebJars version-agnostic path resolution -->
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>webjars-locator-lite</artifactId>
</dependency>

<!-- Thymeleaf Layout Dialect for page layouts -->
<dependency>
    <groupId>nz.net.ultraq.thymeleaf</groupId>
    <artifactId>thymeleaf-layout-dialect</artifactId>
</dependency>
```

### Dependencies to Remove (pom.xml)

```xml
<!-- REMOVE: JWT is replaced by OAuth2 session-based auth -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
</dependency>
```

## Architecture Patterns

### Recommended Project Structure

```
src/main/java/com/bemobi/aiusercontrol/
├── AiUserControlApplication.java
├── config/
│   ├── SecurityConfig.java          # OAuth2 login, session mgmt, CSRF, authorization rules
│   ├── OAuth2Config.java            # Google OAuth2 properties binding (@ConfigurationProperties)
│   ├── AppProperties.java           # Custom app config (admin emails list, etc.)
│   ├── WebConfig.java               # Static resource handling, WebJars
│   └── DatabaseConfig.java          # DataSource, JPA config (if needed beyond auto-config)
├── security/
│   ├── CustomOidcUserService.java   # Domain validation, pre-registration check, admin role assignment
│   ├── OAuth2AuthenticationSuccessHandler.java  # Update user profile on login (name, avatar)
│   └── OAuth2AuthenticationFailureHandler.java  # Friendly rejection for unauthorized users
├── model/
│   └── entity/
│       ├── User.java                # Corporate user (email, name, department, avatarUrl, status)
│       └── AITool.java              # AI tool catalog entry (name, type, description, config)
├── enums/
│   ├── UserStatus.java              # ACTIVE, INACTIVE, OFFBOARDED
│   └── AIToolType.java              # CLAUDE, GITHUB_COPILOT, CURSOR, CUSTOM
├── user/
│   ├── controller/UserController.java
│   ├── service/UserService.java
│   └── repository/UserRepository.java
├── aitool/
│   ├── controller/AIToolController.java
│   ├── service/AIToolService.java
│   └── repository/AIToolRepository.java
├── web/
│   └── DashboardController.java     # Main dashboard page, login page
├── dto/
│   ├── request/AIToolRequest.java
│   └── response/
│       ├── UserResponse.java
│       └── AIToolResponse.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── BusinessException.java

src/main/resources/
├── application.yml                   # Base config with OAuth2 Google client registration
├── application-dev.yml
├── application-prod.yml
├── db/migration/
│   ├── V1__create_users_table.sql
│   └── V2__create_ai_tools_table.sql
├── templates/
│   ├── layout/
│   │   └── default.html             # Base layout: sidebar + content area + navbar
│   ├── fragments/
│   │   ├── sidebar.html             # Left sidebar navigation
│   │   └── navbar.html              # Top bar with user avatar + logout
│   ├── login.html                   # Google SSO login page
│   ├── error/
│   │   ├── access-denied.html       # Friendly rejection for non-registered/deactivated users
│   │   └── error.html               # Generic error page
│   ├── dashboard.html               # Main dashboard (empty state for now)
│   ├── users/
│   │   ├── list.html                # User list data table (read-only)
│   │   └── fragments/               # HTMX partial fragments for user table
│   └── ai-tools/
│       ├── list.html                # AI tool catalog list
│       ├── form.html                # AI tool create/edit form
│       └── fragments/               # HTMX partial fragments for tool CRUD
└── static/
    └── css/
        └── custom.css               # Minimal custom CSS beyond Tailwind
```

### Pattern 1: Custom OidcUserService for Multi-Layer Validation

**What:** A custom `OidcUserService` that intercepts Google OIDC login to enforce domain restriction, pre-registration validation, and admin role assignment.

**When to use:** Every Google SSO login attempt flows through this service.

**Example:**
```java
// Source: Spring Security docs - Advanced Configuration
// https://docs.spring.io/spring-security/reference/servlet/oauth2/login/advanced.html

@Component
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final AppProperties appProperties; // Contains admin email list

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();

        // 1. Validate @bemobi.com domain (defense in depth; hd param is first layer)
        if (email == null || !email.endsWith("@bemobi.com")) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_domain"),
                "Access restricted to @bemobi.com accounts"
            );
        }

        // 2. Check pre-registration: user must exist in database
        User user = userRepository.findByEmail(email).orElse(null);
        boolean isConfigAdmin = appProperties.getAdminEmails().contains(email);

        if (user == null && !isConfigAdmin) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("user_not_registered"),
                "Your account has not been registered. Contact an administrator."
            );
        }

        // 3. If config admin but not yet in DB, auto-create
        if (user == null && isConfigAdmin) {
            user = User.builder()
                .email(email)
                .name(oidcUser.getFullName())
                .avatarUrl(oidcUser.getPicture())
                .status(UserStatus.ACTIVE)
                .build();
            userRepository.save(user);
        }

        // 4. Check user is not deactivated
        if (user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.OFFBOARDED) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("user_deactivated"),
                "Your account has been deactivated. Contact an administrator."
            );
        }

        // 5. Update profile info from Google on every login
        user.setName(oidcUser.getFullName());
        user.setAvatarUrl(oidcUser.getPicture());
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        // 6. Assign ADMIN authority (all authenticated users are admins)
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
```

### Pattern 2: Thymeleaf Layout with Sidebar Navigation

**What:** Decorator pattern using Thymeleaf Layout Dialect for consistent page layout with sidebar navigation.

**When to use:** Every authenticated page inherits the base layout.

**Example:**
```html
<!-- templates/layout/default.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <title layout:title-pattern="$CONTENT_TITLE - AI User Control">AI User Control</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script th:src="@{/webjars/htmx.org/dist/htmx.min.js}"></script>
</head>
<body class="bg-gray-50 min-h-screen">
    <div class="flex h-screen">
        <!-- Left Sidebar -->
        <nav th:replace="~{fragments/sidebar :: sidebar}"></nav>

        <!-- Main Content Area -->
        <div class="flex-1 flex flex-col overflow-hidden">
            <!-- Top Navbar with user avatar -->
            <header th:replace="~{fragments/navbar :: navbar}"></header>

            <!-- Page Content -->
            <main class="flex-1 overflow-y-auto p-6">
                <div layout:fragment="content">
                    <!-- Page-specific content goes here -->
                </div>
            </main>
        </div>
    </div>
</body>
</html>
```

```html
<!-- templates/users/list.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/default}">
<head>
    <title>Users</title>
</head>
<body>
    <div layout:fragment="content">
        <h1 class="text-2xl font-semibold text-gray-900 mb-6">Users</h1>
        <!-- Data table with HTMX partial loading -->
        <div id="user-table" hx-get="/users/table" hx-trigger="load" hx-swap="innerHTML">
            <!-- Loading skeleton -->
        </div>
    </div>
</body>
</html>
```

### Pattern 3: SecurityFilterChain for OAuth2 + Session Management

**What:** Spring Security configuration for Google SSO with session-based auth.

**When to use:** Single security config replaces the JWT-based scaffold.

**Example:**
```java
// Source: Spring Security Reference - OAuth2 Login Core Configuration
// https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login/**", "/oauth2/**", "/error/**").permitAll()
                .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)
                )
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)  // New login displaces old session
            );

        return http.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
```

### Pattern 4: HTMX Partial Updates for Data Tables

**What:** Use HTMX to load and update table content without full page reloads.

**When to use:** User lists, AI tool catalog -- any data table that benefits from partial updates.

**Example:**
```java
// Controller with HTMX support
@Controller
@RequestMapping("/ai-tools")
@RequiredArgsConstructor
public class AIToolController {

    private final AIToolService aiToolService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tools", aiToolService.findAll());
        return "ai-tools/list";
    }

    @GetMapping("/table")
    @HxRequest  // Only responds to HTMX requests
    public String tableFragment(Model model) {
        model.addAttribute("tools", aiToolService.findAll());
        return "ai-tools/fragments/table :: toolTable";
    }

    @PostMapping
    @HxRequest
    public HtmxResponse create(@Valid @ModelAttribute AIToolRequest request, Model model) {
        aiToolService.create(request);
        model.addAttribute("tools", aiToolService.findAll());
        return HtmxResponse.builder()
            .view("ai-tools/fragments/table :: toolTable")
            .trigger("tool-created")
            .build();
    }
}
```

### Anti-Patterns to Avoid

- **JWT for server-rendered pages:** JWT tokens are for stateless APIs. Thymeleaf + HTMX is server-rendered, so use HTTP sessions. The existing scaffold's JWT approach is wrong for this architecture.
- **Storing OAuth2 tokens in the database:** Spring Security handles token storage in the session automatically. Do not hand-roll token persistence.
- **Manual CSRF handling with HTMX:** HTMX automatically sends the CSRF token if you include `<meta name="_csrf" th:content="${_csrf.token}">` and configure htmx with `hx-headers`. However, Spring Security's default CSRF is cookie-based in 6.x so HTMX works out of the box.
- **`open-in-view: true`:** The scaffold correctly has `open-in-view: false`. Keep it that way to avoid lazy-loading issues in templates.
- **Fat controllers:** Keep controllers thin (delegation to service layer). Thymeleaf controllers should only populate the model and return view names.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Google OAuth2 flow | Custom OAuth2 token exchange code | `spring-boot-starter-oauth2-client` + `oauth2Login()` | OAuth2/OIDC is complex (nonces, PKCE, token refresh). Spring Security handles all of it. |
| Session management | Custom session cookie logic | Spring Security's `HttpSession` + `sessionManagement()` DSL | Session fixation protection, concurrent session control, timeout handling all built in. |
| CSRF protection | Manual token generation/validation | Spring Security auto-CSRF (default enabled) | CSRF tokens are automatically injected into forms and validated. HTMX works with cookie-based CSRF. |
| Google domain restriction | Manual email parsing after login | `hd` parameter on authorization request + server-side validation | Two-layer defense: Google limits consent screen to domain, then server validates email. |
| Template layouts | Copy-paste HTML boilerplate | Thymeleaf Layout Dialect (`layout:decorate`) | Decorator pattern eliminates HTML duplication. One change to sidebar updates all pages. |
| Database schema management | Manual `CREATE TABLE` scripts or Hibernate `ddl-auto=create` | Flyway versioned migrations | Reproducible, auditable schema changes. Already configured in project. |
| Object mapping | Manual entity-to-DTO conversion | MapStruct (already in `pom.xml`) or simple constructor mapping | Reduces boilerplate and potential mapping bugs. |

**Key insight:** Google SSO with Spring Security is almost entirely configuration-driven. The custom code should only be the `OidcUserService` (domain + registration validation) and the `SecurityFilterChain` bean. Everything else (token exchange, session creation, CSRF, logout) is handled by the framework.

## Common Pitfalls

### Pitfall 1: Missing `spring-boot-starter-oauth2-client` Dependency
**What goes wrong:** `oauth2Login()` is available on `HttpSecurity` even without the OAuth2 client starter (it's in `spring-security-config`), but the actual `ClientRegistrationRepository` auto-configuration requires `spring-boot-starter-oauth2-client`. The app starts but fails at runtime when a user tries to log in.
**Why it happens:** The security starter provides the DSL; the oauth2-client starter provides the implementation.
**How to avoid:** Explicitly add `spring-boot-starter-oauth2-client` to `pom.xml`.
**Warning signs:** `NoSuchBeanDefinitionException: ClientRegistrationRepository` at startup or login time.

### Pitfall 2: Google `hd` Parameter is a Hint, Not Enforcement
**What goes wrong:** The `hd` (hosted domain) parameter on Google's authorization URL only pre-selects the domain in the Google account chooser. It does NOT prevent users from switching to a personal Gmail account. Relying solely on `hd` leaves a security hole.
**Why it happens:** Google documents `hd` as a UI hint, not a security boundary.
**How to avoid:** Always validate the email domain server-side in the custom `OidcUserService`, even if `hd` is set. Check both `oidcUser.getEmail()` suffix AND `oidcUser.getClaimAsString("hd")`.
**Warning signs:** Users with personal Gmail accounts able to access the system.

### Pitfall 3: Session Timeout Not Configured
**What goes wrong:** Default Tomcat session timeout is 30 minutes. The requirement is 8 hours. If not configured, admins have to re-login every 30 minutes.
**Why it happens:** Session timeout is a servlet container setting, not a Spring Security setting.
**How to avoid:** Set `server.servlet.session.timeout=8h` in `application.yml`.
**Warning signs:** Users reporting frequent re-authentication prompts.

### Pitfall 4: HTMX Requests Returning Full Pages Instead of Fragments
**What goes wrong:** An HTMX request triggers a full page load (with layout, sidebar, etc.) instead of just the fragment, causing nested layouts in the DOM.
**Why it happens:** The controller returns the full template name instead of a fragment selector, or the `@HxRequest` annotation is missing.
**How to avoid:** Use `@HxRequest` from `htmx-spring-boot-thymeleaf` to detect HTMX requests, and return fragment selectors like `"users/fragments/table :: userTable"`.
**Warning signs:** Duplicated sidebars/navbars appearing after HTMX-triggered updates.

### Pitfall 5: OAuth2 Login Redirect Loop After Failure
**What goes wrong:** When a user fails authentication (wrong domain, not pre-registered), they get redirected back to the login page which auto-redirects to Google, creating an infinite loop.
**Why it happens:** The failure handler redirects to a URL that is itself protected, triggering another OAuth2 login.
**How to avoid:** Configure `failureUrl("/login?error=true")` and ensure `/login` and `/login?error=true` are in `permitAll()`. The login page should display the error message rather than auto-redirecting to Google.
**Warning signs:** Browser shows "too many redirects" error.

### Pitfall 6: Flyway Migration Checksum Mismatch
**What goes wrong:** Modifying an already-applied migration causes Flyway to fail on startup with checksum validation errors.
**Why it happens:** Flyway's immutability principle -- applied migrations must never change.
**How to avoid:** Never edit applied migrations. Always create new V{n+1}__ migrations. The project has `validate-on-migrate: true` which catches this.
**Warning signs:** `FlywayValidateException: Migration Checksum mismatch` at startup.

### Pitfall 7: Tailwind CDN Purge Not Working
**What goes wrong:** In development, Tailwind CDN works fine. But the CDN script does not purge unused classes, resulting in a large CSS payload.
**Why it happens:** The CDN version includes all utility classes (~3MB).
**How to avoid:** For an internal admin tool with desktop-only users on a corporate network, the CDN's bundle size is acceptable. If it becomes an issue in the future, switch to a build pipeline with `frontend-maven-plugin`. Do not pre-optimize.
**Warning signs:** Slow first load on very low bandwidth connections (unlikely for corporate network).

## Code Examples

Verified patterns from official sources:

### OAuth2 Google Client Configuration (application.yml)

```yaml
# Source: Spring Security OAuth2 Login Core Configuration
# https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"

server:
  servlet:
    session:
      timeout: 8h  # 8-hour session = one workday
      cookie:
        http-only: true
        secure: true   # Set to false for local dev (no HTTPS)
        same-site: lax

app:
  admin-emails:
    - admin1@bemobi.com
    - admin2@bemobi.com
```

### AppProperties Configuration Binding

```java
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {
    private List<String> adminEmails = new ArrayList<>();
}
```

### User Entity (Flyway Migration)

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255),
    department      VARCHAR(255),
    avatar_url      VARCHAR(1024),
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_login_at   TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'OFFBOARDED')),
    CONSTRAINT chk_user_email_domain CHECK (email LIKE '%@bemobi.com')
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
```

### AI Tool Entity (Flyway Migration)

```sql
-- V2__create_ai_tools_table.sql
CREATE TABLE ai_tools (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    tool_type       VARCHAR(50) NOT NULL,
    description     VARCHAR(1024),
    api_base_url    VARCHAR(1024),
    enabled         BOOLEAN NOT NULL DEFAULT true,
    icon_url        VARCHAR(1024),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tool_type CHECK (tool_type IN ('CLAUDE', 'GITHUB_COPILOT', 'CURSOR', 'CUSTOM'))
);
```

### User JPA Entity

```java
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;
    private String department;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
```

### HTMX Data Table Fragment

```html
<!-- templates/ai-tools/fragments/table.html -->
<div th:fragment="toolTable">
    <table class="min-w-full divide-y divide-gray-200" th:if="${not #lists.isEmpty(tools)}">
        <thead class="bg-gray-50">
            <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Type</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
            <tr th:each="tool : ${tools}">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900"
                    th:text="${tool.name}"></td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500"
                    th:text="${tool.toolType}"></td>
                <td class="px-6 py-4 whitespace-nowrap">
                    <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                          th:classappend="${tool.enabled ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}"
                          th:text="${tool.enabled ? 'Enabled' : 'Disabled'}"></span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    <button hx-get th:hx-get="@{'/ai-tools/' + ${tool.id} + '/edit'}"
                            hx-target="#modal-container" hx-swap="innerHTML"
                            class="text-indigo-600 hover:text-indigo-900">Edit</button>
                    <button hx-delete th:hx-delete="@{'/ai-tools/' + ${tool.id}}"
                            hx-target="closest tr" hx-swap="outerHTML swap:1s"
                            hx-confirm="Are you sure?"
                            class="ml-4 text-red-600 hover:text-red-900">Delete</button>
                </td>
            </tr>
        </tbody>
    </table>

    <!-- Empty State -->
    <div th:if="${#lists.isEmpty(tools)}" class="text-center py-12">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <h3 class="mt-2 text-sm font-medium text-gray-900">No AI tools configured</h3>
        <p class="mt-1 text-sm text-gray-500">Get started by adding your first AI tool.</p>
        <div class="mt-6">
            <button hx-get="/ai-tools/new" hx-target="#modal-container" hx-swap="innerHTML"
                    class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700">
                Add AI Tool
            </button>
        </div>
    </div>
</div>
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `spring-security-oauth2` (separate module) | `spring-boot-starter-oauth2-client` (built into Spring Security 5.x+/6.x) | Spring Security 5.0 (2017) | All OAuth2 config is now part of core Spring Security. Do NOT use the old `spring-security-oauth2` artifact. |
| `SecurityContextPersistenceFilter` | `SecurityContextHolderFilter` (Spring Security 6+) | Spring Security 6.0 (2022) | `SecurityContext` requires explicit save in some scenarios. OAuth2 login handles this automatically. |
| `webjars-locator-core` | `webjars-locator-lite` | Spring Boot 3.4 (2024) | `webjars-locator-core` deprecated. Use `webjars-locator-lite` for version-agnostic WebJar paths. |
| HTMX 1.x | HTMX 2.x | June 2024 | Dropped IE support, improved defaults. Use 2.0.x for new projects. (HTMX 4.0 alpha exists but is not stable.) |
| `thymeleaf-extras-springsecurity5` | `thymeleaf-extras-springsecurity6` | Spring Boot 3.0 (2022) | The project already uses the correct artifact (`thymeleaf-extras-springsecurity6`). |
| Tailwind CSS 3.x (`tailwind.config.js`) | Tailwind CSS 4.x (CSS-based config) | Jan 2025 | Tailwind 4.0 changed configuration to be CSS-based. CDN approach abstracts this away. |

**Deprecated/outdated:**
- JJWT (`io.jsonwebtoken:jjwt-*`) in the current scaffold: Not deprecated as a library, but wrong for this use case. Replace with OAuth2 session-based auth.
- `spring.security.user.name` / `spring.security.user.password` in current `application.yml`: This is for basic auth fallback. Remove when switching to OAuth2.
- `jwt.secret`, `jwt.expiration`, `jwt.refresh-expiration` in current `application.yml`: Remove entirely.

## Discretion Recommendations

### Primary User Identifier: Corporate Email

**Recommendation:** Use corporate email (`email VARCHAR(255) UNIQUE`) as the primary identifier.
**Rationale:** Email is human-readable, naturally unique within @bemobi.com domain, and is the common identifier across Google Workspace and all AI tool providers. Google Workspace ID (numeric `sub` claim) is opaque and adds unnecessary indirection. Email is what admins will search for and recognize.
**Confidence:** HIGH

### AI Tool Entity Fields

**Recommendation:** The following fields for the `ai_tools` table:
- `id` (BIGSERIAL PK) -- internal ID
- `name` (VARCHAR, UNIQUE, NOT NULL) -- display name (e.g., "Claude", "GitHub Copilot")
- `tool_type` (VARCHAR, CHECK constraint) -- enum: CLAUDE, GITHUB_COPILOT, CURSOR, CUSTOM
- `description` (VARCHAR) -- optional description
- `api_base_url` (VARCHAR) -- base URL for the tool's API (informational, actual credentials in config)
- `enabled` (BOOLEAN) -- whether this tool is active for monitoring
- `icon_url` (VARCHAR) -- URL or path for tool icon in the UI
- `created_at`, `updated_at` (TIMESTAMPTZ) -- audit timestamps

**Rationale:** Intentionally minimal. Actual API credentials stay in application properties (not DB) per CONTEXT.md. The `tool_type` enum enables type-specific integration logic in later phases, while CUSTOM allows extensibility.
**Confidence:** HIGH

### Empty State Design

**Recommendation:** Show contextual empty states with clear calls-to-action:
- Dashboard: "Welcome to AI User Control. Add AI tools to get started." with button to AI Tools page
- User list: "No users discovered yet. Users will appear here once AI tool integrations are configured." (no action button -- users come from provider APIs)
- AI Tool list: "No AI tools configured yet. Add your first tool to begin monitoring." with "Add AI Tool" button

**Rationale:** Follow the Linear/Vercel pattern of clean, informative empty states with illustrations (simple SVG icons) rather than blank pages.
**Confidence:** HIGH

### Login Page Design

**Recommendation:** Centered card layout on a light background:
- Company logo or app name at top
- Brief tagline: "AI Tool Usage Management"
- Single "Sign in with Google" button (branded per Google guidelines)
- Error messages displayed inline (for domain rejection, unregistered user, etc.)
- No other form fields (Google SSO is the only auth method)

**Rationale:** Minimal and clean. One authentication method means one button. No distracting options.
**Confidence:** HIGH

## Open Questions

1. **Google OAuth2 Client Credentials**
   - What we know: Need `client-id` and `client-secret` from Google Cloud Console
   - What's unclear: Whether the Google Cloud project already exists for Bemobi, or needs to be created
   - Recommendation: Document the Google Cloud Console setup steps in a deployment guide. For development, mock the OAuth2 flow or use actual test credentials.

2. **User Discovery Timing (Phase 1 vs Phase 2+)**
   - What we know: Users come from provider APIs, not manual creation. Phase 1 builds the User entity but the table will be mostly empty.
   - What's unclear: Should the initial admin(s) bootstrapped via config also create User records, or only exist as config-level identities?
   - Recommendation: Auto-create User records for config admins on first login (shown in the code example above). This ensures the User table is populated from day one and the admin appears in the user list.

3. **HTMX Version Compatibility with htmx-spring-boot-thymeleaf**
   - What we know: htmx-spring-boot-thymeleaf 4.0.2 works with Spring Boot 3.4.x. HTMX 2.0.x is the latest stable line.
   - What's unclear: Whether htmx-spring-boot-thymeleaf 4.0.2 specifically targets HTMX 2.0 or is version-agnostic.
   - Recommendation: Use HTMX 2.0.4 (latest 2.0.x stable). The Spring Boot library primarily works with server-side headers and annotations, so HTMX JS version compatibility is unlikely to be an issue. Test during implementation.

## Sources

### Primary (HIGH confidence)
- [Spring Security OAuth2 Login Core Configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html) - OAuth2 login setup, Google provider config, SecurityFilterChain DSL
- [Spring Security OAuth2 Login Advanced Configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/advanced.html) - Custom OidcUserService, authorization request customization, success/failure handlers
- [Spring Security Session Management](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html) - Session timeout, concurrent sessions, invalidSessionUrl
- [htmx-spring-boot GitHub (wimdeblauwe)](https://github.com/wimdeblauwe/htmx-spring-boot) - @HxRequest, HtmxResponse, Thymeleaf dialect, version compatibility
- [Thymeleaf Layout Dialect](https://ultraq.github.io/thymeleaf-layout-dialect/getting-started/) - Decorator pattern, layout:decorate, layout:fragment

### Secondary (MEDIUM confidence)
- [Spring Boot and OAuth2 Guide](https://spring.io/guides/tutorials/spring-boot-oauth2/) - Official Spring guide on OAuth2 login flow
- [htmx.org Official Docs](https://htmx.org/docs/) - HTMX 2.0 attributes, hx-get, hx-swap, hx-target
- [Maciej Walkowiak - Spring Boot with Thymeleaf and Tailwind CSS](https://maciejwalkowiak.com/blog/spring-boot-thymeleaf-tailwindcss/) - Tailwind integration options (CDN vs build)
- [Maven Central - htmx-spring-boot-thymeleaf](https://mvnrepository.com/artifact/io.github.wimdeblauwe/htmx-spring-boot-thymeleaf) - Version history: 4.0.2 supports Spring Boot 3.4.x
- [Maven Central - htmx.org WebJar](https://mvnrepository.com/artifact/org.webjars.npm/htmx.org) - WebJar versions for htmx.org

### Tertiary (LOW confidence)
- htmx.org WebJar version 2.0.4 -- version number based on search results showing 2.0.x releases; exact latest patch version should be verified on Maven Central before implementation

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - All libraries verified via official Spring Security docs, Maven Central, and GitHub releases. Spring Boot 3.4.2 compatibility confirmed.
- Architecture: HIGH - OAuth2 login + custom OidcUserService is the documented, standard pattern. Thymeleaf Layout Dialect + HTMX is well-established.
- Pitfalls: HIGH - Based on official documentation warnings (hd parameter, session timeout defaults) and common community issues.

**Research date:** 2026-02-24
**Valid until:** 2026-03-24 (30 days -- stable stack, no major releases expected)
