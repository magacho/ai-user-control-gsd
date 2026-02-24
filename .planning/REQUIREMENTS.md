# Requirements: AI User Control

**Defined:** 2026-02-24
**Core Value:** Transparent AI tool costs with zero wasted spending on orphaned accounts

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Authentication & Authorization

- [x] **AUTH-01**: Admin can log in via Google SSO and access all system features
- [ ] ~~**AUTH-02**: Developer can log in and access only personal usage data~~ **DROPPED** -- Platform is admin-only per CONTEXT.md; Phase 8 (Developer Self-Service) dropped entirely
- [ ] **AUTH-03**: System enforces admin-only access control (all authenticated users are admins)
- [x] **AUTH-04**: User session persists securely across requests with 8-hour timeout

### User Management

- [ ] **USER-01**: Admin can view list of all users with corporate email (@bemobi.com)
- [ ] ~~**USER-02**: Admin can create new user with email, name, department, status~~ **REFRAMED** -- Users are discovered from provider APIs, not manually created; data model supports the fields but no create endpoint exists
- [ ] ~~**USER-03**: Admin can update user information (name, department, status)~~ **REFRAMED** -- User data is read-only in dashboard; profile data comes from Google Workspace and provider APIs, not manual edits
- [ ] ~~**USER-04**: Admin can deactivate user (soft delete, preserves historical data)~~ **REFRAMED** -- User status is determined automatically from data (orphaned account detection), not manually set by admin
- [x] **USER-05**: System validates corporate email domain (@bemobi.com)
- [x] **USER-06**: System tracks user status (ACTIVE, INACTIVE, OFFBOARDED)

### Google Workspace Integration

- [ ] **GW-01**: System connects to Google Workspace API with appropriate permissions
- [ ] **GW-02**: System retrieves user list from Google Workspace directory
- [ ] **GW-03**: System reads custom property `github_username` from Google Workspace user profiles
- [ ] **GW-04**: System handles pagination when fetching large user lists
- [ ] **GW-05**: System validates Google Workspace API credentials at startup

### Account Linking

- [ ] **ACCT-01**: Admin can link user email to Claude account identifier
- [ ] **ACCT-02**: Admin can link user email to Cursor account identifier
- [ ] **ACCT-03**: Admin can link user email to GitHub username via Google Workspace custom property
- [ ] **ACCT-04**: Admin can view all tool accounts linked to a specific user
- [ ] **ACCT-05**: Admin can unlink tool account from user
- [ ] **ACCT-06**: System tracks account status per tool (ACTIVE, SUSPENDED, REVOKED)
- [ ] **ACCT-07**: System handles multiple accounts per tool per user

### Metrics Collection - Claude

- [ ] **CLAUDE-01**: System connects to Anthropic API with organization credentials
- [ ] **CLAUDE-02**: Scheduled job collects Claude usage data daily (2 AM default)
- [ ] **CLAUDE-03**: System retrieves token counts (input + output) per user per day
- [ ] **CLAUDE-04**: System retrieves request counts per user per day
- [ ] **CLAUDE-05**: System retrieves last access date per user
- [ ] **CLAUDE-06**: System handles Anthropic API rate limits with backoff and retry
- [ ] **CLAUDE-07**: System stores raw Claude metrics in normalized format

### Metrics Collection - GitHub Copilot

- [ ] **GITHUB-01**: System connects to GitHub API with organization token
- [ ] **GITHUB-02**: Scheduled job collects GitHub Copilot usage data daily
- [ ] **GITHUB-03**: System retrieves seat assignments per GitHub username
- [ ] **GITHUB-04**: System retrieves last activity date per seat
- [ ] **GITHUB-05**: System retrieves suggestion counts and acceptance rates (org-level)
- [ ] **GITHUB-06**: System maps GitHub username to corporate email via Google Workspace
- [ ] **GITHUB-07**: System handles GitHub API rate limits with backoff and retry
- [ ] **GITHUB-08**: System stores raw GitHub metrics in normalized format

### Metrics Collection - Cursor

- [ ] **CURSOR-01**: System connects to Cursor API (or fallback: CSV import)
- [ ] **CURSOR-02**: Scheduled job collects Cursor usage data daily (if API available)
- [ ] **CURSOR-03**: System retrieves usage metrics per user per day
- [ ] **CURSOR-04**: System retrieves last access date per user
- [ ] **CURSOR-05**: Admin can manually upload Cursor usage CSV if API unavailable
- [ ] **CURSOR-06**: System stores raw Cursor metrics in normalized format

### Scheduled Jobs & Resilience

- [ ] **SCHED-01**: System uses ShedLock for distributed scheduling (prevents duplicate runs)
- [ ] **SCHED-02**: Scheduled jobs are idempotent (safe to re-run for same date)
- [ ] **SCHED-03**: System implements circuit breaker pattern for external API calls
- [ ] **SCHED-04**: System retries failed API calls with exponential backoff
- [ ] **SCHED-05**: System logs collection health (last run, success/failure counts)
- [ ] **SCHED-06**: Admin can manually trigger metric collection for specific date
- [ ] **SCHED-07**: System caches API responses to avoid rate limit exhaustion

