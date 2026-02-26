# Roadmap: AI User Control

## Overview

This roadmap delivers a centralized dashboard for tracking AI tool usage and costs across Bemobi's engineering organization. The journey starts with user identity and authentication (who are the users), progresses through shared metrics infrastructure, then integrates each AI provider individually (Cursor, Claude, GitHub Copilot), adds cost analysis and inactive account detection, and finishes with the admin dashboard. Each phase delivers a coherent, verifiable capability that builds on the previous.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Auth & User Management** - Secure access with role-based control and corporate user registry
- [x] **Phase 2: Identity Resolution & Account Linking** - Google Workspace sync and tool account mapping per user
- [ ] **Phase 3: Metrics Infrastructure** - ShedLock, scheduling, persistence, circuit breaker, retry — shared infra for all providers
- [ ] **Phase 4: Cursor Integration** - Collect Cursor usage metrics via API, scheduled job, normalized storage
- [ ] **Phase 5: Claude Integration** - Collect Claude usage metrics via API, scheduled job, normalized storage
- [ ] **Phase 6: GitHub Copilot Integration** - Collect GitHub Copilot metrics with username→email mapping via GWS
- [ ] **Phase 7: Cost Calculation & Inactive Detection** - Pricing rules, cost calculation, inactive account flagging
- [ ] **Phase 8: Admin Dashboard** - Full visibility UI for admins across all users, tools, and costs
- [x] **Phase 9: Developer Self-Service** - ~~Personal usage dashboard restricted to own data~~ **DROPPED** (admin-only platform)

## Phase Details

### Phase 1: Auth & User Management
**Goal**: Admin-only access via Google SSO with corporate user registry (read-only) and extensible AI tool catalog
**Depends on**: Nothing (first phase)
**Requirements**: AUTH-01, AUTH-03, AUTH-04, USER-01, USER-05, USER-06
**Dropped/Reframed**: AUTH-02 (dropped -- no developer login), USER-02 (reframed -- users from provider APIs), USER-03 (reframed -- read-only), USER-04 (reframed -- status automatic)
**Success Criteria** (what must be TRUE):
  1. Admin can log in via Google SSO and access all system features; platform is admin-only (developer login dropped)
  2. Admin can view users (read-only), and manage AI tool catalog through the UI
  3. User sessions persist securely across requests with 8-hour timeout
  4. System tracks user status (ACTIVE, INACTIVE, OFFBOARDED) in data model
  5. Unit tests verify security-critical authentication validation logic
**Plans**: 3 plans

Plans:
- [x] 01-01-PLAN.md -- Data foundation: OAuth2 deps, Flyway migrations, JPA entities, repositories, config
- [x] 01-02-PLAN.md -- Auth flow: SecurityConfig, CustomOidcUserService, login page, unit tests
- [x] 01-03-PLAN.md -- UI: Dashboard layout with sidebar, read-only user list, AI Tool CRUD with HTMX

### Phase 2: Identity Resolution & Account Linking
**Goal**: System resolves corporate identities via Google Workspace and admins can link users to their AI tool accounts
**Depends on**: Phase 1
**Requirements**: GW-01, GW-02, GW-03, GW-04, GW-05, ACCT-01, ACCT-02, ACCT-03, ACCT-04, ACCT-05, ACCT-06, ACCT-07
**Success Criteria** (what must be TRUE):
  1. System connects to Google Workspace API, retrieves the full user directory with pagination, and reads the github_username custom property
  2. Admin can link a user to Claude, Cursor, and GitHub accounts (including automatic GitHub username resolution from Google Workspace)
  3. Admin can view all linked tool accounts for a user and unlink any account
  4. System tracks account status per tool (ACTIVE, SUSPENDED, REVOKED) and supports multiple accounts per tool per user
  5. System validates Google Workspace API credentials at startup and fails fast with clear error if misconfigured
**Plans**: 3 plans

Plans:
- [x] 02-01-PLAN.md -- Data foundation: Google Admin SDK dep, V5 migration (accounts table, credentials, github_username), entities, repository, GWS config with startup validation
- [x] 02-02-PLAN.md -- Sync service: GoogleWorkspaceService with pagination, Claude/Cursor API user fetch, AccountLinkingService with email matching, SyncOrchestrator, unit tests
- [x] 02-03-PLAN.md -- UI: Sync button on dashboard with toast feedback, user detail page with linked accounts and unlink, "Contas Pendentes" page with sidebar badge

### Phase 02.2: Validar integração Cursor API e testar sync end-to-end (INSERTED)

