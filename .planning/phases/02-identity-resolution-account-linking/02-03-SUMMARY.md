---
phase: 02-identity-resolution-account-linking
plan: 03
subsystem: ui, web
tags: [thymeleaf, htmx, tailwind, sync-button, user-detail, pending-accounts, sidebar-badge, controller-advice, oauth2]

# Dependency graph
requires:
  - phase: 02-identity-resolution-account-linking
    plan: 02
    provides: "SyncOrchestrator, AccountLinkingService, SyncResultResponse, GoogleWorkspaceService, ClaudeApiClient, CursorApiClient"
provides:
  - "SyncController with HTMX POST /sync endpoint triggering full sync with toast feedback"
  - "User detail page (/users/{id}) with profile data, linked accounts table, and unlink action"
  - "Contas Pendentes page (/pending-accounts) listing unmatched and offboarded tool accounts"
  - "Sidebar badge count for pending accounts via SidebarModelAdvice @ControllerAdvice"
  - "UserDetailResponse and UserAccountResponse DTOs for user detail view"
  - "PendingAccountResponse DTO for pending accounts listing"
  - "OAuth2 prompt=select_account for Google account selector on login"
affects: [03-claude-integration, 06-admin-dashboard]

# Tech tracking
tech-stack:
  added: []
  patterns: [controller-advice-global-model, htmx-post-toast-feedback, htmx-delete-table-refresh, thymeleaf-fragment-swap, sidebar-badge-count]

key-files:
  created:
    - src/main/java/com/bemobi/aiusercontrol/web/SyncController.java
    - src/main/java/com/bemobi/aiusercontrol/web/SidebarModelAdvice.java
    - src/main/java/com/bemobi/aiusercontrol/web/PendingAccountsController.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/UserDetailResponse.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/UserAccountResponse.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/PendingAccountResponse.java
    - src/main/resources/templates/fragments/sync-result.html
    - src/main/resources/templates/users/detail.html
    - src/main/resources/templates/users/fragments/accounts-table.html
    - src/main/resources/templates/pending-accounts/list.html
    - src/main/resources/templates/pending-accounts/fragments/table.html
  modified:
    - src/main/resources/templates/dashboard.html
    - src/main/resources/templates/fragments/sidebar.html
    - src/main/resources/templates/users/fragments/table.html
    - src/main/java/com/bemobi/aiusercontrol/web/DashboardController.java
    - src/main/java/com/bemobi/aiusercontrol/user/controller/UserController.java
    - src/main/java/com/bemobi/aiusercontrol/user/service/UserService.java
    - src/main/java/com/bemobi/aiusercontrol/config/SecurityConfig.java

key-decisions:
  - "SidebarModelAdvice @ControllerAdvice provides pendingAccountsCount globally for all pages"
  - "OAuth2 prompt=select_account added to SecurityConfig for Google account selector on login"
  - "Pending accounts defined as: unmatched (user=null) OR belonging to OFFBOARDED users"
  - "Unlink action uses HTMX DELETE with hx-confirm for safety"

patterns-established:
  - "ControllerAdvice for global model attributes: SidebarModelAdvice adds pendingAccountsCount to all views"
  - "HTMX POST with toast feedback: sync button triggers POST, response swaps into result container"
  - "HTMX DELETE with table refresh: unlink action deletes account and refreshes accounts-table fragment"
  - "User detail pattern: GET /users/{id} with profile section + linked accounts table"
  - "Sidebar badge count: red pill badge from ControllerAdvice-provided model attribute"

requirements-completed: [ACCT-04, GW-01, GW-02]

# Metrics
duration: 15min
completed: 2026-02-24
---

# Phase 2 Plan 3: Admin UI for Sync, User Detail, and Pending Accounts Summary

**Dashboard sync button with HTMX toast feedback, user detail page with profile and linked accounts (unlink action), Contas Pendentes page with sidebar badge count, and OAuth2 account selector**

## Performance

- **Duration:** 15 min
- **Started:** 2026-02-24T21:17:29Z
- **Completed:** 2026-02-24T21:32:21Z
- **Tasks:** 4 (3 auto + 1 checkpoint)
- **Files modified:** 18

## Accomplishments
- Dashboard "Sincronizar" button triggers full GWS + Claude + Cursor sync via HTMX POST with toast showing counts (novos, atualizados, offboarded, vinculadas, sem correspondencia)
- User detail page at /users/{id} shows profile (avatar, name, email, department, status, github_username, dates) and linked tool accounts table with unlink action
- Contas Pendentes page at /pending-accounts lists all unmatched tool accounts and accounts of OFFBOARDED users with reason badges
- Sidebar "Contas Pendentes" link with red badge count provided globally by SidebarModelAdvice @ControllerAdvice
- OAuth2 login now shows Google account selector (prompt=select_account) for multi-account environments

