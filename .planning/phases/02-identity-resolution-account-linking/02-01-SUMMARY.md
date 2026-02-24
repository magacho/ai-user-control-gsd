---
phase: 02-identity-resolution-account-linking
plan: 01
subsystem: database, auth, config
tags: [google-admin-sdk, flyway, jpa, google-workspace, account-linking]

# Dependency graph
requires:
  - phase: 01-auth-user-management
    provides: "User/AITool entities, Flyway V1-V4 migrations, OIDC auth, AI Tool CRUD"
provides:
  - "UserAIToolAccount JPA entity with ManyToOne to User and AITool"
  - "AccountStatus enum (ACTIVE, SUSPENDED, REVOKED)"
  - "UserAIToolAccountRepository with pending/orphaned account queries"
  - "V5 Flyway migration for account linking table, github_username, credential columns"
  - "Google Admin SDK dependency and GoogleWorkspaceConfig with startup validation"
  - "AI Tool CRUD form with API Key and Organization ID fields"
  - "User.githubUsername field for Google Workspace identity resolution"
  - "AITool.apiKey and AITool.apiOrgId credential storage"
affects: [02-identity-resolution-account-linking, 03-claude-integration, 04-github-copilot-integration]

# Tech tracking
tech-stack:
  added: [google-api-services-admin-directory, google-auth-library-oauth2-http]
  patterns: [service-account-delegation, credential-masking, conditional-bean-configuration]

key-files:
  created:
    - src/main/resources/db/migration/V5__add_credentials_and_github_username.sql
    - src/main/java/com/bemobi/aiusercontrol/enums/AccountStatus.java
    - src/main/java/com/bemobi/aiusercontrol/model/entity/UserAIToolAccount.java
    - src/main/java/com/bemobi/aiusercontrol/user/repository/UserAIToolAccountRepository.java
    - src/main/java/com/bemobi/aiusercontrol/config/GoogleWorkspaceConfig.java
  modified:
    - pom.xml
    - src/main/resources/application.yml
    - src/main/java/com/bemobi/aiusercontrol/config/AppProperties.java
    - src/main/java/com/bemobi/aiusercontrol/model/entity/User.java
    - src/main/java/com/bemobi/aiusercontrol/model/entity/AITool.java
    - src/main/java/com/bemobi/aiusercontrol/dto/request/AIToolRequest.java
    - src/main/java/com/bemobi/aiusercontrol/dto/response/AIToolResponse.java
    - src/main/java/com/bemobi/aiusercontrol/aitool/service/AIToolService.java
    - src/main/java/com/bemobi/aiusercontrol/aitool/controller/AIToolController.java
    - src/main/resources/templates/ai-tools/fragments/form-modal.html

key-decisions:
  - "V5 migration uses CREATE TABLE IF NOT EXISTS since V3 stub was empty and already applied by Flyway"
  - "API key masked in responses as ****<last4> for security"
  - "Update endpoint preserves existing API key when blank value submitted (leave blank to keep current)"
  - "GoogleWorkspaceConfig uses @ConditionalOnProperty to avoid loading when disabled"
  - "UserAIToolAccount has unique constraint on (ai_tool_id, account_identifier) for deduplication"

patterns-established:
  - "Credential masking: API keys shown as ****<last4> in UI/API responses"
  - "Conditional config: @ConditionalOnProperty with @PostConstruct validation for fail-fast"
  - "Account linking: UserAIToolAccount bridges User and AITool with status tracking"

requirements-completed: [GW-01, GW-03, GW-05, ACCT-06, ACCT-07]

# Metrics
duration: 5min
completed: 2026-02-24
---

# Phase 2 Plan 1: Data Foundation Summary

**Google Admin SDK integration, V5 migration for account linking and credentials, UserAIToolAccount entity with status tracking, and AI Tool CRUD form with API key/org ID fields**

## Performance

- **Duration:** 5 min
- **Started:** 2026-02-24T20:59:03Z
- **Completed:** 2026-02-24T21:04:20Z
- **Tasks:** 3
- **Files modified:** 15

