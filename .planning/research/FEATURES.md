# Feature Research: AI Tool Usage Management Dashboard

**Research Date:** 2026-02-24
**Domain:** Enterprise AI tool cost control and usage visibility
**Target Tools:** Claude Code (Anthropic), Cursor, GitHub Copilot

---

## Context

Bemobi needs a centralized dashboard to manage AI tool accounts (Claude Code, Cursor, GitHub Copilot), track usage, attribute costs per developer, and detect orphaned accounts. The system serves two audiences: administrators (full visibility, account management) and developers (self-service personal usage). The existing codebase is a Spring Boot 3.4.2 application with stub implementations across all layers.

Enterprise AI tool management is an emerging category. Most companies today rely on each provider's native admin console, which means fragmented visibility, no cross-tool cost attribution, and manual reconciliation. The value proposition is unification: one place to see all AI tool spending, per person, with lifecycle management.

---

## Table Stakes Features

These features are required for the system to fulfill its core purpose. Without any one of them, the dashboard fails to deliver its value proposition of "transparent AI tool costs with zero wasted spending."

### TS-1: Unified User Registry with Corporate Email Key

**What:** Central registry of all employees who use AI tools, keyed on @bemobi.com corporate email. Single source of truth for "who has what."

**Why table stakes:** Without a canonical user list, you cannot answer "who has access to what" or "who costs how much." This is the foundational data model.

**Complexity:** Low-Medium
- Data model: User entity with email, name, department, status
- Google Workspace sync provides the authoritative employee list
- GitHub username mapping via Google Workspace custom property bridges the identity gap

**Dependencies:** None (foundational)

**Sub-features:**
- User CRUD operations (create, read, update, deactivate)
- Corporate email validation (@bemobi.com domain enforcement)
- User status tracking (ACTIVE, INACTIVE, OFFBOARDED)
- Bulk import from Google Workspace directory

---

### TS-2: AI Tool Account Linking

**What:** Map each user's corporate email to their tool-specific identifiers across Claude, Cursor, and GitHub Copilot. Each user can have 0-N tool accounts.

**Why table stakes:** The tools identify users differently (email for Claude/Cursor, GitHub username for Copilot). Without this mapping, you cannot attribute usage or costs to a person.

**Complexity:** Medium
- Junction table: user_ai_tool_accounts linking users to tool accounts
- GitHub username resolution via Google Workspace custom property field
- Handle edge cases: personal GitHub accounts used for work, multiple accounts per tool
- Account status tracking per tool (ACTIVE, SUSPENDED, REVOKED)

**Dependencies:** TS-1 (User Registry)

**Sub-features:**
- Manual account linking by admin
- Auto-discovery of accounts via provider APIs where possible
- Account status tracking per tool per user
- Unlink/revoke capability

---

### TS-3: Automated Daily Metrics Collection

**What:** Scheduled background job (nightly, 2 AM default) that pulls usage data from each provider's API. Collects last access date, token consumption, request counts, and any cost-relevant metrics each provider exposes.

**Why table stakes:** Manual data entry defeats the purpose. Without automated collection, the dashboard shows stale or no data. This is the data pipeline that powers everything downstream.

**Complexity:** High
- Three separate API integrations, each with different auth, endpoints, rate limits, and data models
- Error handling: partial failures (one API down), rate limiting, credential expiration
- Idempotent collection: re-running for the same date should not duplicate data
- Resilience: retry with exponential backoff, circuit breaker pattern
- Data normalization: different providers report usage differently

**Dependencies:** TS-2 (Account Linking)

**Sub-features:**
- Claude API integration (token counts, request counts, costs via Anthropic admin API)
- GitHub Copilot API integration (seat usage, acceptance rates via organization API)
- Cursor API integration (usage metrics via Cursor business/team API)
- Configurable cron schedule per integration
- Collection health monitoring (last successful run, failure counts)
- Manual trigger capability for ad-hoc re-collection

