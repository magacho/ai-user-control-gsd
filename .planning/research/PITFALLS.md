# Pitfalls Analysis: AI User Control

**Research Date:** 2026-02-24
**Dimension:** Common mistakes, failure modes, and prevention strategies for AI usage tracking, multi-provider integration, cost management, and user identity mapping.

---

## Pitfall 1: GitHub Username-to-Email Mapping Fragility

**Category:** Identity Resolution
**Severity:** Critical
**Phase:** Phase 1 (Foundation / User Management)

### The Problem

The project relies on Google Workspace custom properties to store `github_username` for each user, bridging the gap between corporate email (`user@bemobi.com`) and GitHub identity. This is the single most fragile link in the entire system. If the custom property is empty, stale, or wrong, GitHub Copilot usage cannot be attributed to any user, creating cost blind spots.

Common failures:
- Developers change their GitHub username but nobody updates the Workspace property.
- New hires are added to Google Workspace before the `github_username` field is populated (the field is custom, not part of standard onboarding flows).
- Developers use personal GitHub accounts for some activities, with their org-linked account being different.
- The custom property field name in Google Workspace is misspelled or uses inconsistent casing (`github_username` vs `githubUsername` vs `GitHub_Username`), causing silent lookup failures.

### Warning Signs

- Usage metrics collection completes but the number of users with GitHub Copilot data is significantly lower than the number of known Copilot seat assignments.
- Copilot usage reports show "unmatched" or "unknown" users that correspond to valid org members.
- A developer who is actively using Copilot shows zero usage in the dashboard.
- Google Workspace API returns empty custom schemas for users who definitely have GitHub accounts.

### Prevention Strategy

1. **Validate on ingest, not on query.** When syncing users from Google Workspace, log an explicit warning for every user where `github_username` is null or empty. Surface this as a dashboard metric ("N users missing GitHub mapping").
2. **Cross-validate against GitHub org membership.** After fetching the org member list from GitHub API (`GET /orgs/{org}/members`), compare it against the set of mapped usernames. Report any GitHub org members who have no corresponding internal user, and any internal users whose mapped username is not in the org.
3. **Normalize the custom property access.** Define the exact Google Workspace custom schema name and field name as a configuration constant. Never rely on string matching at query time. Validate the schema exists at application startup.
4. **Build a manual override mechanism.** Allow admins to manually set or correct the GitHub username for a user through the dashboard, independent of Google Workspace sync. Store the override locally and prefer it over the Workspace value.
5. **Schedule periodic reconciliation alerts.** A weekly job that compares Copilot seat count vs. mapped user count and emails admins if the delta exceeds a threshold (e.g., more than 10% unmatched).

---

## Pitfall 2: Provider API Rate Limit Exhaustion During Batch Collection

**Category:** Integration Reliability
**Severity:** High
**Phase:** Phase 2 (Integration Layer)

### The Problem

The system collects metrics from three providers (Claude/Anthropic, GitHub, Cursor) during a nightly batch window. Each provider has different rate limits:

- **Anthropic Admin API:** Rate limits vary by tier; the admin/usage endpoints may have stricter limits than the chat completions API. Paginated responses require multiple calls per time period.
- **GitHub REST API:** 5,000 requests/hour for authenticated requests. Copilot usage endpoints for organizations require multiple paginated calls (by day, by user). For an org with 100+ developers, fetching 30 days of daily breakdown can require hundreds of calls.
- **Cursor:** API documentation is limited and may change. Rate limits are poorly documented or nonexistent in public docs, meaning you only discover them at runtime.

The common mistake is treating the nightly batch as a simple "loop over all users and fetch." Without rate-limit awareness, the job hits 429 responses, fails partway through, and leaves metrics partially collected with no way to resume.

### Warning Signs

- Scheduled job logs show HTTP 429 (Too Many Requests) responses from any provider.
- Metrics collection job takes progressively longer each night as the user base grows.
- Partial data: some users have metrics for a given day, others do not, and there is no failed-job alert.
- GitHub API responses include `X-RateLimit-Remaining: 0` headers.

### Prevention Strategy

