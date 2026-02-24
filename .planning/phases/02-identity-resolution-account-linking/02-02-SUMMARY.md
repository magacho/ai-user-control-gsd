---
phase: 02-identity-resolution-account-linking
plan: 02
subsystem: integration, service, sync
tags: [google-workspace, google-admin-sdk, claude-api, cursor-api, account-linking, email-matching, sync-orchestration, webclient]

# Dependency graph
requires:
  - phase: 02-identity-resolution-account-linking
    plan: 01
    provides: "UserAIToolAccount entity, AccountStatus enum, UserAIToolAccountRepository, GoogleWorkspaceConfig, AITool credentials"
provides:
  - "GoogleWorkspaceService with paginated GWS directory fetch and github_username extraction"
  - "ClaudeApiClient fetching organization members via Anthropic Admin API"
  - "CursorApiClient fetching organization members via Cursor API"
  - "AccountLinkingService auto-linking tool accounts to users by email matching"
  - "Account status transitions: ACTIVE -> SUSPENDED -> REVOKED on disappearance"
  - "SyncOrchestrator orchestrating GWS sync then Claude/Cursor account linking"
  - "SyncResultResponse DTO with all sync count fields for UI feedback"
  - "ToolAccountInfo shared DTO for tool account user data"
  - "14 unit tests covering all sync and linking paths"
affects: [02-identity-resolution-account-linking, 03-claude-integration, 07-cursor-integration]

# Tech tracking
tech-stack:
  added: [spring-webflux-webclient]
  patterns: [paginated-api-fetch, email-based-account-linking, status-transition-state-machine, optional-bean-injection, sync-orchestration]

key-files:
  created:
    - src/main/java/com/bemobi/aiusercontrol/integration/google/GoogleWorkspaceService.java
    - src/main/java/com/bemobi/aiusercontrol/integration/claude/ClaudeApiClient.java
    - src/main/java/com/bemobi/aiusercontrol/integration/cursor/CursorApiClient.java
    - src/main/java/com/bemobi/aiusercontrol/service/AccountLinkingService.java
    - src/main/java/com/bemobi/aiusercontrol/service/SyncOrchestrator.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/SyncResultResponse.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/ToolAccountInfo.java
    - src/main/java/com/bemobi/aiusercontrol/exception/ResourceNotFoundException.java
    - src/main/resources/db/migration/V6__make_user_id_nullable_for_unmatched_accounts.sql
    - src/test/java/com/bemobi/aiusercontrol/service/SyncOrchestratorTest.java
    - src/test/java/com/bemobi/aiusercontrol/service/AccountLinkingServiceTest.java
  modified:
    - src/main/java/com/bemobi/aiusercontrol/config/AppProperties.java
    - src/main/java/com/bemobi/aiusercontrol/model/entity/UserAIToolAccount.java

key-decisions:
  - "V6 migration makes user_id nullable to support unmatched tool accounts (user=null for non-corporate emails)"
  - "Account disappearance: ACTIVE -> SUSPENDED on first miss, SUSPENDED -> REVOKED on second consecutive miss"
  - "GoogleWorkspaceService uses customSchemaName config (default 'Custom') for github_username extraction"
  - "ClaudeApiClient returns empty list on HTTP error (does not crash sync)"
  - "SyncOrchestrator uses @Autowired(required=false) for optional integration beans"
  - "GitHub Copilot skipped during sync (deferred to Phase 4 per CONTEXT.md)"

patterns-established:
  - "Paginated API fetch: loop with nextPageToken until no more pages"
  - "Email-based auto-linking: match tool account email to corporate user email (case-insensitive)"
  - "Status transition state machine: ACTIVE -> SUSPENDED -> REVOKED on consecutive disappearances"
  - "Optional bean injection: @Autowired(required=false) with setter methods for testability"
  - "Sync orchestration: GWS phase first (identity source of truth), then tool linking phase"
  - "Graceful error handling: catch per-tool exceptions, add to errors list, continue with remaining tools"

requirements-completed: [GW-02, GW-04, ACCT-01, ACCT-02, ACCT-03, ACCT-05]

# Metrics
duration: 6min
completed: 2026-02-24
---

# Phase 2 Plan 2: Sync Service Layer Summary

**Google Workspace paginated directory sync, Claude/Cursor API user-list clients, email-based auto-linking with ACTIVE->SUSPENDED->REVOKED state machine, and sync orchestration with 14 unit tests**

## Performance

- **Duration:** 6 min
- **Started:** 2026-02-24T21:06:48Z
- **Completed:** 2026-02-24T21:12:30Z
- **Tasks:** 2
- **Files modified:** 13

## Accomplishments
- GoogleWorkspaceService fetches all GWS users with pagination (500 per page) and extracts github_username from custom schemas
- ClaudeApiClient and CursorApiClient fetch organization member lists via WebClient with pagination and graceful error handling
- AccountLinkingService auto-links tool accounts to corporate users by @bemobi.com email matching, with status transition state machine (ACTIVE -> SUSPENDED -> REVOKED)
- SyncOrchestrator executes full sync: GWS identity sync then Claude + Cursor account linking, with error isolation per tool
- 14 unit tests pass covering new user creation, updates, offboarding, account linking, unmatched accounts, suspended/revoked transitions, error handling, and account unlinking

## Task Commits

Each task was committed atomically:

