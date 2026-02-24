# Project Research Summary

**Project:** AI User Control
**Domain:** Enterprise AI tool usage tracking and cost management
**Researched:** 2026-02-24
**Confidence:** MEDIUM-HIGH

## Executive Summary

This project builds an enterprise admin dashboard for tracking AI tool usage (Claude Code, Cursor, GitHub Copilot) and costs across Bemobi's engineering organization. The system serves two audiences: administrators who need complete visibility into all users and costs, and developers who want self-service access to their own usage data. The core value proposition is eliminating orphaned accounts after employee departures and providing transparent per-developer cost attribution.

The recommended approach is to extend the existing Spring Boot 3.4.2 monolith with scheduled API integrations, server-side rendering (Thymeleaf + HTMX), and pre-aggregated metrics. The architecture follows a five-layer pipeline: Collection (fetch from provider APIs), Normalization (unify heterogeneous data), Storage (raw records + pre-aggregated summaries), Calculation (apply pricing and budgets), and Reporting (dashboards and exports). This pattern is standard for enterprise usage tracking systems and scales to thousands of users without requiring microservices or complex infrastructure.

The primary risk is identity resolution: mapping corporate emails to GitHub usernames via Google Workspace custom properties is fragile and will fail silently if not validated rigorously. Secondary risks include provider API instability (especially Cursor, which lacks mature enterprise APIs), cost calculation drift as pricing changes, and scheduled job failures corrupting partial data. Mitigation requires validation-first design (fail fast on missing mappings), configurable pricing (never hardcode rates), and idempotent batch jobs with explicit state tracking.

## Key Findings

### Recommended Stack

The project benefits from an established Spring Boot foundation (Java 21, Spring Boot 3.4.2, PostgreSQL 16). The critical additions are provider API clients, resilience libraries, scheduled job distribution, and caching to handle rate limits.

**Core technologies:**
- **Anthropic Java SDK (`com.anthropic:anthropic-java`)**: Official SDK released early 2025, handles authentication, retries, and token counting for Claude API integration — avoids manual API maintenance
- **GitHub API (`org.kohsuke:github-api`)**: Mature library covering full GitHub REST API including Copilot usage endpoints and billing — handles pagination, rate limiting, and OAuth out of the box
- **Resilience4j (`io.github.resilience4j:resilience4j-spring-boot3`)**: Circuit breaker, retry with exponential backoff, and rate limiting for all provider APIs — non-negotiable for production reliability against external API failures
- **ShedLock (`net.javacrumbs.shedlock:shedlock-spring`)**: Distributed lock for scheduled tasks using existing PostgreSQL as lock provider — ensures only one instance runs nightly collection in multi-instance deployments
- **Caffeine cache (`com.github.ben-manes.caffeine:caffeine`)**: In-memory cache for user profiles, org data, and rate limit status — essential to stay within provider rate limits without external infrastructure like Redis
- **Thymeleaf + HTMX**: Server-side rendering for admin dashboard — avoids SPA build toolchain complexity while still enabling dynamic UI updates for internal tool use case

**Critical exclusions (do NOT add):**
- Redis caching (infrastructure overhead for single-instance internal tool)
- Quartz Scheduler (over-engineered for simple periodic polling)
- React/Angular/Vue (adds build complexity; server-side rendering is adequate)
- Netflix Hystrix (abandoned; use Resilience4j instead)

### Expected Features

The feature set divides cleanly into table stakes (required for MVP) and differentiators (add after core works).

**Must have (table stakes):**
- **User Registry with corporate email key** — foundational data model linking @bemobi.com emails to tool accounts
- **AI Tool Account Linking** — maps corporate emails to provider-specific identifiers (GitHub username from Google Workspace custom property, Claude org member ID, Cursor email/team ID)
- **Automated daily metrics collection** — nightly batch job (2 AM default) pulling usage from Claude, GitHub, Cursor APIs with resilience patterns
- **Admin dashboard with full visibility** — server-side rendered view showing all users, all tools, aggregated costs, inactive accounts
- **Developer self-service view** — restricted view where developers see only their own usage and costs
- **Inactive account detection** — automated process flagging accounts with no activity within 30 days (configurable)
- **Cost estimation and attribution** — translate raw metrics (tokens, seats) to dollars using configurable pricing models, roll up per user/tool/month
- **JWT authentication with role-based access** — ADMIN role sees everything, DEVELOPER role sees own data only