**Provider-Specific Notes:**
- **Claude/Anthropic:** Admin API provides organization-level usage. Token counts (input/output) and cost data are available. Rate limits apply.
- **GitHub Copilot:** Organization Copilot API exposes seat assignments, last activity dates, and IDE-specific usage. Copilot metrics API provides acceptance rates and suggestion counts at org level.
- **Cursor:** Cursor's team/business API availability is less mature. May require screen-scraping or CSV export as fallback. This is the highest-risk integration.

---

### TS-4: Admin Dashboard - Full Visibility

**What:** Server-side rendered (Thymeleaf) dashboard for administrators showing all users, all tools, all metrics across the entire organization. The "single pane of glass" for AI tool spending.

**Why table stakes:** This is the primary UI for the core audience. An admin who cannot see everything cannot manage costs.

**Complexity:** Medium
- Summary cards: total monthly cost, active users count, cost trend direction
- User table with sortable/filterable columns: name, email, tools used, last active, monthly cost
- Drill-down into individual users to see tool-by-tool breakdown
- Date range selector for historical views
- Server-side pagination for large user bases

**Dependencies:** TS-1, TS-2, TS-3

**Sub-features:**
- Organization-level cost summary (total, by tool, by period)
- Per-user cost breakdown with tool-level detail
- Active user count per tool
- Last activity timestamps per user per tool
- Tabular user list with sort, filter, pagination
- Export to CSV for finance/procurement use

---

### TS-5: Developer Self-Service View

**What:** Restricted view where developers log in and see only their own usage data: which tools they use, how much they've consumed, estimated cost attributed to them.

**Why table stakes:** Self-service usage reduces admin burden and creates cost awareness. The PROJECT.md explicitly requires this as a core user type. Developers should never see other developers' data.

**Complexity:** Low-Medium
- Same data model as admin, but filtered to authenticated user only
- Personal dashboard: tools assigned, usage per tool, estimated personal cost
- Historical trend of own usage (last 30/60/90 days)
- No access to other users' data (authorization enforcement)

**Dependencies:** TS-1, TS-3, Authentication

**Sub-features:**
- Personal usage summary (all tools combined)
- Per-tool usage detail (tokens, requests, last active)
- Personal cost attribution (estimated monthly cost)
- Historical usage trend chart or table

---

### TS-6: Inactive Account Detection

**What:** Automated process (nightly, 3 AM default) that flags accounts with no activity within a configurable threshold (default: 30 days). Surfaces these in the admin dashboard for review.

**Why table stakes:** "Zero orphaned accounts" is an explicit success metric. Orphaned accounts after employee offboarding are the primary cost waste this system exists to prevent.

**Complexity:** Medium
- Compare last_activity_date against threshold for each user-tool-account
- Flag accounts as INACTIVE when threshold exceeded
- Surface inactive accounts prominently in admin dashboard
- Handle edge cases: developer on leave, seasonal usage patterns
- Do NOT auto-disable (per project scope: "manual offboarding with system detection")

**Dependencies:** TS-3 (requires activity data from metrics collection)

**Sub-features:**
- Configurable inactivity threshold (default 30 days, per environment variable)
- Inactive accounts list in admin dashboard with last activity date
- Filter by tool, by department, by inactivity duration
- "Acknowledged" status so admins can dismiss known-inactive accounts (e.g., parental leave)

---

### TS-7: Cost Estimation and Attribution

**What:** Calculate estimated cost per user, per tool, per time period. Translate raw usage metrics (tokens, seats, requests) into dollar amounts using configurable pricing models.

**Why table stakes:** "Complete cost visibility: Know exactly what each developer costs per tool per month" is the first success metric. Usage data without cost translation is incomplete for decision-making.

**Complexity:** Medium-High
- Each tool has a different pricing model: token-based (Claude), seat-based (Copilot), subscription-based (Cursor)
- Pricing can change; must be configurable, not hardcoded
- Some costs are per-seat (fixed), others are usage-based (variable)
- Need to handle: shared costs (organization-level), individual attribution, blended rates