1. **Task 1: Implement GoogleWorkspaceService, Claude/Cursor API clients, and AccountLinkingService** - `fab08f0` (feat)
2. **Task 2: Implement SyncOrchestrator and unit tests for sync business logic** - `9d75c98` (feat)

## Files Created/Modified
- `src/main/java/com/bemobi/aiusercontrol/integration/google/GoogleWorkspaceService.java` - GWS API client with paginated user directory fetch and github_username extraction
- `src/main/java/com/bemobi/aiusercontrol/integration/claude/ClaudeApiClient.java` - Claude organization member list via Anthropic Admin API
- `src/main/java/com/bemobi/aiusercontrol/integration/cursor/CursorApiClient.java` - Cursor organization member list via Cursor API
- `src/main/java/com/bemobi/aiusercontrol/service/AccountLinkingService.java` - Auto-links tool accounts to users by email, manages status transitions, supports unlinking
- `src/main/java/com/bemobi/aiusercontrol/service/SyncOrchestrator.java` - Orchestrates GWS sync + Claude/Cursor account linking
- `src/main/java/com/bemobi/aiusercontrol/dto/response/SyncResultResponse.java` - Sync result DTO with all count fields
- `src/main/java/com/bemobi/aiusercontrol/dto/response/ToolAccountInfo.java` - Shared DTO for tool account user data
- `src/main/java/com/bemobi/aiusercontrol/exception/ResourceNotFoundException.java` - Exception for unlinkAccount not-found case
- `src/main/resources/db/migration/V6__make_user_id_nullable_for_unmatched_accounts.sql` - Makes user_id nullable for unmatched accounts
- `src/main/java/com/bemobi/aiusercontrol/config/AppProperties.java` - Added customSchemaName property for GWS github_username
- `src/main/java/com/bemobi/aiusercontrol/model/entity/UserAIToolAccount.java` - Made user_id nullable in JPA mapping
- `src/test/java/com/bemobi/aiusercontrol/service/SyncOrchestratorTest.java` - 7 test cases for sync orchestration
- `src/test/java/com/bemobi/aiusercontrol/service/AccountLinkingServiceTest.java` - 7 test cases for account linking logic

## Decisions Made
- V6 migration makes `user_id` nullable to support unmatched tool accounts (accounts with non-@bemobi.com emails are tracked with `user=null`)
- Account disappearance strategy: ACTIVE -> SUSPENDED on first miss, SUSPENDED -> REVOKED on second consecutive miss (per CONTEXT.md Claude's Discretion)
- GoogleWorkspaceService uses configurable `customSchemaName` (default: "Custom") for github_username custom schema extraction
- ClaudeApiClient and CursorApiClient return empty list on HTTP errors (graceful degradation, sync continues)
- SyncOrchestrator uses `@Autowired(required=false)` for optional beans with package-private setter methods for testability
- GitHub Copilot explicitly skipped during sync (deferred to Phase 4 per CONTEXT.md)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Made user_id nullable in UserAIToolAccount for unmatched accounts**
- **Found during:** Task 1 (AccountLinkingService implementation)
- **Issue:** V5 migration defined `user_id BIGINT NOT NULL` and JPA mapping had `nullable = false`, but the plan requires unmatched accounts with `user=null`
- **Fix:** Created V6 migration to `ALTER COLUMN user_id DROP NOT NULL` and updated JPA `@JoinColumn` to remove `nullable = false`
- **Files modified:** `src/main/resources/db/migration/V6__make_user_id_nullable_for_unmatched_accounts.sql`, `src/main/java/com/bemobi/aiusercontrol/model/entity/UserAIToolAccount.java`
- **Verification:** Compile succeeds, AccountLinkingServiceTest confirms unmatched accounts created with `user=null`
- **Committed in:** `fab08f0` (Task 1 commit)

**2. [Rule 3 - Blocking] Implemented ResourceNotFoundException**
- **Found during:** Task 1 (AccountLinkingService needs it for unlinkAccount)
- **Issue:** `ResourceNotFoundException.java` was an empty stub file with no content
- **Fix:** Implemented the exception class with message constructor and (resourceName, id) constructor
- **Files modified:** `src/main/java/com/bemobi/aiusercontrol/exception/ResourceNotFoundException.java`
- **Verification:** Compile succeeds, AccountLinkingServiceTest confirms exception thrown on invalid ID
- **Committed in:** `fab08f0` (Task 1 commit)

---

**Total deviations:** 2 auto-fixed (1 bug fix, 1 blocking)
**Impact on plan:** Both auto-fixes necessary for correctness. The nullable user_id is required by the plan's own specification (unmatched accounts with user=null). No scope creep.

## Issues Encountered
None

## User Setup Required
None - Google Workspace, Claude, and Cursor integrations are all disabled by default. Configuration was established in Plan 01.

## Next Phase Readiness
- Sync service layer complete: SyncOrchestrator ready to be triggered from UI (Plan 03: Sync trigger button, Contas Pendentes page)
- SyncResultResponse provides all count fields needed for UI toast feedback ("X novos, Y atualizados, Z offboarded")
- AccountLinkingService.unlinkAccount ready for admin unlink action in UI
- GoogleWorkspaceService, ClaudeApiClient, CursorApiClient all conditional - app functions normally when integrations disabled

## Self-Check: PASSED

All 11 created files verified present. All 2 task commits verified in git log.

---
*Phase: 02-identity-resolution-account-linking*
*Completed: 2026-02-24*