## Task Commits

Each task was committed atomically:

1. **Task 1: Add sync button to dashboard with HTMX feedback, and create SyncController** - `596d803` (feat)
2. **Task 2: Create user detail page with profile data and linked accounts, plus unlink action** - `feee0ba` (feat)
3. **Task 3: Create "Contas Pendentes" page with sidebar navigation and badge count** - `2c7c605` (feat)
4. **Task 4: Verify complete sync flow and UI** - checkpoint:human-verify (passed)
   - Additional fix committed: `5061a83` (fix) - add prompt=select_account to OAuth2 login

## Files Created/Modified
- `src/main/java/com/bemobi/aiusercontrol/web/SyncController.java` - POST /sync endpoint triggering full sync, returns toast fragment
- `src/main/java/com/bemobi/aiusercontrol/web/SidebarModelAdvice.java` - @ControllerAdvice providing pendingAccountsCount to all views
- `src/main/java/com/bemobi/aiusercontrol/web/PendingAccountsController.java` - GET /pending-accounts listing unmatched and offboarded accounts
- `src/main/java/com/bemobi/aiusercontrol/dto/response/UserDetailResponse.java` - DTO with profile data and nested account list
- `src/main/java/com/bemobi/aiusercontrol/dto/response/UserAccountResponse.java` - DTO for linked tool account in user detail
- `src/main/java/com/bemobi/aiusercontrol/dto/response/PendingAccountResponse.java` - DTO for pending accounts listing
- `src/main/resources/templates/fragments/sync-result.html` - Toast/banner fragment for sync result feedback
- `src/main/resources/templates/users/detail.html` - User detail page with profile and accounts table
- `src/main/resources/templates/users/fragments/accounts-table.html` - HTMX-refreshable accounts table with unlink action
- `src/main/resources/templates/pending-accounts/list.html` - Contas Pendentes page layout
- `src/main/resources/templates/pending-accounts/fragments/table.html` - Pending accounts table with reason badges
- `src/main/resources/templates/dashboard.html` - Added sync button with HTMX post and spinner
- `src/main/resources/templates/fragments/sidebar.html` - Added Contas Pendentes link with badge count
- `src/main/resources/templates/users/fragments/table.html` - User rows now clickable links to detail page
- `src/main/java/com/bemobi/aiusercontrol/web/DashboardController.java` - Updated for sync integration
- `src/main/java/com/bemobi/aiusercontrol/user/controller/UserController.java` - Added GET /users/{id} and DELETE unlink endpoint
- `src/main/java/com/bemobi/aiusercontrol/user/service/UserService.java` - Added getUserDetail method
- `src/main/java/com/bemobi/aiusercontrol/config/SecurityConfig.java` - Added prompt=select_account to OAuth2 login

## Decisions Made
- SidebarModelAdvice @ControllerAdvice chosen over per-controller @ModelAttribute for global sidebar badge count -- cleaner, avoids duplication
- OAuth2 prompt=select_account added during checkpoint verification to improve multi-account login UX
- Pending accounts defined as union of: accounts with user=null (unmatched) and accounts where user.status=OFFBOARDED
- Unlink action uses HTMX DELETE with hx-confirm dialog for safety before removing account link

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Added prompt=select_account to OAuth2 login**
- **Found during:** Task 4 (human verification checkpoint)
- **Issue:** Google OAuth2 login always auto-selected the first Google account without showing the account picker, problematic for admins with multiple Google accounts
- **Fix:** Added `prompt=select_account` to the OAuth2 authorization request customizer in SecurityConfig
- **Files modified:** `src/main/java/com/bemobi/aiusercontrol/config/SecurityConfig.java`
- **Verification:** User confirmed account selector appears on login
- **Committed in:** `5061a83`

---

**Total deviations:** 1 auto-fixed (1 bug fix)
**Impact on plan:** OAuth2 prompt fix is a UX improvement discovered during verification. No scope creep.

## Issues Encountered
None

## User Setup Required
None - all features work with existing configuration from Plans 01 and 02. Google Workspace, Claude, and Cursor integrations remain conditional/optional.

## Next Phase Readiness
- Phase 2 fully complete: identity resolution data model, sync service layer, and admin UI all delivered
- Ready for Phase 3 (Metrics Pipeline & Claude Integration): SyncOrchestrator, scheduling infrastructure, and UI feedback patterns are established
- Admin dashboard foundation (sidebar, layout, HTMX patterns) ready for Phase 6 enrichment
- All GW-* and ACCT-* requirements for Phase 2 are complete

## Self-Check: PASSED

All 11 created files verified present. All 7 modified files verified present. All 4 task commits verified in git log.

---
*Phase: 02-identity-resolution-account-linking*
*Completed: 2026-02-24*