**Should have (competitive advantage):**
- **Google Workspace directory sync** — automated user list sync with github_username custom property extraction, handles new hires and departures
- **Alert system for anomalies** — proactive notifications for cost spikes, inactive accounts, unusual usage patterns
- **Usage trend analytics** — historical analysis showing AI tool adoption curves and cost forecasting over time
- **Department-level cost aggregation** — enables chargeback/showback models for engineering teams
- **Audit trail** — log all admin actions (link/unlink accounts, config changes) for compliance

**Defer (v2+):**
- Real-time usage tracking (daily batch is sufficient for cost control)
- Automatic account provisioning/deprovisioning (system detects and reports; humans act)
- Multi-tenancy (built specifically for Bemobi)
- Mobile application (web dashboard covers use cases)
- Code quality analysis (measures quantity, not quality of AI outputs)

### Architecture Approach

Enterprise usage tracking systems follow a pipeline pattern with five layers processing data left-to-right: Collection fetches raw data from provider APIs via scheduled jobs; Normalization transforms heterogeneous formats into a unified schema and resolves user identities; Storage persists both raw records and pre-aggregated summaries (daily/monthly rollups); Calculation applies pricing rules and budget policies to produce costs and alerts; Reporting exposes data through dashboards, APIs, and exports. This unidirectional flow with no backward dependencies (except alerting) enables independent layer development and testing.

**Major components:**
1. **Collection Layer** — Provider API clients (one per provider implementing `UsageDataClient` interface), scheduled collectors using `@Scheduled` with ShedLock distributed locking, gateway log ingestor for any proxy-based usage capture
2. **Normalization Layer** — Usage normalizer (maps provider-specific fields to canonical `NormalizedUsageRecord` schema), user-to-account mapper (resolves which internal user owns an API key), model registry (maintains pricing tiers and token types per model)
3. **Storage Layer** — Usage record repository (append-heavy, partitioned by month), aggregation engine (batch job materializing daily/monthly summary tables), time-series store (same RDBMS with materialized views)
4. **Calculation Layer** — Cost calculator (applies per-token/per-seat pricing with historical rate versioning), pricing rule engine (configuration-driven with effective dates), budget tracker (compares accumulated costs against limits), alert generator (publishes to email/Slack channels)
5. **Reporting Layer** — Dashboard API (REST endpoints serving pre-aggregated data only), detail drill-down API (paginated raw records for investigation), export service (async CSV/PDF generation), admin configuration API (CRUD for budgets, mappings, pricing)

### Critical Pitfalls

**Top 5 to address proactively:**

1. **GitHub username-to-email mapping fragility (CRITICAL)** — The Google Workspace custom property `github_username` is the sole bridge between corporate identity and GitHub Copilot usage. If empty, stale, or wrong, Copilot costs cannot be attributed. Mitigation: validate on ingest (log warnings for missing mappings), cross-validate against GitHub org member list, build manual override mechanism, schedule weekly reconciliation alerts comparing Copilot seat count to mapped user count.

2. **Provider API rate limit exhaustion during batch collection (HIGH)** — Nightly batch can hit 429 responses if not rate-aware. GitHub limits to 5,000 req/hr; fetching 30 days of per-user Copilot data for 100+ developers requires hundreds of calls. Mitigation: parse `X-RateLimit-Remaining` headers proactively, exponential backoff with jitter on 429, resumable batch jobs (track which users completed), stagger provider calls (Claude 2:00, GitHub 2:20, Cursor 2:40), circuit breaker per provider.

3. **Cost calculation drift from actual provider billing (CRITICAL)** — Hardcoded pricing rates go stale as providers change pricing. Claude changed rates multiple times in 2025. Over 12 months retention, this produces materially wrong reports. Mitigation: externalize pricing to database table with effective dates, version pricing (use rate in effect on usage date), separate raw metrics from calculated costs (allows recalculation), monthly invoice reconciliation feature (compare estimated vs actual), alert on pricing staleness (90+ days without review).

4. **Scheduled job failures silently corrupting data state (CRITICAL)** — Spring's default `@Scheduled` has no retry, no idempotency, and single-threaded execution. Partial failures (collected 40 of 100 users, then crashed) produce duplicate data on re-run. Mitigation: make collection idempotent (unique constraint on user+tool+date+metric, use upsert), track collection state in `collection_runs` table, configure multi-threaded scheduler (3+ threads), implement distributed locking with ShedLock, wrap scheduled methods in try-catch with explicit failure recording.