**Dependencies:** TS-3 (usage metrics data)

**Sub-features:**
- Configurable pricing models per tool (admin-managed)
- Cost calculation engine: input metrics + pricing model = estimated cost
- Per-user monthly cost rollup across all tools
- Per-tool monthly cost rollup across all users
- Organization total monthly cost
- Cost trend over time (month-over-month comparison)

---

### TS-8: JWT Authentication with Role-Based Access

**What:** Secure login with JWT tokens. Two roles: ADMIN (full access) and DEVELOPER (self-service only). Existing Spring Security infrastructure must be completed.

**Why table stakes:** Without authentication and authorization, the admin/developer separation doesn't work, and usage data is exposed to unauthorized parties.

**Complexity:** Medium
- JWT token generation, validation, refresh (infrastructure exists, needs implementation)
- Role-based endpoint protection: admin endpoints vs developer endpoints
- Session management: token expiration, refresh flow
- Login page (Thymeleaf) and API authentication endpoint

**Dependencies:** TS-1 (User Registry for credential storage)

**Sub-features:**
- Login endpoint (API and web form)
- JWT token issuance with role claims
- Token refresh mechanism
- Role-based route protection (admin vs developer)
- Logout / token invalidation

---

## Differentiating Features

These features provide competitive advantage, improve user experience, or add operational value beyond the minimum viable product. They are "nice to have" and should be prioritized after table stakes are complete.

### DF-1: Alert System for Anomalies

**What:** Configurable alerts that fire when usage patterns deviate from norms: sudden cost spikes, unusually high token consumption, new accounts appearing without admin knowledge, or accounts going inactive.

**Why differentiating:** Proactive notification transforms the dashboard from "check when you remember" to "it tells you when something's wrong." Most native provider dashboards lack cross-tool anomaly detection.

**Complexity:** Medium-High
- Define alert rules: threshold-based (cost > X), trend-based (usage up >50% week-over-week), lifecycle-based (inactive > N days)
- Notification channels: email (already stubbed), potentially Slack/Teams later
- Alert fatigue management: snooze, acknowledge, escalation

**Dependencies:** TS-3, TS-6, TS-7

**Sub-features:**
- Inactive account alerts (email to admin when accounts go dormant)
- Cost spike alerts (when user or org cost exceeds threshold)
- New account detection alerts
- Configurable thresholds per alert type
- Alert history log

---

### DF-2: Usage Trend Analytics and Reporting

**What:** Historical trend analysis showing how AI tool adoption and costs evolve over time. Monthly/quarterly reports suitable for leadership and procurement.

**Why differentiating:** Moves beyond "what's the current state" to "where are we headed." Enables budget forecasting and procurement negotiation with data.

**Complexity:** Medium
- Time-series aggregation of usage metrics (daily, weekly, monthly granularity)
- Trend visualization in Thymeleaf templates (charts via JS library)
- Pre-built report templates: monthly cost summary, tool adoption trends, per-team breakdown
- 12-month data retention (per project constraints)

**Dependencies:** TS-3, TS-7

**Sub-features:**
- Monthly cost trend charts (per tool, per user, org total)
- Tool adoption curves (active users over time)
- Usage intensity trends (tokens/requests per active user over time)
- Exportable PDF/CSV reports for leadership
- Month-over-month cost comparison

---

### DF-3: Google Workspace Directory Sync

**What:** Automated synchronization with Google Workspace to keep the user registry current. Pulls employee list, department info, employment status, and the github_username custom property.

**Why differentiating:** Eliminates manual user management. When someone joins or leaves the company, the system knows automatically. This is the bridge between HR reality and AI tool accounts.

**Complexity:** Medium
- Google Workspace Admin SDK integration
- Sync schedule (daily or on-demand)
- Handle: new employees, departures (mark as OFFBOARDED), department changes
- Custom property extraction for github_username mapping
- Conflict resolution: what happens when Google says someone left but they still have active AI tool accounts