1. **Implement per-provider rate-limit tracking.** Parse `X-RateLimit-Remaining`, `X-RateLimit-Reset`, and `Retry-After` headers from every API response. Store current limits in memory and throttle proactively before hitting the wall.
2. **Use exponential backoff with jitter on 429 responses.** Do not simply retry immediately. Back off with randomized delays (e.g., 1s, 2s, 4s, 8s with +/- 20% jitter) up to a configurable maximum (e.g., 5 retries).
3. **Design the batch job as resumable.** Track which users/time-periods have been successfully collected. If the job fails partway, the next run should resume from where it left off rather than restarting from scratch.
4. **Stagger provider calls.** Do not hit all three providers simultaneously at 2 AM. Schedule Claude at 2:00, GitHub at 2:20, Cursor at 2:40. This spreads the load and makes debugging easier.
5. **Add circuit breaker per provider.** If a provider returns 5 consecutive errors, stop calling it for that batch run and raise an alert. Do not let one broken provider block collection from the others (use Resilience4j or similar).
6. **Pre-calculate required API calls.** Before starting a collection run, estimate the number of API calls needed (users x days x endpoints). If the estimate exceeds available rate limits, split across multiple runs or reduce the time window.

---

## Pitfall 3: Cost Calculation Drift from Actual Provider Billing

**Category:** Data Accuracy
**Severity:** Critical
**Phase:** Phase 3 (Usage Metrics & Cost Reporting)

### The Problem

AI providers frequently change their pricing. The system must estimate costs based on token consumption or seat-based billing, but hardcoded rates drift from reality:

- **Claude:** Token-based pricing with different rates for input vs. output tokens, and rates that vary by model (Haiku vs. Sonnet vs. Opus). Anthropic has changed pricing multiple times. Cached input tokens have a different rate than non-cached tokens.
- **GitHub Copilot:** Seat-based pricing ($19/user/month for Individual, $39/user/month for Business as of early 2025, but these change). Copilot also now has metered billing for premium models. The API does not return dollar costs directly; you must calculate them.
- **Cursor:** Subscription tiers (free, pro, business) with different feature sets. Usage-based pricing for certain features. Pricing and tiers change frequently.

The critical mistake is calculating costs once and assuming the formula stays correct. Over 12 months (the required data retention period), outdated rates produce materially wrong cost reports that undermine the system's core value proposition of "transparent AI tool costs."

### Warning Signs

- Finance team reports that the dashboard total for a provider does not match the provider's actual invoice.
- Cost-per-user numbers seem too low or too high compared to manual spot-checks.
- A pricing change is announced by a provider but the dashboard shows no corresponding shift in costs.
- Monthly cost trends show suspiciously smooth lines with no variance (suggests stale static rates).

### Prevention Strategy

1. **Externalize pricing as configuration, not code.** Store pricing rates in a database table (`provider_pricing`) with effective dates, not as constants in `TokenCalculator.java`. This allows updating rates without redeployment.
2. **Version pricing with effective dates.** Each pricing record should have `effective_from` and `effective_until` timestamps. When calculating historical costs, use the rate that was in effect on that date, not the current rate.
3. **Separate raw metrics from calculated costs.** Store raw usage data (tokens, minutes, seats) separately from dollar amounts. Recalculate costs on-the-fly or in a separate materialized view. This allows retroactive correction when rates are updated.
4. **Add a manual invoice reconciliation feature.** Allow admins to input the actual invoice amount from each provider monthly. Display the delta between estimated and actual cost, surfacing the drift immediately.
5. **Alert on pricing staleness.** If a provider's pricing configuration has not been reviewed/updated in 90 days, generate an admin alert. Include links to provider pricing pages for quick verification.
6. **Account for model-level granularity.** For token-based providers (Claude), track which model was used per request. The cost difference between Haiku and Opus is 50-100x. Aggregating all tokens at one rate produces wildly inaccurate costs.

---

## Pitfall 4: Google Workspace API Quota and Custom Schema Access Errors

**Category:** Integration Reliability
**Severity:** High
**Phase:** Phase 1 (Foundation / User Sync)

### The Problem

Google Workspace Admin SDK has specific requirements that commonly trip up developers:

- **Custom schemas must be explicitly projected.** Calling `users.list()` or `users.get()` without specifying `projection=custom` and the `customFieldMask` parameter returns standard fields only. The `github_username` custom property will be silently absent, and the code will interpret this as "user has no GitHub username" rather than "we forgot to request it."
- **API quotas are per-project, not per-API-key.** The Admin SDK has a default quota of roughly 2,400 queries per minute for the Directory API. If other services in the same GCP project consume quota, user sync can fail.
- **Domain-wide delegation is required for service accounts.** If the application uses a service account (likely for server-to-server calls), it must be granted domain-wide delegation with the correct scopes. Missing scopes produce cryptic 403 errors that look like permission issues on the user rather than the service account.
- **Pagination is mandatory.** The Users list endpoint returns a maximum of 500 results per page. Organizations with more than 500 users require pagination. Forgetting pagination means silently losing users.

### Warning Signs

- User sync reports fewer users than expected (missing custom properties or incomplete pagination).
- The `github_username` field is null for all users even though it is populated in the Google Workspace Admin console.
- Intermittent 403 or 429 errors from the Directory API during user sync.
- User sync succeeds in development (small number of test users) but fails in production (real org size).

### Prevention Strategy

1. **Explicitly test custom schema access in integration tests.** Create a test that fetches a known user with a custom property and asserts the property is returned. This catches projection misconfigurations early.
2. **Log the full API request URL and projection parameters** at DEBUG level during user sync. When debugging missing data, the first thing to check is whether `projection=custom` was actually sent.
3. **Implement pagination from day one.** Never assume the org is small enough to fit in one page. Use a `while (nextPageToken != null)` pattern with the Google API client.
4. **Validate API scopes at startup.** On application startup, make a simple test call to the Directory API (e.g., fetch the authenticated service account's own profile) and verify it succeeds. Fail fast with a clear error message if scopes are misconfigured.
5. **Use a dedicated GCP project** for this application's API quotas. This prevents quota competition with other internal tools.

---

## Pitfall 5: Scheduled Job Failures Silently Corrupt Data State

**Category:** Data Integrity
**Severity:** Critical
**Phase:** Phase 2 (Integration Layer) / Phase 3 (Usage Metrics)

### The Problem

The application uses Spring `@Scheduled` tasks for nightly metrics collection and inactive account detection. Spring's default scheduler behavior is dangerous for this use case:

- **No built-in retry.** If the job throws an exception, it is logged and the scheduler moves on. The next run starts a new collection cycle from scratch.
- **No idempotency by default.** If a job partially succeeds (collected metrics for 40 of 100 users, then crashed), running the job again inserts duplicate records for the first 40 users. Without deduplication logic, usage metrics double-count.
- **Single-threaded by default.** Spring's `@Scheduled` uses a single-thread executor. If the metrics collection job runs long (due to rate limiting or retries), it blocks the inactive account checker and any other scheduled task.
- **No distributed locking.** If the application runs multiple instances (e.g., for high availability), both instances will execute the scheduled job simultaneously, causing duplicate API calls and duplicate data.

### Warning Signs

- Usage metrics show sudden spikes (2x or 3x normal values) on certain days, indicating duplicate collection.
- Metrics for certain users are consistently missing, suggesting the job fails at the same point each time.
- The inactive account check never runs, because the metrics collection job always overruns its time window.
- In a multi-instance deployment, providers report double the expected API call volume.

### Prevention Strategy

1. **Make metric collection idempotent.** Use a composite unique constraint on `(user_id, tool_id, metric_date, metric_type)` in the `usage_metrics` table. Use `INSERT ... ON CONFLICT DO UPDATE` (PostgreSQL upsert) rather than blind inserts.
2. **Track collection state explicitly.** Maintain a `collection_runs` table that records: run_id, provider, start_time, end_time, status (running/completed/failed), last_processed_user. This enables resumable collection.
3. **Configure a multi-threaded scheduler.** In `SchedulingConfig.java`, configure a `ThreadPoolTaskScheduler` with at least 3 threads (one per concern: metrics collection, inactive checks, future tasks). This prevents one long-running job from blocking others.
4. **Implement distributed locking.** Use PostgreSQL advisory locks (`SELECT pg_try_advisory_lock(lock_id)`) or Spring's `@SchedulerLock` (via ShedLock library with JDBC backend) to ensure only one instance runs each job.
5. **Wrap every scheduled method in try-catch.** Log the error, record the failure in the collection_runs table, and send an alert. Never let a scheduled method propagate exceptions to the scheduler framework.
6. **Set execution timeouts.** If the metrics collection job has not completed within 2 hours, kill it and alert. This prevents zombie jobs that hold database connections indefinitely.

---

## Pitfall 6: Cursor API Instability and Lack of Official Enterprise API

**Category:** Integration Risk
**Severity:** High
**Phase:** Phase 2 (Integration Layer)

### The Problem

Unlike Anthropic and GitHub, Cursor does not (as of early 2026) provide a stable, well-documented enterprise usage API. Common mistakes:

- **Assuming all three providers have comparable APIs.** Claude and GitHub have mature REST APIs with versioning, documentation, and stable pagination. Cursor's API surface is limited and may rely on undocumented endpoints, web scraping, or manual CSV exports.
- **Building a generic integration pattern and forcing Cursor into it.** The abstraction of "call API, get JSON, parse metrics" may not apply to Cursor. It may require browser automation, manual data import, or polling a different data source entirely.
- **Investing equal development effort across all three providers.** If Cursor's API changes or disappears, the integration breaks and the development effort is wasted.

### Warning Signs

- Cursor API endpoints return different response formats between minor version updates.
- No official Cursor API documentation exists for the specific endpoints being used.
- Cursor integration tests pass against a mock but fail against the real API.
- The `CURSOR_API_URL` has no default value in the configuration (already visible in `application.yml`), indicating uncertainty about the endpoint.

### Prevention Strategy

1. **Design the Cursor integration as a pluggable adapter from the start.** Use the Strategy pattern so that Cursor data can come from API calls, CSV import, manual entry, or any future mechanism without changing the rest of the system.
2. **Implement a manual data import feature for Cursor.** Allow admins to upload CSV/JSON usage reports from Cursor's admin console. This serves as the fallback when the API is unavailable or unreliable.
3. **Decouple Cursor collection from the main batch job.** If Cursor's API is down, Claude and GitHub collection should proceed unaffected. Use the circuit breaker pattern and treat Cursor failures as non-blocking.
4. **Gate Cursor integration behind its feature flag** (`CURSOR_INTEGRATION_ENABLED`). The configuration already has this. Ensure the rest of the system gracefully shows "no data available" rather than errors when Cursor is disabled.
5. **Document the specific Cursor API endpoints and response formats** being used. When the API changes, having a baseline makes it faster to identify what broke.

---

## Pitfall 7: Token Counting Inconsistencies Across Providers

**Category:** Data Accuracy
**Severity:** Medium
**Phase:** Phase 3 (Usage Metrics & Cost Reporting)

### The Problem

The system aggregates "token consumption" as a cross-provider metric, but tokens mean different things across providers:

- **Claude (Anthropic):** Reports input tokens and output tokens separately. Uses its own tokenizer (not tiktoken). Cached input tokens are tracked separately. System prompt tokens are counted.
- **GitHub Copilot:** Does not expose token counts in its usage API. Usage is measured in "suggestions accepted," "lines suggested," "acceptance rate," and time-based activity data. There is no direct token-to-cost mapping because Copilot is seat-based.
- **Cursor:** May report tokens, completions, or a proprietary metric. The unit depends on the plan tier and the underlying model being used.

The common mistake is creating a `UsageMetric` entity with a single `tokens` field and trying to normalize everything into it. This produces meaningless cross-provider comparisons and incorrect cost calculations.

### Warning Signs

- Dashboard shows GitHub Copilot with zero tokens but high activity (because tokens are not the right metric).
- Cost-per-token calculations for Copilot produce nonsensical numbers (dividing seat cost by zero tokens).
- Aggregated "total tokens across all tools" metrics are meaningless because they mix different units.
- The `TokenCalculator.java` utility applies the same formula to all providers.

### Prevention Strategy

1. **Use provider-specific metric types.** The `UsageMetricType` enum should not be a single "TOKENS" value. Define types per provider: `CLAUDE_INPUT_TOKENS`, `CLAUDE_OUTPUT_TOKENS`, `COPILOT_SUGGESTIONS_ACCEPTED`, `COPILOT_ACTIVE_DAYS`, `CURSOR_COMPLETIONS`, etc.
2. **Store raw provider data.** Include a JSON column or separate fields for the provider's native metrics. Never discard data to fit a generic schema.
3. **Calculate costs per provider, not per token.** Claude cost = (input_tokens * input_rate) + (output_tokens * output_rate). Copilot cost = seat_price / month. Cursor cost = subscription_tier_price. These are fundamentally different formulas.
4. **Display per-provider dashboards.** The dashboard should show provider-specific metrics in their native units, with cost as the only common denominator. Do not attempt to show a unified "tokens used" chart.
5. **Design the `usage_metrics` table with a flexible schema.** Use a `metric_type` discriminator and `metric_value` numeric field, with an optional `metadata` JSONB column for provider-specific details.

---

## Pitfall 8: Orphaned Account Detection with High False-Positive Rate

**Category:** Business Logic
**Severity:** Medium
**Phase:** Phase 3 (Usage Metrics) / Phase 4 (Reporting & Alerts)

### The Problem

The system detects inactive accounts using a threshold (default: 30 days of no usage). This sounds simple but produces false positives that erode trust in the system:

- Developers on extended leave (parental, medical, sabbatical) show as inactive.
- Developers who primarily review code or manage projects may use Copilot infrequently but legitimately.
- API failures that prevent metric collection for a user make them appear inactive even though they are actively using the tools.
- Different tools have different natural usage frequencies. A developer might use Claude Code daily but Cursor weekly.

If the system sends alerts to disable accounts for developers who are on parental leave or whose metrics simply were not collected, administrators will stop trusting the alerts and ignore them.

### Warning Signs

- Admin receives inactive account alerts for developers known to be actively working.
- Alert volume is consistently high (more than 20% of users flagged monthly).
- Alerts correlate with days when metrics collection failed (same users who show as "inactive" are the ones whose data was not collected).
- No distinction between "no data" and "confirmed zero usage."

### Prevention Strategy

1. **Distinguish between "no usage" and "no data."** If the metrics collection job failed for a user/provider, mark the metric as `COLLECTION_FAILED`, not as zero usage. Only flag as inactive when you have successful collection with confirmed zero activity.
2. **Allow configurable exclusion lists.** Admins should be able to mark users as "on leave" or "exempt from inactivity checks" with an optional end date.
3. **Use per-tool inactivity thresholds.** A developer inactive on Cursor for 30 days is normal. A developer inactive on all three tools for 30 days is concerning. Only alert when all tools show inactivity.
4. **Require a confirmation period.** Do not alert on the first day a user crosses the threshold. Wait for 2-3 consecutive collection cycles to confirm the pattern before alerting.
5. **Cross-reference with HR data (Google Workspace).** If a user's Google Workspace account is still active and their last login to Workspace is recent, they are a current employee. The inactivity is tool-specific, not a departure indicator.
6. **Show inactivity as a report, not just alerts.** Provide a monthly inactivity report that admins review, rather than individual real-time alerts for each user crossing the threshold.

---

## Pitfall 9: Database Schema Inflexibility for Evolving Provider Data

**Category:** Architecture
**Severity:** Medium
**Phase:** Phase 1 (Foundation / Database Design)

### The Problem

The initial database schema (migrations V1-V4) defines the data model for users, tools, accounts, and metrics. Once deployed with real data and 12 months of retention, schema changes become expensive. Common mistakes:

- Designing `usage_metrics` with fixed columns that assume all providers return the same fields. When a provider adds a new metric (e.g., Copilot adds "premium model requests"), the schema requires a migration.
- Using a single `account_identifier` column in `user_ai_tool_accounts` without considering that GitHub needs a username, Claude needs an organization member ID, and Cursor needs an email or team ID.
- Not indexing for the most common query patterns (usage by user over time range, usage by tool over time range, aggregated costs by month).
- Choosing TIMESTAMP WITHOUT TIME ZONE for metric dates, then discovering that providers return UTC timestamps while the nightly job runs in the server's local timezone, creating off-by-one-day errors.

### Warning Signs

- Every new provider feature requires a database migration.
- Queries for the dashboard become increasingly complex with CASE statements to handle different metric types.
- Monthly cost reports show slightly different totals depending on which timezone the query is run from.
- Performance degrades as the `usage_metrics` table grows, because indexes do not match query patterns.

### Prevention Strategy

1. **Use JSONB columns for provider-specific data.** The `usage_metrics` table should have a structured core (user_id, tool_id, date, metric_type, metric_value) plus a `details JSONB` column for provider-specific raw data. This avoids migrations for new provider fields.
2. **Always use TIMESTAMP WITH TIME ZONE (TIMESTAMPTZ)** for all date/time columns. Store everything in UTC. Convert to local timezone only at the presentation layer.
3. **Create indexes for known query patterns from day one:**
   - `(user_id, metric_date)` for per-user time-range queries
   - `(tool_id, metric_date)` for per-tool aggregation
   - `(metric_date)` for global daily/monthly rollups
   - Consider partitioning `usage_metrics` by month for large datasets
4. **Design account identifiers as typed fields.** In `user_ai_tool_accounts`, store `external_identifier` (varchar) plus `identifier_type` (enum: USERNAME, EMAIL, ORG_MEMBER_ID). This makes it explicit what each identifier represents.
5. **Plan for data archival.** With 12 months of daily metrics for multiple users and tools, the table will grow. Design a partitioning strategy (e.g., monthly range partitioning on `metric_date`) or an archival job that moves old data to a summary table.

---

## Pitfall 10: Insufficient Error Context in Integration Clients

**Category:** Observability
**Severity:** Medium
**Phase:** Phase 2 (Integration Layer)

### The Problem

When external API calls fail, the error handling in integration clients often loses critical context:

- Catching exceptions and rethrowing with generic messages like "Failed to fetch usage data" without including the HTTP status code, response body, or the specific user/tool being queried.
- Not logging the request payload or URL parameters, making it impossible to reproduce failures.
- Swallowing errors in the batch collection loop to "keep going," but not recording which users were skipped.
- Using WebFlux reactive types (the project uses `spring-boot-starter-webflux`) without proper error handling. Reactive error signals like `onErrorResume` can silently swallow failures if not carefully implemented.

### Warning Signs

- Log files show "Failed to collect metrics" with no actionable detail about which API, which user, or what HTTP status was returned.
- Debugging requires adding temporary logging, reproducing the issue, and then removing the logging.
- Provider support asks "what was the exact request and response?" and you cannot answer.
- Metrics show gaps for random users with no explanation in the logs.

### Prevention Strategy

1. **Log structured context with every API call.** At minimum: provider name, endpoint URL, user identifier, HTTP method, response status code, response time in milliseconds. Use MDC (Mapped Diagnostic Context) in SLF4J to attach user/provider context to all log lines within a collection cycle.
2. **Store API call results in the collection_runs table.** For each user/provider combination, record: success/failure, HTTP status, error message if any, timestamp. This creates a queryable audit trail.
3. **Preserve response bodies on error.** When a provider returns a non-2xx response, log the response body (truncated to a reasonable size, e.g., 1000 characters). Provider error messages often contain actionable information ("rate limit exceeded, retry after 60 seconds," "invalid token," "user not found in organization").
4. **Use correlation IDs.** Generate a unique ID per collection run and include it in all log entries and API call headers (where supported). This makes it possible to trace a single collection cycle across all log entries.
5. **Implement health check endpoints per provider.** Expose actuator endpoints like `/actuator/health/claude`, `/actuator/health/github`, `/actuator/health/cursor` that report the last successful collection time, last error, and current circuit breaker state.

---

## Summary: Phase Mapping

| Phase | Pitfalls to Address | Priority |
|-------|---------------------|----------|
| Phase 1 - Foundation | #1 (GitHub username mapping), #4 (Google Workspace API), #9 (Database schema design) | Must address before building on top |
| Phase 2 - Integration | #2 (Rate limit exhaustion), #5 (Scheduled job failures), #6 (Cursor API instability), #10 (Error context) | Must address during integration implementation |
| Phase 3 - Metrics & Cost | #3 (Cost calculation drift), #7 (Token counting inconsistencies) | Must address during metrics/reporting implementation |
| Phase 4 - Reporting & Alerts | #8 (Orphaned account false positives) | Must address when building alerting |
| Ongoing | #3 (Pricing updates), #1 (Username reconciliation), #6 (Cursor API stability monitoring) | Continuous operational concern |

---

*Research completed: 2026-02-24*