5. **Cursor API instability and lack of official enterprise API (HIGH)** — Unlike Claude/GitHub, Cursor has no documented stable enterprise usage API as of early 2026. Integration may rely on undocumented endpoints, web scraping, or manual CSV import. Mitigation: design Cursor integration as pluggable adapter (Strategy pattern), implement manual CSV import feature as fallback, decouple from main batch job (circuit breaker pattern), gate behind feature flag (`CURSOR_INTEGRATION_ENABLED`), document specific endpoints being used for rapid debugging when they change.

**Additional concerns:**
- Google Workspace API quota exhaustion and custom schema access errors (must use `projection=custom` explicitly, implement pagination from day one, validate scopes at startup)
- Token counting inconsistencies across providers (use provider-specific metric types, never normalize to single "tokens" field)
- Orphaned account detection with high false-positive rate (distinguish "no usage" from "no data", allow exclusion lists for employees on leave, require confirmation period)
- Database schema inflexibility (use JSONB for provider-specific data, always use TIMESTAMPTZ, index for known query patterns, plan partitioning by month)

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Foundation & User Management
**Rationale:** Everything depends on the canonical data model and user-to-account mapping. Build the schema, user registry, Google Workspace sync, and authentication before any provider integration. This phase addresses Pitfall #1 (GitHub username mapping) and Pitfall #9 (database schema design) upfront.

**Delivers:** Database schema with migrations, User and UserAiToolAccount entities, Google Workspace Admin SDK integration for user sync (with github_username custom property extraction), JWT authentication with ADMIN/DEVELOPER roles, manual user management UI.

**Addresses features:** TS-1 (User Registry), TS-8 (Authentication), DF-3 (Google Workspace Sync)

**Avoids pitfalls:** #1 (mapping validation from day one), #4 (correct Google API projection and scopes), #9 (flexible schema with JSONB, TIMESTAMPTZ, proper indexes)

**Research flag:** Standard Spring Boot + Google Workspace integration patterns are well-documented. No additional research needed.

---

### Phase 2: First Provider Integration (Claude/Anthropic)
**Rationale:** Start with one provider to validate the collection pipeline architecture before multiplying complexity. Claude has the most mature Java SDK and clearest token-based cost model. This phase builds the five-layer pipeline (Collection, Normalization, Storage, Calculation, Reporting) end-to-end for a single provider.

**Delivers:** `UsageDataClient` interface, Anthropic client implementation, usage normalizer, cost calculator with configurable pricing, scheduled collector with resilience patterns (Resilience4j retry/circuit-breaker, ShedLock distributed locking), usage record repository with aggregation engine, first admin dashboard showing Claude usage and costs.

**Uses stack:** Anthropic Java SDK, Resilience4j, ShedLock, Caffeine cache, Spring Scheduling

**Implements architecture:** All five layers (Collection through Reporting) for one provider

**Addresses features:** TS-2 (Account Linking for Claude), TS-3 (Metrics Collection for Claude), TS-7 (Cost Estimation for Claude), TS-4 (Admin Dashboard minimal version)

**Avoids pitfalls:** #2 (rate limit handling with backoff), #3 (configurable pricing from day one), #5 (scheduled job idempotency and state tracking)

**Research flag:** Standard Spring Batch patterns. No additional research needed unless Anthropic API changes significantly.

---

### Phase 3: GitHub Copilot Integration
**Rationale:** GitHub is the second-highest priority provider and tests the architecture's ability to handle seat-based (not token-based) pricing and username-based (not email-based) identity. This phase validates the user mapping strategy.

**Delivers:** GitHub API client implementation, Copilot-specific normalizer (suggestions accepted, active days, not tokens), GitHub username resolution via User entity mapping, seat-based cost calculation, expanded admin dashboard showing Copilot usage alongside Claude.

**Uses stack:** GitHub API (`org.kohsuke:github-api`), existing Resilience4j patterns

**Addresses features:** TS-2 (Account Linking for GitHub), TS-3 (Metrics Collection for GitHub), TS-7 (Cost Estimation for seat-based pricing)

**Avoids pitfalls:** #1 (cross-validation of GitHub usernames against org member list), #2 (GitHub has strictest rate limits; paginated Copilot API calls), #7 (provider-specific metric types, not generic tokens)