### Cost Calculation

- [ ] **COST-01**: System calculates estimated cost per user per tool per month
- [ ] **COST-02**: System uses configurable pricing rules (not hardcoded rates)
- [ ] **COST-03**: System supports versioned pricing with effective dates
- [ ] **COST-04**: System converts tokens to costs using model-specific rates
- [ ] **COST-05**: System aggregates costs by user, tool, department, and time period
- [ ] **COST-06**: System stores aggregated costs (daily, monthly rollups)

### Inactive Account Detection

- [ ] **DETECT-01**: System identifies accounts with no usage in last 30 days
- [ ] **DETECT-02**: System flags inactive accounts in admin dashboard
- [ ] **DETECT-03**: System excludes accounts with known gaps (collection failures) from detection
- [ ] **DETECT-04**: Admin can configure inactivity threshold (default 30 days)
- [ ] **DETECT-05**: System tracks account lifecycle (first seen, last seen, inactive since)

### Admin Dashboard

- [ ] **DASH-01**: Admin can view dashboard showing all users, tools, and costs
- [ ] **DASH-02**: Admin can filter users by status, department, or tool
- [ ] **DASH-03**: Admin can sort users by cost (highest to lowest)
- [ ] **DASH-04**: Admin can drill down into specific user to see detailed usage
- [ ] **DASH-05**: Admin can view inactive accounts requiring attention
- [ ] **DASH-06**: Dashboard shows total monthly cost across all tools
- [ ] **DASH-07**: Dashboard shows cost breakdown by tool (Claude vs GitHub vs Cursor)
- [ ] **DASH-08**: Dashboard uses Thymeleaf + HTMX (server-side rendering)

### Developer Self-Service -- DROPPED

~~- [ ] **DEV-01**: Developer can view personal usage dashboard (no access to others)~~
~~- [ ] **DEV-02**: Developer can see own token consumption per tool~~
~~- [ ] **DEV-03**: Developer can see own cost estimate per tool~~
~~- [ ] **DEV-04**: Developer can see own usage history (last 90 days)~~
~~- [ ] **DEV-05**: Developer can see which tools they have active accounts for~~

**DROPPED** -- Platform is admin-only per CONTEXT.md. Phase 8 (Developer Self-Service) dropped entirely. Developers never log in.

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Alerts & Notifications

- **ALERT-01**: System sends email alert when account inactive for 30+ days
- **ALERT-02**: System sends email alert when user cost exceeds threshold
- **ALERT-03**: System sends email alert when collection job fails
- **ALERT-04**: Admin can configure alert rules and recipients

### Advanced Reporting

- **REPORT-01**: Admin can generate usage trend reports (month-over-month)
- **REPORT-02**: Admin can export cost reports to CSV
- **REPORT-03**: Admin can view department-level cost aggregation
- **REPORT-04**: Admin can view historical cost trends with charts

### Google Workspace Automation

- **GW-AUTO-01**: System auto-creates user when new employee added to Google Workspace
- **GW-AUTO-02**: System auto-deactivates user when employee offboarded in Google Workspace
- **GW-AUTO-03**: System syncs user data changes from Google Workspace daily

### Bulk Operations

- **BULK-01**: Admin can bulk import users from CSV
- **BULK-02**: Admin can bulk link accounts from CSV
- **BULK-03**: Admin can bulk deactivate multiple users at once

### Audit Trail

- **AUDIT-01**: System logs all admin actions (create, update, delete, link, unlink)
- **AUDIT-02**: Admin can view audit log with timestamp, user, and action
- **AUDIT-03**: System retains audit logs for 12 months minimum

### API Extension