**Dependencies:** TS-1

**Sub-features:**
- Scheduled directory sync (daily)
- New employee auto-registration
- Departed employee auto-flagging (status change, not account deletion)
- Department and metadata sync
- GitHub username extraction from custom property
- Sync health monitoring and error reporting

---

### DF-4: Department-Level Cost Aggregation

**What:** Aggregate costs and usage by department/team, not just by individual. Enables chargeback or showback models where engineering teams bear their own AI tool costs.

**Why differentiating:** Individual attribution is table stakes, but department aggregation enables organizational cost management and budget planning. This is what finance and engineering leadership actually want to see.

**Complexity:** Low-Medium
- Requires department field on User (from Google Workspace sync or manual entry)
- Aggregation queries: sum costs by department, by tool, by period
- Department-level dashboard view for admins

**Dependencies:** TS-1, TS-7, DF-3 (for automated department data)

**Sub-features:**
- Department field on user profile
- Department cost summary view
- Department-level usage comparison
- Department drill-down to individual users

---

### DF-5: Audit Trail

**What:** Log all administrative actions: who linked/unlinked accounts, who acknowledged inactive accounts, who changed configurations, when metrics were collected.

**Why differentiating:** Compliance and accountability. When costs are questioned, you can trace exactly what happened and who did what. Not required for MVP but critical for production maturity.

**Complexity:** Low-Medium
- Spring Data JPA Auditing for entity changes (created_by, modified_by, timestamps)
- Action log table for admin operations
- Read-only audit log viewer in admin dashboard

**Dependencies:** TS-8 (needs authenticated user context for "who did it")

**Sub-features:**
- Entity-level audit fields (created_at, updated_at, created_by, updated_by)
- Admin action log (account link/unlink, status changes, configuration changes)
- Audit log viewer (filterable by action type, user, date range)
- Scheduled task execution log (success/failure/duration)

---

### DF-6: Bulk Operations

**What:** Admin ability to perform operations on multiple users/accounts at once: bulk deactivate accounts, bulk export, bulk acknowledge inactive.

**Why differentiating:** With dozens or hundreds of developers, one-at-a-time management doesn't scale. Bulk operations transform admin experience from tedious to efficient.

**Complexity:** Low-Medium
- Checkbox selection in user/account tables
- Bulk action dropdown: deactivate, export, acknowledge
- Confirmation dialog with summary of affected records
- Background processing for large bulk operations

**Dependencies:** TS-4 (Admin Dashboard)

**Sub-features:**
- Multi-select in user and account tables
- Bulk deactivate/reactivate accounts
- Bulk export selected users/accounts to CSV
- Bulk acknowledge inactive accounts
- Progress indicator for long-running bulk operations

---

### DF-7: API-First Design with External Access

**What:** Well-documented REST API (OpenAPI/Swagger already scaffolded) that allows external systems to query usage data and costs programmatically. Enables integration with BI tools, Slack bots, or internal portals.

**Why differentiating:** The web dashboard serves human users; the API serves machines. This unlocks integrations that aren't part of the initial scope but create long-term platform value.

**Complexity:** Low (infrastructure exists)
- OpenAPI documentation already configured (SpringDoc)
- Swagger UI already enabled
- API endpoints mirror dashboard functionality
- Authentication via JWT for API consumers

**Dependencies:** TS-4, TS-5, TS-8

**Sub-features:**
- Comprehensive REST API for all read operations
- API key support for service-to-service auth (beyond JWT)
- Rate limiting for API consumers
- Versioned API endpoints

---

### DF-8: Configurable Dashboard Widgets

**What:** Allow admins to customize which metrics appear prominently on the dashboard. Pin most-important KPIs, hide irrelevant ones, rearrange layout.

**Why differentiating:** Different admins care about different things. A finance-oriented admin cares about costs; an engineering lead cares about adoption. Customization serves both without building separate dashboards.