## Accomplishments
- Established data model for identity resolution: UserAIToolAccount entity linking users to AI tool accounts with ACTIVE/SUSPENDED/REVOKED status
- Added Google Admin SDK dependency and configuration with startup validation (fail-fast if misconfigured)
- Extended AI Tool CRUD to support credential storage (API key masked, Organization ID visible)
- Added github_username field to User entity for Google Workspace identity matching

## Task Commits

Each task was committed atomically:

1. **Task 1: Add Google Admin SDK dependency, Flyway V5 migration, and configuration** - `06acf77` (feat)
2. **Task 2: Implement UserAIToolAccount entity, AccountStatus enum, and repository** - `9ecc138` (feat)
3. **Task 3: Update AITool DTOs and CRUD form with credential fields** - `cf9d267` (feat)

## Files Created/Modified
- `pom.xml` - Added Google Admin SDK and Auth library dependencies
- `src/main/resources/db/migration/V5__add_credentials_and_github_username.sql` - DDL for user_ai_tool_accounts table, github_username column, credential columns
- `src/main/resources/application.yml` - Added google-workspace config section with env var overrides
- `src/main/java/com/bemobi/aiusercontrol/config/AppProperties.java` - Added GoogleWorkspace nested config class
- `src/main/java/com/bemobi/aiusercontrol/config/GoogleWorkspaceConfig.java` - Directory bean with @PostConstruct startup validation
- `src/main/java/com/bemobi/aiusercontrol/enums/AccountStatus.java` - ACTIVE, SUSPENDED, REVOKED enum
- `src/main/java/com/bemobi/aiusercontrol/model/entity/UserAIToolAccount.java` - JPA entity with ManyToOne to User and AITool
- `src/main/java/com/bemobi/aiusercontrol/user/repository/UserAIToolAccountRepository.java` - Queries for pending, orphaned, and status-based lookups
- `src/main/java/com/bemobi/aiusercontrol/model/entity/User.java` - Added githubUsername field
- `src/main/java/com/bemobi/aiusercontrol/model/entity/AITool.java` - Added apiKey and apiOrgId fields
- `src/main/java/com/bemobi/aiusercontrol/dto/request/AIToolRequest.java` - Added apiKey and apiOrgId fields
- `src/main/java/com/bemobi/aiusercontrol/dto/response/AIToolResponse.java` - Added masked apiKey, apiOrgId, hasApiKey fields
- `src/main/java/com/bemobi/aiusercontrol/aitool/service/AIToolService.java` - Credential mapping and API key masking logic
- `src/main/java/com/bemobi/aiusercontrol/aitool/controller/AIToolController.java` - Passes hasApiKey flag to edit form
- `src/main/resources/templates/ai-tools/fragments/form-modal.html` - API Key (password) and Organization ID fields in create/edit forms

## Decisions Made
- V5 migration uses `CREATE TABLE IF NOT EXISTS` since V3 empty stub was already applied by Flyway
- API key masked in responses as `****<last4>` for security (never expose full key to frontend)
- Update endpoint preserves existing API key when submitted value is blank (avoids accidental deletion)
- GoogleWorkspaceConfig uses `@ConditionalOnProperty` so app starts normally when GWS is disabled
- UserAIToolAccount has unique constraint on `(ai_tool_id, account_identifier)` for account deduplication

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - Google Workspace integration is disabled by default (GWS_ENABLED=false). Configuration required when enabling:
- `GWS_ENABLED=true`
- `GWS_SERVICE_ACCOUNT_KEY_PATH=/path/to/service-account.json`
- `GWS_DELEGATED_ADMIN_EMAIL=admin@bemobi.com`
- `GWS_DOMAIN=bemobi.com`

## Next Phase Readiness
- Data model ready for Plan 02 (Google Workspace sync service) to query users and create/update UserAIToolAccount records
- GoogleWorkspaceConfig provides the Directory bean that sync service will inject
- Repository queries (findPendingAccounts, findByAiToolAndAccountIdentifier) ready for sync logic
- Credential fields allow Plan 03 (UI) to display account linking status per tool

## Self-Check: PASSED

All 6 key files verified present. All 3 task commits verified in git log.

---
*Phase: 02-identity-resolution-account-linking*
*Completed: 2026-02-24*