**Goal:** Validate CursorApiClient against real Cursor Admin API, remove @ConditionalOnProperty from all tool clients (DB-driven enablement), and add per-tool sync result breakdown in UI toast
**Depends on:** Phase 02.1
**Requirements**: TBD (inserted urgent phase, no formal requirement IDs)
**Success Criteria** (what must be TRUE):
  1. CursorApiClient uses Cursor's real Admin API (Basic Auth, /teams/members, teamMembers response, isRemoved filtering)
  2. All three API clients (Claude, Cursor, GitHub Copilot) are always-available Spring beans (no @ConditionalOnProperty)
  3. DB tool registration is the sole source of truth for tool enablement (no YAML enabled flags)
  4. Sync toast shows per-tool breakdown (seats found, linked, errors per tool)
  5. CursorApiClient has dedicated unit tests
  6. `mvn compile` + `mvn test` pass
**Plans**: 2 plans

Plans:
- [ ] 02.2-01-PLAN.md -- Rewrite CursorApiClient for real Cursor Admin API, remove @ConditionalOnProperty from all clients, clean up config
- [ ] 02.2-02-PLAN.md -- Per-tool sync result details in SyncResultResponse and toast UI, CursorApiClient unit tests, SyncOrchestratorTest update

### Phase 02.1: Inverter fonte de usuários — IAs primeiro, GWS depois (INSERTED)

**Goal:** Invert user discovery flow so AI tool seats/licenses are the primary user source, GWS becomes individual email lookup only, and admin has a report page for invalid/removable accounts
**Depends on:** Phase 2
**Requirements**: INVERT-DATA, INVERT-GWS, INVERT-GHCLIENT, INVERT-FLOW, INVERT-PARALLEL, INVERT-VALIDATE, INVERT-DISAPPEAR, INVERT-LEGACY, INVERT-REMOVEGWS, INVERT-REPORT, INVERT-SIDEBAR
**Success Criteria** (what must be TRUE):
  1. SyncOrchestrator fetches AI tool seats first (Claude, Cursor, GitHub Copilot in parallel), then validates each email against GWS individually
  2. Users are only created when their seat email is validated in GWS; non-corporate seats stay with user_id=NULL
  3. Full GWS directory sync is removed; GWS is used only for per-email lookup
  4. GitHub Copilot seats are fetched via API and matched to users by github_username
  5. Disappeared seats (no longer returned by API) are marked SUSPENDED then REVOKED
  6. Legacy users with no AI seats are archived as INACTIVE
  7. Admin can view a report page with two sections: seats to remove and external/invalid seats
  8. `mvn compile` + `mvn test` pass
**Plans**: 3 plans

Plans:
- [x] 02.1-01-PLAN.md -- Data foundation: V9 migration, User entity updates, GWS single-user lookup, GitHub Copilot client, repository queries
- [x] 02.1-02-PLAN.md -- SyncOrchestrator rewrite: AI-first flow with parallel seat fetch, GWS validation, GitHub matching, legacy cleanup, unit tests
- [x] 02.1-03-PLAN.md -- Admin report UI: Two-section report page (seats to remove + external seats), tool filter, sidebar update

### Phase 3: Metrics Infrastructure
**Goal**: Shared infrastructure for metrics collection — scheduling with distributed locking, persistence with idempotency, circuit breaker and retry — ready for any provider
**Depends on**: Phase 2
**Requirements**: SCHED-01 to SCHED-07 (partial), metrics data model
**Success Criteria** (what must be TRUE):
  1. ShedLock prevents duplicate scheduled job runs across multiple application instances
  2. Table `usage_metrics` accepts metrics from any provider with normalized schema
  3. Unique constraint on (account, date, metric_type) guarantees idempotency
  4. Circuit breaker and retry are configured and ready for provider integrations
  5. Admin can manually trigger metric collection (even without providers implemented)
  6. `mvn compile` + `mvn test` pass
**Plans**: TBD

Plans:
- [ ] 03-01: TBD

### Phase 4: Cursor Integration
**Goal**: System collects Cursor usage data via API on a daily schedule, storing normalized metrics ready for cost calculation
**Depends on**: Phase 3
**Requirements**: CURSOR-01, CURSOR-02, CURSOR-03, CURSOR-04, CURSOR-05, CURSOR-06
**Success Criteria** (what must be TRUE):
  1. Scheduled job collects usage metrics and last access date per Cursor user daily via API
  2. Cursor metrics are stored in the same normalized format as other providers
  3. Cursor integration failures do not block other provider collection (isolated circuit breaker)
  4. Collection is idempotent: re-running for the same date produces no duplicates
**Plans**: TBD

Plans:
- [ ] 04-01: TBD
- [ ] 04-02: TBD

