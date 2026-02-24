# Roadmap: AI User Control

## Overview

This roadmap delivers a centralized dashboard for tracking AI tool usage and costs across Bemobi's engineering organization. The journey starts with user identity and authentication (who are the users), progresses through provider API integrations (what are they using), adds cost analysis and inactive account detection (what does it cost and who left), and finishes with admin and developer dashboards (who sees what). Each phase delivers a coherent, verifiable capability that builds on the previous.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Auth & User Management** - Secure access with role-based control and corporate user registry
- [ ] **Phase 2: Identity Resolution & Account Linking** - Google Workspace sync and tool account mapping per user
- [ ] **Phase 3: Metrics Pipeline & Claude Integration** - Scheduling infrastructure and first provider collection end-to-end
- [ ] **Phase 4: GitHub Copilot Integration** - Second provider with seat-based metrics and username mapping
- [ ] **Phase 5: Cost Calculation & Inactive Detection** - Turn raw metrics into dollar costs and flag orphaned accounts
- [ ] **Phase 6: Admin Dashboard** - Full visibility UI for admins across all users, tools, and costs
- [ ] **Phase 7: Cursor Integration** - Third provider with fallback CSV import for API instability
- [x] **Phase 8: Developer Self-Service** - ~~Personal usage dashboard restricted to own data~~ **DROPPED** (admin-only platform)

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
- [ ] 01-01-PLAN.md -- Data foundation: OAuth2 deps, Flyway migrations, JPA entities, repositories, config
- [ ] 01-02-PLAN.md -- Auth flow: SecurityConfig, CustomOidcUserService, login page, unit tests
- [ ] 01-03-PLAN.md -- UI: Dashboard layout with sidebar, read-only user list, AI Tool CRUD with HTMX

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
**Plans**: TBD

Plans:
- [ ] 02-01: TBD
- [ ] 02-02: TBD

### Phase 3: Metrics Pipeline & Claude Integration
**Goal**: System collects Claude usage data on a daily schedule with full resilience, storing normalized metrics ready for cost calculation
**Depends on**: Phase 2
**Requirements**: CLAUDE-01, CLAUDE-02, CLAUDE-03, CLAUDE-04, CLAUDE-05, CLAUDE-06, CLAUDE-07, SCHED-01, SCHED-02, SCHED-03, SCHED-04, SCHED-05, SCHED-06, SCHED-07
**Success Criteria** (what must be TRUE):
  1. Scheduled job runs nightly (2 AM default) and collects token counts, request counts, and last access dates per Claude user
  2. Collection is idempotent: re-running for the same date produces no duplicates and no data corruption
  3. System handles Anthropic API rate limits with exponential backoff and circuit breaker; failures are logged with health status visible to admin
  4. Admin can manually trigger metric collection for a specific date
  5. ShedLock prevents duplicate job runs across multiple application instances
**Plans**: TBD

Plans:
- [ ] 03-01: TBD
- [ ] 03-02: TBD
- [ ] 03-03: TBD

### Phase 4: GitHub Copilot Integration
**Goal**: System collects GitHub Copilot usage data with seat-based metrics, mapping GitHub usernames to corporate users
**Depends on**: Phase 3
**Requirements**: GITHUB-01, GITHUB-02, GITHUB-03, GITHUB-04, GITHUB-05, GITHUB-06, GITHUB-07, GITHUB-08
**Success Criteria** (what must be TRUE):
  1. Scheduled job collects Copilot seat assignments, last activity dates, and suggestion metrics daily
  2. System maps GitHub usernames to corporate emails via the Google Workspace github_username property established in Phase 2
  3. System handles GitHub API rate limits with backoff and retry; Copilot-specific pagination works for large organizations
  4. Raw GitHub metrics are stored in the same normalized format as Claude metrics, ready for cost calculation
**Plans**: TBD

Plans:
- [ ] 04-01: TBD
- [ ] 04-02: TBD