**Complexity:** Medium-High
- Widget system with drag-and-drop (requires significant JS work)
- User-specific dashboard configuration stored in database
- Widget catalog: cost summary, active users, inactive accounts, trend charts, tool breakdown

**Dependencies:** TS-4

**Sub-features:**
- Widget catalog with predefined metrics
- Dashboard layout persistence per user
- Drag-and-drop arrangement
- Show/hide toggle per widget

---

## Anti-Features

Things to deliberately NOT build. Each has a clear rationale for exclusion.

### AF-1: Real-Time Usage Tracking

**Do not build.** Daily batch collection is explicitly sufficient for cost control. Real-time adds:
- Massive complexity (WebSockets, streaming APIs, event processing)
- Higher API rate limit consumption
- Marginal value (cost decisions are made monthly, not per-second)
- Per PROJECT.md: "Real-time usage tracking -- Daily batch collection is sufficient for cost control"

---

### AF-2: Automatic Account Provisioning/Deprovisioning

**Do not build.** The system detects and reports; it does not act. Reasons:
- Auto-disabling accounts risks disrupting active developers
- Each tool has different provisioning mechanisms
- Manual control is safer for a first version
- Per PROJECT.md: "Automatic account provisioning -- Manual provisioning with centralized tracking is enough" and "Automated offboarding -- Manual offboarding process remains, system provides visibility only"

---

### AF-3: Multi-Tenancy

**Do not build.** This is built specifically for Bemobi's needs. Multi-tenancy adds:
- Massive data isolation complexity
- Tenant management overhead
- No current business need
- Per PROJECT.md: "Multi-tenancy for other companies -- Built specifically for Bemobi's needs"

---

### AF-4: Mobile Application

**Do not build.** Web dashboard covers all use cases. Mobile adds:
- Separate codebase or responsive redesign effort
- Push notification infrastructure
- No demonstrated need (admins work from desktops)
- Per PROJECT.md: "Mobile app -- Web dashboard covers admin and developer use cases"

---

### AF-5: AI Tool Configuration Management

**Do not build.** The system tracks usage, not tool configuration. Do not attempt to:
- Configure AI tool settings (model selection, context limits, etc.) through this dashboard
- Manage IDE plugin settings
- Control which AI features are enabled per tool
This is each tool's own admin console's job.

---

### AF-6: Code Quality or Output Analysis

**Do not build.** The system measures quantity (tokens, costs, frequency), not quality. Do not attempt to:
- Analyze the quality of AI-generated code
- Measure code acceptance rates beyond what provider APIs expose natively
- Build code review or AI output assessment features
This is out of scope and would require entirely different infrastructure.

---

### AF-7: SPA Frontend / React/Vue Migration

**Do not build.** Thymeleaf server-side rendering is the decided approach. Do not migrate to:
- React, Vue, Angular, or any SPA framework
- Per project key decisions: "Thymeleaf server-side rendering -- Simpler than SPA, adequate for internal dashboard, leverages Spring Boot integration"
Server-side rendering is adequate for an internal tool with limited concurrent users.

---

### AF-8: Cross-Company Benchmarking

**Do not build.** The system shows Bemobi's own data. Do not attempt to:
- Compare Bemobi's AI spending to industry averages
- Provide benchmarking against peer companies
- Aggregate anonymous data across organizations
There is no data source for this and it's not the problem being solved.

---

## Feature Dependency Map

```
TS-1: User Registry (foundational)
  |
  +-- TS-2: Account Linking
  |     |
  |     +-- TS-3: Metrics Collection
  |           |
  |           +-- TS-4: Admin Dashboard
  |           |     |
  |           |     +-- DF-6: Bulk Operations
  |           |     +-- DF-8: Configurable Widgets
  |           |
  |           +-- TS-5: Developer View
  |           |
  |           +-- TS-6: Inactive Account Detection
  |           |     |
  |           |     +-- DF-1: Alert System
  |           |
  |           +-- TS-7: Cost Estimation
  |                 |
  |                 +-- DF-2: Trend Analytics
  |                 +-- DF-4: Department Costs
  |
  +-- DF-3: Google Workspace Sync
  |     |
  |     +-- DF-4: Department Costs (automated dept data)
  |
  +-- TS-8: Authentication
        |
        +-- DF-5: Audit Trail
        +-- DF-7: API-First Design
```