- **API-01**: System exposes REST API for external integrations
- **API-02**: System provides OpenAPI documentation for all endpoints
- **API-03**: External systems can query usage data via API with authentication

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Real-time usage tracking | Daily batch collection is sufficient for cost control; real-time adds complexity without value |
| Automatic account provisioning | Manual provisioning with centralized tracking is enough; auto-provisioning adds risk |
| Automatic account deprovisioning | System detects inactive accounts but does not auto-disable; safety first, admin approval required |
| Multi-tenancy for other companies | Built specifically for Bemobi's needs; single-tenant simplifies everything |
| Mobile app | Web dashboard covers admin and developer use cases; mobile not justified |
| AI tool configuration management | System tracks usage, not tool configuration (IDE settings, etc.) |
| Code quality analysis | Measures quantity (tokens, requests), not quality; out of scope |
| SPA frontend migration | Thymeleaf + HTMX decision made; no React/Vue/Angular complexity |
| Cross-company benchmarking | No data source, not the problem we're solving |
| Webhook integrations | API-first design deferred to v2; webhooks not needed for v1 |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| AUTH-01 | Phase 1: Auth & User Management | Complete |
| AUTH-02 | -- | Dropped (admin-only platform, no developer login) |
| AUTH-03 | Phase 1: Auth & User Management | Pending (reframed: admin-only access control) |
| AUTH-04 | Phase 1: Auth & User Management | Complete |
| USER-01 | Phase 1: Auth & User Management | Pending |
| USER-02 | -- | Reframed (users from provider APIs, not manual creation) |
| USER-03 | -- | Reframed (read-only dashboard, no manual edits) |
| USER-04 | -- | Reframed (status automatic from data, not manual action) |
| USER-05 | Phase 1: Auth & User Management | Complete |
| USER-06 | Phase 1: Auth & User Management | Complete |
| GW-01 | Phase 2: Identity Resolution & Account Linking | Pending |
| GW-02 | Phase 2: Identity Resolution & Account Linking | Pending |
| GW-03 | Phase 2: Identity Resolution & Account Linking | Pending |
| GW-04 | Phase 2: Identity Resolution & Account Linking | Pending |
| GW-05 | Phase 2: Identity Resolution & Account Linking | Pending |
| ACCT-01 | Phase 2: Identity Resolution & Account Linking | Pending |
| ACCT-02 | Phase 2: Identity Resolution & Account Linking | Pending |
| ACCT-03 | Phase 2: Identity Resolution & Account Linking | Pending |
| ACCT-04 | Phase 2: Identity Resolution & Account Linking | Pending |
| ACCT-05 | Phase 2: Identity Resolution & Account Linking | Pending |
| ACCT-06 | Phase 2: Identity Resolution & Account Linking | Pending |
| ACCT-07 | Phase 2: Identity Resolution & Account Linking | Pending |
| CLAUDE-01 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| CLAUDE-02 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| CLAUDE-03 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| CLAUDE-04 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| CLAUDE-05 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| CLAUDE-06 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| CLAUDE-07 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| SCHED-01 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| SCHED-02 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| SCHED-03 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| SCHED-04 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| SCHED-05 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| SCHED-06 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| SCHED-07 | Phase 3: Metrics Pipeline & Claude Integration | Pending |
| GITHUB-01 | Phase 4: GitHub Copilot Integration | Pending |
| GITHUB-02 | Phase 4: GitHub Copilot Integration | Pending |
| GITHUB-03 | Phase 4: GitHub Copilot Integration | Pending |
| GITHUB-04 | Phase 4: GitHub Copilot Integration | Pending |
| GITHUB-05 | Phase 4: GitHub Copilot Integration | Pending |
| GITHUB-06 | Phase 4: GitHub Copilot Integration | Pending |
| GITHUB-07 | Phase 4: GitHub Copilot Integration | Pending |
| GITHUB-08 | Phase 4: GitHub Copilot Integration | Pending |
| COST-01 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| COST-02 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| COST-03 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| COST-04 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| COST-05 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| COST-06 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| DETECT-01 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| DETECT-02 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| DETECT-03 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| DETECT-04 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| DETECT-05 | Phase 5: Cost Calculation & Inactive Detection | Pending |
| DASH-01 | Phase 6: Admin Dashboard | Pending |
| DASH-02 | Phase 6: Admin Dashboard | Pending |
| DASH-03 | Phase 6: Admin Dashboard | Pending |
| DASH-04 | Phase 6: Admin Dashboard | Pending |
| DASH-05 | Phase 6: Admin Dashboard | Pending |
| DASH-06 | Phase 6: Admin Dashboard | Pending |
| DASH-07 | Phase 6: Admin Dashboard | Pending |
| DASH-08 | Phase 6: Admin Dashboard | Pending |
| CURSOR-01 | Phase 7: Cursor Integration | Pending |
| CURSOR-02 | Phase 7: Cursor Integration | Pending |
| CURSOR-03 | Phase 7: Cursor Integration | Pending |
| CURSOR-04 | Phase 7: Cursor Integration | Pending |
| CURSOR-05 | Phase 7: Cursor Integration | Pending |
| CURSOR-06 | Phase 7: Cursor Integration | Pending |
| DEV-01 | -- | Dropped (Phase 8 dropped, admin-only platform) |
| DEV-02 | -- | Dropped (Phase 8 dropped, admin-only platform) |
| DEV-03 | -- | Dropped (Phase 8 dropped, admin-only platform) |
| DEV-04 | -- | Dropped (Phase 8 dropped, admin-only platform) |
| DEV-05 | -- | Dropped (Phase 8 dropped, admin-only platform) |

**Coverage:**
- v1 requirements: 74 total (68 active, 1 dropped, 5 reframed)
- Dropped: AUTH-02, DEV-01 through DEV-05 (6 total -- platform is admin-only)
- Reframed: USER-02, USER-03, USER-04 (users from provider APIs, read-only dashboard)
- Active mapped to phases: 68
- Unmapped: 0

---
*Requirements defined: 2026-02-24*
*Last updated: 2026-02-24 after Phase 1 CONTEXT.md decisions*