### Phase 5: Claude Integration
**Goal**: System collects Claude usage data via Anthropic API on a daily schedule with full resilience, storing normalized metrics
**Depends on**: Phase 3
**Requirements**: CLAUDE-01, CLAUDE-02, CLAUDE-03, CLAUDE-04, CLAUDE-05, CLAUDE-06, CLAUDE-07
**Success Criteria** (what must be TRUE):
  1. Scheduled job runs nightly and collects token counts, request counts, and last access dates per Claude user
  2. Collection is idempotent: re-running for the same date produces no duplicates and no data corruption
  3. System handles Anthropic API rate limits with exponential backoff and circuit breaker; failures are logged with health status visible to admin
  4. Claude metrics are stored in the same normalized format as other providers
**Plans**: TBD

Plans:
- [ ] 05-01: TBD
- [ ] 05-02: TBD

### Phase 6: GitHub Copilot Integration
**Goal**: System collects GitHub Copilot usage data with seat-based metrics, mapping GitHub usernames to corporate users via GWS
**Depends on**: Phase 3
**Requirements**: GITHUB-01, GITHUB-02, GITHUB-03, GITHUB-04, GITHUB-05, GITHUB-06, GITHUB-07, GITHUB-08
**Success Criteria** (what must be TRUE):
  1. Scheduled job collects Copilot seat assignments, last activity dates, and suggestion metrics daily
  2. System maps GitHub usernames to corporate emails via the Google Workspace github_username property established in Phase 2
  3. System handles GitHub API rate limits with backoff and retry; Copilot-specific pagination works for large organizations
  4. Raw GitHub metrics are stored in the same normalized format as other providers, ready for cost calculation
**Plans**: TBD

Plans:
- [ ] 06-01: TBD
- [ ] 06-02: TBD

### Phase 7: Cost Calculation & Inactive Detection
**Goal**: System translates raw metrics into dollar costs with configurable pricing and identifies accounts with no recent activity
**Depends on**: Phases 4, 5, 6
**Requirements**: COST-01, COST-02, COST-03, COST-04, COST-05, COST-06, DETECT-01, DETECT-02, DETECT-03, DETECT-04, DETECT-05
**Success Criteria** (what must be TRUE):
  1. System calculates estimated cost per user per tool per month using configurable, versioned pricing rules (not hardcoded rates)
  2. Costs are aggregated by user, tool, department, and time period with daily and monthly rollups stored
  3. System identifies accounts with no usage in the last N days (default 30, admin-configurable) while excluding accounts affected by known collection failures
  4. Inactive accounts are flagged and visible in the admin interface with account lifecycle data (first seen, last seen, inactive since)
**Plans**: TBD

Plans:
- [ ] 07-01: TBD
- [ ] 07-02: TBD

### Phase 8: Admin Dashboard
**Goal**: Admins have full visibility into all users, tools, costs, and inactive accounts through a server-rendered dashboard
**Depends on**: Phase 7
**Requirements**: DASH-01, DASH-02, DASH-03, DASH-04, DASH-05, DASH-06, DASH-07, DASH-08
**Success Criteria** (what must be TRUE):
  1. Admin can view a dashboard showing all users with their tools, costs, and status; filter by status, department, or tool; and sort by cost
  2. Admin can drill down into any user to see detailed per-tool usage history
  3. Dashboard shows total monthly cost across all tools and cost breakdown by tool (Claude vs GitHub vs Cursor)
  4. Admin can view a dedicated section listing inactive accounts requiring attention
  5. Dashboard renders server-side with Thymeleaf + HTMX for dynamic updates without full page reloads
**Plans**: TBD

Plans:
- [ ] 08-01: TBD
- [ ] 08-02: TBD

### Phase 9: Developer Self-Service -- DROPPED
**Goal**: ~~Developers can view their own usage and costs without admin involvement~~
**Status**: DROPPED per CONTEXT.md -- Platform is admin-only. Developers never log in. All DEV-01 through DEV-05 requirements dropped.
**Requirements**: ~~DEV-01, DEV-02, DEV-03, DEV-04, DEV-05~~ (all dropped)
**Plans**: None

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8

Note: Phases 4, 5, and 6 (Cursor, Claude, GitHub) all depend on Phase 3 only, so they could execute in parallel if needed.

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Auth & User Management | 3/3 | Complete | 2026-02-24 |
| 2. Identity Resolution & Account Linking | 3/3 | Complete | 2026-02-24 |
| 02.1. Inverter fonte de usuarios | 3/3 | Complete    | 2026-02-26 |
| 02.2. Validar Cursor API + sync E2E | 1/2 | In Progress|  |
| 3. Metrics Infrastructure | 0/1 | Not started | - |
| 4. Cursor Integration | 0/2 | Not started | - |
| 5. Claude Integration | 0/2 | Not started | - |
| 6. GitHub Copilot Integration | 0/2 | Not started | - |
| 7. Cost Calculation & Inactive Detection | 0/2 | Not started | - |
| 8. Admin Dashboard | 0/2 | Not started | - |
| 9. Developer Self-Service | -- | DROPPED | - |