**Research flag:** GitHub Copilot Metrics API endpoints may need validation. Low priority for `/gsd:research-phase` unless API changes.

---

### Phase 4: Inactive Account Detection & Alerts
**Rationale:** Core business value (zero orphaned accounts) depends on detecting inactivity. This phase builds on complete usage data from Phase 2/3 to flag accounts crossing thresholds and send alerts.

**Delivers:** Inactive account detection scheduled job (nightly, 3 AM), configurable inactivity thresholds per tool, admin UI for inactive accounts with filters and acknowledgment, alert generator with email notifications, audit trail for account status changes.

**Addresses features:** TS-6 (Inactive Account Detection), DF-1 (Alert System for inactive accounts), DF-5 (Audit Trail)

**Avoids pitfalls:** #8 (distinguish "no usage" from "no data", exclusion lists, confirmation period before alerting)

**Research flag:** Standard alerting patterns. No additional research needed.

---

### Phase 5: Cursor Integration (with fallback design)
**Rationale:** Cursor is the highest-risk integration due to API immaturity. Defer until the pipeline is proven with two stable providers. Design as pluggable adapter with manual import fallback.

**Delivers:** Cursor API client (if stable API available), CSV import feature for manual Cursor data, Cursor-specific normalizer, feature flag to enable/disable Cursor integration, circuit breaker isolation so Cursor failures don't block Claude/GitHub.

**Addresses features:** TS-2 (Account Linking for Cursor), TS-3 (Metrics Collection for Cursor with fallback)

**Avoids pitfalls:** #6 (Strategy pattern for pluggable data source, manual import fallback, feature flag, isolated circuit breaker)

**Research flag:** HIGH PRIORITY for `/gsd:research-phase`. Cursor API availability, endpoints, and response formats must be validated before phase planning. If no API exists, this phase pivots to CSV import only.

---

### Phase 6: Developer Self-Service & Polish
**Rationale:** Admin features are higher priority than developer self-service. Once the system has complete data and admin tools work, build the developer view and remaining differentiators.

**Delivers:** Developer dashboard (restricted to own usage), usage trend analytics with charts, department-level cost aggregation (if department data available from Google Workspace), bulk operations for admins, export service (CSV/PDF reports), API documentation finalization.

**Addresses features:** TS-5 (Developer View), DF-2 (Trend Analytics), DF-4 (Department Costs), DF-6 (Bulk Operations), DF-7 (API-First Design)

**Avoids pitfalls:** None specific; focuses on UX and reporting polish.

**Research flag:** Standard Spring MVC + Thymeleaf + Chart.js patterns. No additional research needed.

---

### Phase Ordering Rationale

- **Foundation first (Phase 1):** Data model and authentication are dependencies for everything else. Google Workspace sync provides the authoritative user list and GitHub username mapping; without this, later phases cannot attribute usage.
- **One provider end-to-end (Phase 2):** Building the full pipeline for Claude validates the five-layer architecture and scheduled job patterns before multiplying providers. Mistakes discovered here are cheaper to fix than after building three parallel integrations.
- **GitHub second (Phase 3):** Tests the architecture's flexibility (seat-based vs token-based, username vs email). Validates the GitHub username mapping strategy that Phase 1 established.
- **Inactive detection fourth (Phase 4):** Requires complete usage data from at least two providers (Claude + GitHub) to reliably detect cross-tool inactivity patterns.
- **Cursor deferred (Phase 5):** Highest integration risk due to API instability. Defer until core value (Claude + GitHub tracking) is delivered. Design with fallback so manual CSV import is viable if API fails.
- **Developer features last (Phase 6):** Admin tools are higher priority (per PROJECT.md personas). Developer self-service and analytics add value after the system reliably tracks costs.

### Research Flags

**Phases needing deeper research during planning:**
- **Phase 5 (Cursor Integration):** HIGH PRIORITY. Cursor API availability, authentication, endpoints, response formats, and stability must be validated. If no stable API exists, pivot to CSV import approach. Use `/gsd:research-phase` to investigate Cursor team/business API documentation and test endpoints.