### Phase 5: Cost Calculation & Inactive Detection
**Goal**: System translates raw metrics into dollar costs with configurable pricing and identifies accounts with no recent activity
**Depends on**: Phase 4
**Requirements**: COST-01, COST-02, COST-03, COST-04, COST-05, COST-06, DETECT-01, DETECT-02, DETECT-03, DETECT-04, DETECT-05
**Success Criteria** (what must be TRUE):
  1. System calculates estimated cost per user per tool per month using configurable, versioned pricing rules (not hardcoded rates)
  2. Costs are aggregated by user, tool, department, and time period with daily and monthly rollups stored
  3. System identifies accounts with no usage in the last N days (default 30, admin-configurable) while excluding accounts affected by known collection failures
  4. Inactive accounts are flagged and visible in the admin interface with account lifecycle data (first seen, last seen, inactive since)
**Plans**: TBD

Plans:
- [ ] 05-01: TBD
- [ ] 05-02: TBD

### Phase 6: Admin Dashboard
**Goal**: Admins have full visibility into all users, tools, costs, and inactive accounts through a server-rendered dashboard
**Depends on**: Phase 5
**Requirements**: DASH-01, DASH-02, DASH-03, DASH-04, DASH-05, DASH-06, DASH-07, DASH-08
**Success Criteria** (what must be TRUE):
  1. Admin can view a dashboard showing all users with their tools, costs, and status; filter by status, department, or tool; and sort by cost
  2. Admin can drill down into any user to see detailed per-tool usage history
  3. Dashboard shows total monthly cost across all tools and cost breakdown by tool (Claude vs GitHub vs Cursor)
  4. Admin can view a dedicated section listing inactive accounts requiring attention
  5. Dashboard renders server-side with Thymeleaf + HTMX for dynamic updates without full page reloads
**Plans**: TBD

Plans:
- [ ] 06-01: TBD
- [ ] 06-02: TBD

### Phase 7: Cursor Integration
**Goal**: System collects Cursor usage data via API or manual CSV import, completing coverage of all three AI tools
**Depends on**: Phase 3
**Requirements**: CURSOR-01, CURSOR-02, CURSOR-03, CURSOR-04, CURSOR-05, CURSOR-06
**Success Criteria** (what must be TRUE):
  1. If Cursor API is available: scheduled job collects usage metrics and last access date per user daily
  2. If Cursor API is unavailable: admin can upload a CSV file with Cursor usage data and the system imports it correctly
  3. Cursor metrics are stored in the same normalized format as Claude and GitHub metrics
  4. Cursor integration failures do not block or affect Claude/GitHub collection (isolated circuit breaker)
**Plans**: TBD

Plans:
- [ ] 07-01: TBD
- [ ] 07-02: TBD

### Phase 8: Developer Self-Service -- DROPPED
**Goal**: ~~Developers can view their own usage and costs without admin involvement~~
**Status**: DROPPED per CONTEXT.md -- Platform is admin-only. Developers never log in. All DEV-01 through DEV-05 requirements dropped.
**Requirements**: ~~DEV-01, DEV-02, DEV-03, DEV-04, DEV-05~~ (all dropped)
**Plans**: None

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8

Note: Phase 7 (Cursor) depends on Phase 3 (not Phase 6), so it could execute in parallel with Phases 4-6 if needed.

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Auth & User Management | 0/3 | Planning complete | - |
| 2. Identity Resolution & Account Linking | 0/2 | Not started | - |
| 3. Metrics Pipeline & Claude Integration | 0/3 | Not started | - |
| 4. GitHub Copilot Integration | 0/2 | Not started | - |
| 5. Cost Calculation & Inactive Detection | 0/2 | Not started | - |
| 6. Admin Dashboard | 0/2 | Not started | - |
| 7. Cursor Integration | 0/2 | Not started | - |
| 8. Developer Self-Service | -- | DROPPED | - |