---

## Implementation Priority Matrix

| Priority | Feature | Category | Complexity | Rationale |
|----------|---------|----------|------------|-----------|
| P0 | TS-1: User Registry | Table Stakes | Low-Med | Foundational; everything depends on this |
| P0 | TS-8: Authentication | Table Stakes | Medium | Required for any secure access |
| P0 | TS-2: Account Linking | Table Stakes | Medium | Required before metrics make sense |
| P1 | TS-3: Metrics Collection | Table Stakes | High | Data pipeline; highest complexity |
| P1 | TS-7: Cost Estimation | Table Stakes | Med-High | Core value: cost visibility |
| P1 | TS-6: Inactive Detection | Table Stakes | Medium | Core value: zero orphaned accounts |
| P2 | TS-4: Admin Dashboard | Table Stakes | Medium | Primary UI; depends on data pipeline |
| P2 | TS-5: Developer View | Table Stakes | Low-Med | Secondary UI; simpler than admin |
| P3 | DF-3: Google Workspace Sync | Differentiator | Medium | High ROI for user management automation |
| P3 | DF-1: Alert System | Differentiator | Med-High | Transforms passive to proactive tool |
| P3 | DF-5: Audit Trail | Differentiator | Low-Med | Production maturity requirement |
| P4 | DF-2: Trend Analytics | Differentiator | Medium | Leadership reporting value |
| P4 | DF-4: Department Costs | Differentiator | Low-Med | Chargeback/showback enablement |
| P4 | DF-6: Bulk Operations | Differentiator | Low-Med | Scale-dependent; small team may not need |
| P5 | DF-7: API-First Design | Differentiator | Low | Infrastructure exists; low effort to formalize |
| P5 | DF-8: Configurable Widgets | Differentiator | Med-High | Polish feature; lowest priority |

---

## Risk Assessment by Feature

| Feature | Primary Risk | Mitigation |
|---------|-------------|------------|
| TS-3: Metrics Collection | Cursor API maturity; may not expose needed data | Design fallback: CSV import, manual entry |
| TS-3: Metrics Collection | Provider API changes break collection | Version-pin API clients; monitor for deprecations |
| TS-7: Cost Estimation | Pricing models change frequently | Make pricing configurable by admin, not hardcoded |
| TS-2: Account Linking | GitHub username mapping unreliable | Google Workspace custom property is the bridge; validate during sync |
| DF-3: Google Workspace Sync | Google Admin SDK permissions may be restricted | Verify API access early; have manual fallback |
| DF-1: Alert System | Alert fatigue if thresholds poorly calibrated | Start with minimal alerts; add tuning capability |

---

## Open Questions

1. **Cursor API availability:** What exactly does Cursor's team/business API expose? Is there a documented admin API, or will this require alternative data collection methods?

2. **Cost data granularity:** Do Anthropic and GitHub APIs provide actual billed amounts, or only usage quantities that must be multiplied by published pricing?

3. **Google Workspace API access:** Does Bemobi's Google Workspace admin grant programmatic access to the Admin SDK, including custom user properties?

4. **Department structure:** Is Bemobi's organizational structure flat or hierarchical? Does Google Workspace have department/team data that can be used for DF-4?

5. **Historical data backfill:** How far back can provider APIs report usage? Can the system backfill historical data from before deployment?

6. **Budget ownership:** Who owns the AI tool budget at Bemobi -- engineering, finance, individual teams? This affects whether DF-4 (department costs) should be prioritized higher.

---

*Research completed: 2026-02-24*
