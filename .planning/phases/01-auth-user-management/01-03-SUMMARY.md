---
phase: 01-auth-user-management
plan: 03
subsystem: ui, web
tags: [thymeleaf, htmx, tailwind, layout-dialect, sidebar, navbar, crud, dashboard]

# Dependency graph
requires:
  - phase: 01-02
    provides: "SecurityConfig with OAuth2 login, CustomOidcUserService, login page, ADMIN role enforcement"
provides:
  - Thymeleaf Layout Dialect base template with sidebar + navbar + content area
  - Sidebar navigation with Dashboard, Users, AI Tools links and active state detection
  - Navbar with Google avatar, user name, and logout button from OAuth2 principal
  - Dashboard page with stats cards (user count, tool count) and empty state CTA
  - Read-only user list page with HTMX-loaded table and empty state messaging
  - AI Tool catalog with full CRUD (create, edit, delete) via HTMX modal forms
  - UserService (read-only), AIToolService (full CRUD with name uniqueness validation)
  - UserController (GET-only), AIToolController (full CRUD with @HxRequest)
  - DTOs: UserResponse, AIToolResponse, AIToolRequest with Jakarta Validation
affects: [02-identity-resolution, 03-claude-integration]

# Tech tracking
tech-stack:
  added: []
  patterns: [Thymeleaf Layout Dialect decorator pattern, HTMX partial updates with fragments, CSRF token injection via htmx:configRequest, modal overlay pattern for CRUD forms]

key-files:
  created:
    - src/main/resources/templates/layout/default.html
    - src/main/resources/templates/fragments/sidebar.html
    - src/main/resources/templates/fragments/navbar.html
    - src/main/resources/templates/dashboard.html
    - src/main/resources/static/css/custom.css
    - src/main/java/com/bemobi/aiusercontrol/web/DashboardController.java
    - src/main/java/com/bemobi/aiusercontrol/config/WebConfig.java
    - src/main/resources/templates/users/list.html
    - src/main/resources/templates/users/fragments/table.html
    - src/main/resources/templates/ai-tools/list.html
    - src/main/resources/templates/ai-tools/form.html
    - src/main/resources/templates/ai-tools/fragments/table.html
    - src/main/resources/templates/ai-tools/fragments/form-modal.html
    - src/main/java/com/bemobi/aiusercontrol/user/controller/UserController.java
    - src/main/java/com/bemobi/aiusercontrol/user/service/UserService.java
    - src/main/java/com/bemobi/aiusercontrol/aitool/controller/AIToolController.java
    - src/main/java/com/bemobi/aiusercontrol/aitool/service/AIToolService.java
    - src/main/java/com/bemobi/aiusercontrol/dto/request/AIToolRequest.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/AIToolResponse.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/UserResponse.java
  modified:
    - src/main/resources/templates/index.html

key-decisions:
  - "CSRF token injected via htmx:configRequest event listener in base layout for all HTMX requests"
  - "HTMX modal overlay pattern: form-modal fragments loaded into #modal-container div, dismissed via JavaScript"
  - "UserResponse includes pre-formatted lastLoginFormatted field to avoid Thymeleaf #temporals.format issues with Instant"
  - "AI tool delete returns refreshed table fragment instead of empty response for simpler client-side handling"
  - "No Lombok used in any DTO/service/controller per CLAUDE.md -- all explicit builders, getters, setters"

patterns-established:
  - "Layout decorator pattern: all pages use layout:decorate='~{layout/default}' with layout:fragment='content'"
  - "HTMX fragment pattern: controllers return 'path/fragments/name :: fragmentName' for partial updates"
  - "HTMX modal pattern: GET endpoint loads form into #modal-container, POST/PUT targets #tool-table for refresh"
  - "Sidebar active state: th:classappend with #httpServletRequest.requestURI comparison"
  - "DTO builder pattern: manual Builder inner class on all response DTOs (no Lombok)"

requirements-completed: [USER-01]

# Metrics
duration: 7min
completed: 2026-02-24
---

# Phase 1 Plan 03: Admin Dashboard UI Summary

**Thymeleaf layout with dark sidebar navigation, Google avatar navbar, read-only user list with empty state, and AI tool CRUD via HTMX modal forms with Tailwind styling**

## Performance

- **Duration:** 7 min
- **Started:** 2026-02-24T17:41:35Z
- **Completed:** 2026-02-24T17:48:41Z
- **Tasks:** 2
- **Files modified:** 22

## Accomplishments
- Built shared layout template with Thymeleaf Layout Dialect: dark sidebar (Dashboard, Users, AI Tools navigation with active state), top navbar (Google avatar, user name, logout), and content area
- Implemented read-only user list page with HTMX-loaded data table, avatar display, status badges (Active/Inactive/Offboarded), and informative empty state
- Created full AI tool CRUD with HTMX: add/edit via modal overlay forms, delete with confirmation, inline validation errors, and table refresh without page reloads
- Wired DashboardController to UserService and AIToolService for real-time stats on dashboard landing page

## Task Commits

Each task was committed atomically:

1. **Task 1: Create shared layout, sidebar navigation, navbar with Google avatar, and dashboard page** - `1fb0041` (feat)
2. **Task 2: Implement read-only user list and AI Tool CRUD with HTMX** - `28b4ecf` (feat)

