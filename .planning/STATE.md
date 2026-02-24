# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-02-24)

**Core value:** Transparent AI tool costs with zero wasted spending on orphaned accounts
**Current focus:** Phase 2: Identity Resolution & Account Linking

## Current Position

Phase: 2 of 8 (Identity Resolution & Account Linking)
Plan: 2 of 3 in current phase
Status: Executing
Last activity: 2026-02-24 — Completed 02-02-PLAN.md (Sync Service Layer)

Progress: [████░░░░░░] 25%

## Performance Metrics

**Velocity:**
- Total plans completed: 5
- Average duration: 5min
- Total execution time: 0.42 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 Auth & User Management | 3/3 | 14min | 4.7min |
| 02 Identity Resolution | 2/3 | 11min | 5.5min |

**Recent Trend:**
- Last 5 plans: 01-01 (3min), 01-02 (4min), 01-03 (7min), 02-01 (5min), 02-02 (6min)
- Trend: Steady

*Updated after each plan completion*

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [Roadmap]: 8-phase structure derived from 74 requirements across 12 categories
- [Roadmap]: Claude integration first (Phase 3) to validate pipeline before GitHub/Cursor
- [Roadmap]: Cursor deferred to Phase 7 due to API instability risk; CSV fallback planned
- [Phase 1 Context]: Platform is admin-only forever — Phase 8 (Developer Self-Service) dropped
- [Phase 1 Context]: Platform is read-only dashboard + history — no user CRUD operations
- [Phase 1 Context]: Users discovered from provider APIs, not manually created
- [Phase 1 Context]: Google SSO only, admin-only access, config-based admin bootstrap
- [Phase 1 Context]: AI tool catalog is extensible via database + UI (admin creates tools)
- [Phase 1 Context]: "Inactive" = exists in AI tools but NOT in Google Workspace (orphaned account)
- [01-01]: No Lombok per CLAUDE.md — all entities use explicit getters/setters/constructors/builders
- [01-01]: Manual builder pattern added to JPA entities for convenient construction
- [01-01]: Lombok dependency and lombok.config removed entirely from project
- [01-02]: Protected method extraction (loadOidcUser) for testability of OIDC parent class calls
- [01-02]: Anonymous subclass pattern for unit testing without complex Mockito spy setup
- [01-02]: All users assigned ROLE_ADMIN (admin-only platform, no developer role per CONTEXT.md)
- [01-03]: CSRF token injected via htmx:configRequest for all HTMX POST/PUT/DELETE requests
- [01-03]: HTMX modal overlay pattern for AI tool CRUD forms
- [01-03]: UserResponse pre-formatted date field to avoid Thymeleaf #temporals.format Instant incompatibility
- [01-03]: No Lombok in DTOs/services/controllers per CLAUDE.md (explicit builders, getters, setters)
- [02-01]: V5 migration uses CREATE TABLE IF NOT EXISTS since V3 stub was empty and already applied by Flyway
- [02-01]: API key masked in responses as ****<last4> for security
- [02-01]: Update endpoint preserves existing API key when blank value submitted
- [02-01]: GoogleWorkspaceConfig uses @ConditionalOnProperty for conditional loading
- [02-01]: UserAIToolAccount has unique constraint on (ai_tool_id, account_identifier) for deduplication
- [02-02]: V6 migration makes user_id nullable for unmatched tool accounts (user=null for non-corporate emails)
- [02-02]: Account disappearance: ACTIVE -> SUSPENDED on first miss, SUSPENDED -> REVOKED on second consecutive miss
- [02-02]: GoogleWorkspaceService uses configurable customSchemaName (default 'Custom') for github_username
- [02-02]: ClaudeApiClient/CursorApiClient return empty list on HTTP errors (graceful degradation)
- [02-02]: SyncOrchestrator uses @Autowired(required=false) with setter methods for optional beans and testability
- [02-02]: GitHub Copilot explicitly skipped during sync (deferred to Phase 4)

### Pending Todos

None yet.

### Blockers/Concerns

- [Phase 2]: Verify Google Workspace Admin SDK access and exact custom schema field name for github_username before planning
- [Phase 7]: Cursor API availability is unknown; must research before Phase 7 planning (may pivot to CSV-only)

## Session Continuity

Last session: 2026-02-24
Stopped at: Completed 02-02-PLAN.md (Sync Service Layer)
Resume file: .planning/phases/02-identity-resolution-account-linking/02-02-SUMMARY.md