**Phases with standard patterns (skip research-phase):**
- **Phase 1 (Foundation):** Google Workspace Admin SDK and Spring Security patterns are well-documented.
- **Phase 2 (Claude Integration):** Anthropic Java SDK is official and documented; Spring Scheduling + Resilience4j are standard.
- **Phase 3 (GitHub Integration):** `org.kohsuke:github-api` library is mature with extensive documentation.
- **Phase 4 (Inactive Detection):** Standard scheduled job and alerting patterns.
- **Phase 6 (Developer UI & Polish):** Standard Spring MVC + Thymeleaf patterns.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Spring Boot 3.4.2 foundation exists; all additions are mature libraries with active maintenance (Resilience4j, ShedLock, Caffeine, Anthropic SDK, GitHub API). Only exception is Cursor integration (see below). |
| Features | HIGH | Feature research synthesized PROJECT.md requirements with enterprise usage tracking domain patterns. Table stakes vs differentiators align with project success metrics. Anti-features are explicitly scoped out. |
| Architecture | MEDIUM-HIGH | Five-layer pipeline pattern is standard for usage tracking systems and aligns with Spring Boot best practices. Build order follows dependency flow (foundation -> one provider -> additional providers). Risk: aggregation performance at scale not yet validated (plan partitioning proactively). |
| Pitfalls | MEDIUM-HIGH | Top 5 pitfalls are based on common enterprise integration failures (rate limits, identity mapping, cost drift, job failures, API instability). GitHub username mapping is a known fragile point. Mitigation strategies are specific and actionable. Risk: Cursor API stability is unknown (confidence is LOW for Cursor specifically). |

**Overall confidence:** MEDIUM-HIGH

The project benefits from an existing Spring Boot foundation and mature libraries for Claude/GitHub integration. The architecture follows proven patterns. The primary uncertainty is Cursor API availability, which is explicitly flagged as high-risk and deferred to Phase 5 with a fallback strategy.

### Gaps to Address

**During Phase 1 planning:**
- Verify Google Workspace Admin SDK access and permissions are available at Bemobi. Confirm the exact custom schema and field name for `github_username` before implementing sync.
- Validate database partitioning strategy for `usage_metrics` table. Decide: range partitioning by month, or defer until data volume becomes an issue?

**During Phase 5 planning:**
- **CRITICAL:** Research Cursor's actual API availability. This is the biggest unknown. If Cursor has no stable enterprise API, Phase 5 pivots entirely to CSV import and manual workflows.

**During implementation (all phases):**
- Establish pricing update cadence. How often should admins review Claude/GitHub/Cursor pricing for changes? Build this into operational runbooks.
- Define invoice reconciliation process. Who compares dashboard totals to actual bills monthly, and how are discrepancies resolved?

**Operational concerns (post-launch):**
- Monitor provider API changes. All three providers (especially Cursor) may change endpoints, response formats, or pricing. Establish a process for monitoring provider changelogs and API versioning.
- Plan for historical data backfill. Can provider APIs report usage from before the system's deployment date? If so, how far back, and should it be backfilled for trend analysis?

## Sources

### Primary (HIGH confidence)
- **STACK.md research (2026-02-24):** Evaluated Spring Boot 3.4.2 ecosystem, official SDKs (Anthropic Java, Google Workspace Admin SDK), mature libraries (GitHub API, Resilience4j, ShedLock). All version recommendations verified against Maven Central and Spring Boot compatibility.
- **FEATURES.md research (2026-02-24):** Synthesized from PROJECT.md requirements and enterprise AI tool management domain patterns. Table stakes features align with explicit success metrics ("zero orphaned accounts," "complete cost visibility").
- **ARCHITECTURE.md research (2026-02-24):** Based on enterprise usage tracking system patterns (Collection -> Normalization -> Storage -> Calculation -> Reporting pipeline). Validated against Spring Boot architectural best practices.
- **PITFALLS.md research (2026-02-24):** Common failure modes for multi-provider API integration, scheduled batch jobs, cost calculation, and identity mapping. Based on Spring Boot + external API integration anti-patterns.

### Secondary (MEDIUM confidence)
- Google Workspace Admin SDK documentation (official Google docs) for custom schema access and projection parameters.
- GitHub REST API documentation for Copilot usage endpoints (organization metrics API, seat management).
- Anthropic Admin API documentation (token counting, usage reporting) from official Anthropic platform docs.

### Tertiary (LOW confidence, needs validation)
- **Cursor API availability:** No official enterprise API documentation found as of early 2026. Integration approach assumes Cursor may require web scraping, undocumented endpoints, or CSV export. This must be validated during Phase 5 planning.

---
*Research completed: 2026-02-24*
*Ready for roadmap: yes*