## Files Created/Modified
- `src/main/resources/templates/layout/default.html` - Base layout with sidebar + navbar + content area, CSRF meta tags, HTMX CSRF injection
- `src/main/resources/templates/fragments/sidebar.html` - Dark sidebar with Dashboard/Users/AI Tools nav links and active state detection
- `src/main/resources/templates/fragments/navbar.html` - Top navbar with Google avatar, name from OAuth2 principal, logout form
- `src/main/resources/templates/dashboard.html` - Dashboard with stats cards (user count, tool count, active accounts) and empty state CTA
- `src/main/resources/templates/index.html` - Redirect to /dashboard
- `src/main/resources/static/css/custom.css` - HTMX swap transitions, modal animations, loading indicator styles
- `src/main/java/com/bemobi/aiusercontrol/web/DashboardController.java` - Root redirect + dashboard with real counts from services
- `src/main/java/com/bemobi/aiusercontrol/config/WebConfig.java` - WebJars resource handler configuration
- `src/main/resources/templates/users/list.html` - User list page with count badge and HTMX-loaded table
- `src/main/resources/templates/users/fragments/table.html` - User data table fragment with avatar, status badges, empty state
- `src/main/resources/templates/ai-tools/list.html` - AI tool list with Add button, HTMX table, modal container
- `src/main/resources/templates/ai-tools/form.html` - Redirect page (forms handled via HTMX modal)
- `src/main/resources/templates/ai-tools/fragments/table.html` - AI tool table with edit/delete actions, empty state with add CTA
- `src/main/resources/templates/ai-tools/fragments/form-modal.html` - Modal overlay form for create/edit with validation display
- `src/main/java/com/bemobi/aiusercontrol/user/controller/UserController.java` - GET-only endpoints (list, table fragment) -- no POST/PUT/DELETE
- `src/main/java/com/bemobi/aiusercontrol/user/service/UserService.java` - Read-only service (findAll, findById, count)
- `src/main/java/com/bemobi/aiusercontrol/aitool/controller/AIToolController.java` - Full CRUD with @HxRequest on all HTMX endpoints
- `src/main/java/com/bemobi/aiusercontrol/aitool/service/AIToolService.java` - CRUD service with name uniqueness validation
- `src/main/java/com/bemobi/aiusercontrol/dto/request/AIToolRequest.java` - Request DTO with @NotBlank name and @NotNull toolType
- `src/main/java/com/bemobi/aiusercontrol/dto/response/AIToolResponse.java` - Response DTO with builder pattern
- `src/main/java/com/bemobi/aiusercontrol/dto/response/UserResponse.java` - Response DTO with builder pattern and lastLoginFormatted

## Decisions Made
- **CSRF token injection for HTMX:** Added `htmx:configRequest` event listener in base layout that reads CSRF token from meta tags and adds it to all HTMX request headers. This ensures all HTMX POST/PUT/DELETE requests include the CSRF token automatically.
- **Modal overlay pattern:** AI tool forms are loaded as HTMX fragments into a `#modal-container` div. The overlay is dismissed via JavaScript `onclick` on the backdrop or close button. On successful submit, the response replaces `#tool-table` content with the refreshed table.
- **Delete returns full table:** Instead of returning empty response and relying on `hx-swap="outerHTML"` on the row, delete endpoint returns the full refreshed table fragment targeting `#tool-table`. This is simpler and handles the empty state transition correctly.
- **Pre-formatted date in DTO:** Added `getLastLoginFormatted()` to UserResponse that formats Instant to "MMM d, yyyy HH:mm" using system timezone, avoiding Thymeleaf `#temporals.format` incompatibility with `java.time.Instant`.
- **No Lombok compliance:** All DTOs, services, and controllers use explicit constructors, builders, getters, and setters per CLAUDE.md project guidelines.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed Instant date formatting in user table template**
- **Found during:** Task 2 (user table template creation)
- **Issue:** Thymeleaf `#temporals.format()` does not support `java.time.Instant` directly (requires a `ZonedDateTime` or `LocalDateTime`). The user table template would throw a runtime error when rendering users with non-null `lastLoginAt`.
- **Fix:** Added `getLastLoginFormatted()` method to `UserResponse` that uses `DateTimeFormatter` with `ZoneId.systemDefault()` to pre-format the date. Template uses `${user.lastLoginFormatted}` instead of `#temporals.format()`.
- **Files modified:** `src/main/java/com/bemobi/aiusercontrol/dto/response/UserResponse.java`, `src/main/resources/templates/users/fragments/table.html`
- **Verification:** `mvn compile -DskipTests` succeeds
- **Committed in:** `28b4ecf` (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Bug fix necessary to prevent runtime template rendering error. No scope creep.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required for UI layer.

## Next Phase Readiness
- Phase 1 complete: OAuth2 authentication (Plan 02) + admin dashboard UI (Plan 03) are fully integrated
- All protected pages require ROLE_ADMIN via SecurityConfig
- Dashboard shows real counts from UserService and AIToolService
- AI tool catalog is ready for Phase 3 (Claude integration) to register tool definitions
- User list is ready for Phase 2 (identity resolution) to populate users from Google Workspace

## Self-Check: PASSED
