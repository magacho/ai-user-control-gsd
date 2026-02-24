# Architecture Research: AI Usage Tracking & Cost Management

> **Dimension:** Architecture — Enterprise usage tracking and cost management systems
> **Question:** How are enterprise usage tracking and cost management systems typically structured? What are major components and how do they interact?
> **Milestone context:** How do usage tracking dashboards integrate with existing Spring Boot infrastructure? How should scheduled metric collection, multi-provider API clients, user-to-account mapping, and cost calculation be structured?

---

## 1. System Overview

An enterprise AI usage tracking and cost management system is composed of five major subsystems that form a data pipeline: **Collection**, **Normalization**, **Storage**, **Calculation**, and **Reporting**. Data flows unidirectionally from left to right through these layers, with feedback loops only for alerting and policy enforcement.

```
┌─────────────┐    ┌──────────────┐    ┌───────────┐    ┌─────────────┐    ┌────────────┐
│  Collection  │───>│Normalization │───>│  Storage   │───>│ Calculation │───>│ Reporting  │
│   Layer      │    │   Layer      │    │   Layer    │    │   Layer     │    │   Layer    │
└─────────────┘    └──────────────┘    └───────────┘    └─────────────┘    └────────────┘
       │                                                        │                 │
       │              ┌──────────────┐                          │                 │
       └──────────────│   Alerting   │<─────────────────────────┘                 │
                      │  & Policy    │<───────────────────────────────────────────┘
                      └──────────────┘
```

---

## 2. Component Definitions and Boundaries

### 2.1 Collection Layer

**Purpose:** Acquire raw usage data from multiple AI provider APIs and internal proxy/gateway logs.

**Components:**

| Component | Responsibility | Boundary |
|-----------|---------------|----------|
| **Provider API Clients** | Fetch usage/billing data from each AI provider (OpenAI, Anthropic, Google, Azure OpenAI, etc.) | One client class per provider; each implements a common `UsageDataClient` interface |
| **Scheduled Collectors** | Trigger periodic data pulls on configurable intervals | Spring `@Scheduled` tasks or Spring Batch jobs; no business logic — only orchestration |
| **Gateway Log Ingestor** | Parse request/response logs from an API gateway or proxy that sits in front of AI calls | Reads from log files, message queues, or database tables; produces raw usage events |

**Key design decisions:**
- Each provider client is isolated behind an interface so new providers can be added without modifying collection orchestration.
- Scheduled collectors should use Spring's `@Scheduled` with configurable cron expressions stored in application configuration or database.
- For Spring Boot: define a `UsageDataClient` interface with methods like `fetchUsage(DateRange range)` returning a provider-agnostic `RawUsageRecord` list.

**Integration with Spring Boot:**
- Provider clients live as `@Service` beans, each qualified by provider name.
- A `UsageCollectorRegistry` (similar to a strategy pattern) holds all registered clients.
- Scheduled jobs use `@Scheduled` or Quartz for more complex scheduling needs (retries, distributed locking via ShedLock).

---

### 2.2 Normalization Layer

**Purpose:** Transform heterogeneous provider data into a unified internal format.

**Components:**

| Component | Responsibility | Boundary |
|-----------|---------------|----------|
| **Usage Normalizer** | Map provider-specific fields (tokens, characters, images, seconds) to a canonical schema | One normalizer per provider, implementing a shared `UsageNormalizer` interface |
| **User-to-Account Mapper** | Resolve which internal user/team/department owns a given API key or request | Lookup service against a mapping table; pure data resolution, no side effects |
| **Model Registry** | Maintain metadata about AI models (pricing tiers, token types, capabilities) | Reference data service; updated periodically from provider pricing pages or config |

**Canonical usage record schema (conceptual):**
```
NormalizedUsageRecord {
    id: UUID
    provider: enum (OPENAI, ANTHROPIC, GOOGLE, AZURE, ...)
    model: string
    timestamp: Instant
    accountId: long           // internal account
    userId: long              // internal user who made the request
    teamId: long              // department/team
    inputTokens: long
    outputTokens: long
    totalTokens: long
    otherUnits: Map<String, BigDecimal>  // images, audio seconds, etc.
    rawProviderData: JSON     // preserved for audit
    source: enum (API_FETCH, GATEWAY_LOG, MANUAL)
}
```

**Key design decisions:**
- The canonical record must be extensible (the `otherUnits` map) because AI providers constantly add new billing dimensions (cached tokens, reasoning tokens, image generation, audio).
- User-to-account mapping is its own bounded context — it will be queried by multiple layers and should be cacheable.
- Model registry should be config-driven (database or YAML) so pricing updates don't require redeployment.

---

### 2.3 Storage Layer

**Purpose:** Persist normalized records and pre-aggregated summaries for efficient querying.

**Components:**

| Component | Responsibility | Boundary |
|-----------|---------------|----------|
| **Usage Record Repository** | CRUD for individual normalized usage records | JPA/Spring Data repository; append-heavy, rarely updated |
| **Aggregation Engine** | Pre-compute rollups by time period, user, team, provider, model | Scheduled batch job that materializes summary tables |
| **Time-Series Store** | Store metric data points for trend visualization | Could be same RDBMS with materialized views, or a dedicated time-series solution |

**Data model layers:**
```
┌─────────────────────────────┐
│   Raw Usage Records         │  ← Individual API calls / billing line items
│   (usage_records table)     │     Partitioned by month for manageability
├─────────────────────────────┤
│   Hourly/Daily Aggregates   │  ← Pre-computed rollups
│   (usage_daily_summary)     │     Indexed by account, team, provider, model
├─────────────────────────────┤
│   Monthly Aggregates        │  ← Billing period summaries
│   (usage_monthly_summary)   │     Used for cost reporting and budget tracking
└─────────────────────────────┘
```

**Key design decisions:**
- Use table partitioning (by month) on the raw records table for performance. In PostgreSQL, use declarative partitioning.
- Pre-aggregation is essential — dashboards querying millions of raw records per page load will not scale.
- Aggregation jobs run after each collection cycle and are idempotent (re-runnable without duplication).
- For Spring Boot: standard Spring Data JPA repositories, with native queries for aggregation inserts.

---

### 2.4 Calculation Layer

**Purpose:** Apply pricing rules and budget policies to produce cost figures and alerts.

**Components:**

| Component | Responsibility | Boundary |
|-----------|---------------|----------|
| **Cost Calculator** | Apply pricing rules (per-token, per-image, per-request) to usage records | Stateless service; takes usage + pricing config, returns cost |
| **Pricing Rule Engine** | Manage pricing tiers, volume discounts, and time-based rate changes | Configuration-driven; supports historical rates (price at time of usage) |
| **Budget Tracker** | Compare accumulated costs against configured budgets/limits per user, team, or org | Reads aggregated cost data; emits budget utilization percentages |
| **Alert Generator** | Produce notifications when thresholds are crossed (80%, 90%, 100% of budget) | Event-driven; publishes to notification channels |

**Cost calculation flow:**
```
Usage Record ──> Lookup Model Pricing (at usage timestamp) ──> Apply Rate
                                                                   │
                    ┌──────────────────────────────────────────────┘
                    v
             Cost = (inputTokens * inputRate) + (outputTokens * outputRate)
                    + sum(otherUnits[key] * otherRates[key])
                                                                   │
                    ┌──────────────────────────────────────────────┘
                    v
             Store cost on usage record ──> Update aggregates ──> Check budgets
```

**Key design decisions:**
- Pricing must be versioned with effective dates. A rate change on Feb 1 must not retroactively change January costs.
- Cost calculation should run as part of the normalization-to-storage pipeline (inline) rather than as a separate batch, to keep data consistent.
- Budget checking is eventually consistent — it reads from aggregates, not real-time sums.
- For Spring Boot: `CostCalculationService` is a pure domain service with no infrastructure dependencies; pricing rules come from a `PricingRepository`.

---

### 2.5 Reporting Layer

**Purpose:** Expose usage and cost data to users through dashboards, APIs, and exports.

**Components:**

| Component | Responsibility | Boundary |
|-----------|---------------|----------|
| **Dashboard API** | REST endpoints serving aggregated data for UI consumption | Spring `@RestController`; reads from aggregate tables only |
| **Detail Drill-down API** | REST endpoints for viewing individual usage records | Paginated queries against raw records; used for investigation |
| **Export Service** | Generate CSV/PDF reports for billing periods | Async job that reads aggregates and produces downloadable files |
| **Admin Configuration API** | CRUD for budgets, user mappings, pricing overrides, alert thresholds | Standard REST CRUD; writes to configuration tables |

**Key design decisions:**
- Dashboard API should serve pre-aggregated data only — never scan raw records for summary views.
- Use Spring's `@Async` or a task executor for export generation to avoid blocking HTTP threads.
- Pagination and date-range filtering are mandatory on all list endpoints.

---

## 3. Data Flow: End-to-End

```
                         ┌─────────────────────────────────────────────────┐
                         │              SCHEDULED TRIGGER                   │
                         │  (Cron / ShedLock for distributed locking)      │
                         └─────────────────┬───────────────────────────────┘
                                           │
                         ┌─────────────────v───────────────────────────────┐
                         │         COLLECTION LAYER                        │
                         │                                                 │
                         │  ┌──────────┐ ┌──────────┐ ┌──────────┐       │
                         │  │ OpenAI   │ │Anthropic │ │ Google   │  ...  │
                         │  │ Client   │ │ Client   │ │ Client   │       │
                         │  └────┬─────┘ └────┬─────┘ └────┬─────┘       │
                         │       │             │            │              │
                         │       └─────────────┼────────────┘              │
                         │                     │                           │
                         │              RawUsageRecords                    │
                         └─────────────────────┬───────────────────────────┘
                                               │
                         ┌─────────────────────v───────────────────────────┐
                         │         NORMALIZATION LAYER                     │
                         │                                                 │
                         │  Provider Normalizer ──> User/Account Mapper    │
                         │                     ──> Model Registry Lookup   │
                         │                     ──> Cost Calculator         │
                         │                                                 │
                         │           NormalizedUsageRecords (with cost)    │
                         └─────────────────────┬───────────────────────────┘
                                               │
                         ┌─────────────────────v───────────────────────────┐
                         │         STORAGE LAYER                           │
                         │                                                 │
                         │  ┌──────────────┐  ┌────────────────────┐      │
                         │  │ Raw Records  │  │ Aggregation Engine │      │
                         │  │ (append)     │──>│ (rollup to daily/ │      │
                         │  └──────────────┘  │  monthly summaries)│      │
                         │                    └─────────┬──────────┘      │
                         │                              │                  │
                         │                    ┌─────────v──────────┐      │
                         │                    │ Summary Tables     │      │
                         │                    └────────────────────┘      │
                         └─────────────────────┬───────────────────────────┘
                                               │
                    ┌──────────────────────────┼──────────────────────────┐
                    │                          │                          │
          ┌─────────v──────────┐    ┌─────────v──────────┐    ┌─────────v──────────┐
          │  Budget Tracker    │    │  Dashboard API     │    │  Export Service     │
          │  & Alert Generator │    │  (REST endpoints)  │    │  (Async reports)   │
          └────────────────────┘    └────────────────────┘    └────────────────────┘
```

**Direction of data flow:** Strictly left-to-right / top-to-bottom through the pipeline. No component in an earlier layer depends on a later layer. The only reverse flow is alerting (calculation layer pushes alerts back to users).

---

## 4. Cross-Cutting Concerns

### 4.1 Authentication & Authorization
- Dashboards use the existing Spring Security setup (from the current codebase).
- API key management for provider clients is handled via encrypted configuration (Vault, Spring Cloud Config, or encrypted application properties).
- Role-based access: users see their own usage; team leads see team usage; admins see everything.

### 4.2 Resilience
- Provider API clients must implement retry with exponential backoff (Spring Retry or Resilience4j).
- Circuit breakers protect against provider API outages.
- Idempotent collection: re-running a collection for a time range must not create duplicate records (use provider-specific deduplication keys).

### 4.3 Audit Trail
- Raw provider responses preserved in `rawProviderData` field.
- All configuration changes (budgets, mappings, pricing) logged with who/when/what.

### 4.4 Multi-Tenancy
- User-to-account mapping enables multi-tenant cost attribution.
- Hierarchical structure: Organization > Team > User.
- Each level can have its own budgets and limits.

---

## 5. Suggested Build Order

The build order follows data flow direction — each layer depends on the one before it.

### Phase 1: Foundation (No external dependencies)
**Build first — everything else depends on these.**

| Component | Rationale |
|-----------|-----------|
| Canonical data model (`NormalizedUsageRecord`, entities, DTOs) | Every other component references this schema |
| User-to-Account mapping (entity + repository + service) | Required by normalization; also used by dashboards for access control |
| Model Registry (entity + repository + service) | Required by normalization and cost calculation |
| Pricing Rule Engine (entity + repository + service) | Required by cost calculation |
| Database schema + migrations (Flyway/Liquibase) | All persistence depends on this |

### Phase 2: Collection + Normalization Pipeline
**Build second — this is the input side of the system.**

| Component | Rationale | Depends On |
|-----------|-----------|------------|
| `UsageDataClient` interface | Contract for all provider clients | Canonical data model |
| First provider client (most-used provider) | Validates the interface design with real data | `UsageDataClient` interface |
| Usage Normalizer (for first provider) | Transforms raw data to canonical form | Canonical data model, User-Account mapper, Model registry |
| Cost Calculator service | Computes cost at ingestion time | Pricing Rule Engine, Model Registry |
| Scheduled Collector job | Orchestrates the full pipeline | All of the above |
| Storage: Usage Record Repository | Persists normalized records | Database schema |

### Phase 3: Aggregation + Reporting
**Build third — consumes what Phase 2 produces.**

| Component | Rationale | Depends On |
|-----------|-----------|------------|
| Aggregation Engine (daily/monthly rollups) | Pre-computes data for dashboards | Usage Record Repository |
| Dashboard API (summary endpoints) | First user-visible output | Aggregation tables |
| Detail Drill-down API | Investigation capability | Usage Record Repository |
| Budget Tracker | Reads aggregates to check limits | Aggregation Engine |

### Phase 4: Additional Providers + Polish
**Build last — extends the system horizontally.**

| Component | Rationale | Depends On |
|-----------|-----------|------------|
| Additional provider clients (one per provider) | Each is independent; add incrementally | `UsageDataClient` interface |
| Additional normalizers (one per provider) | Paired with each new client | Normalizer interface |
| Alert Generator (email, Slack, etc.) | Depends on budget tracker being functional | Budget Tracker |
| Export Service (CSV/PDF) | Nice-to-have; async job | Aggregation tables |
| Admin Configuration API | CRUD for all configurable entities | Foundation entities |

### Dependency Graph (Simplified)

```
Phase 1: Data Model ─── User Mapping ─── Model Registry ─── Pricing Rules ─── DB Schema
              │               │                │                  │
              v               v                v                  v
Phase 2: Provider Client ──> Normalizer ──> Cost Calculator ──> Record Repository
              │                                                    │
              v                                                    v
         Scheduler                                          Aggregation Engine
                                                                   │
                                                                   v
Phase 3:                                          Dashboard API ── Budget Tracker
                                                       │               │
                                                       v               v
Phase 4:                                   More Providers    Alerts ── Exports
```

---

## 6. Key Integration Points with Spring Boot

| Concern | Recommended Spring Approach |
|---------|---------------------------|
| Scheduled collection | `@Scheduled` + ShedLock for distributed environments |
| Provider clients | `@Service` beans + WebClient (reactive) or RestTemplate; Resilience4j for retries/circuit-breaker |
| Strategy pattern for multi-provider | `@Qualifier` or a `Map<ProviderType, UsageDataClient>` injected via Spring |
| Data access | Spring Data JPA repositories; native queries for aggregation inserts |
| Async exports | `@Async` with `TaskExecutor` configuration |
| Configuration | `@ConfigurationProperties` for provider API keys, schedules, thresholds |
| Caching | Spring Cache (`@Cacheable`) on Model Registry and Pricing lookups |
| API layer | `@RestController` with standard Spring MVC patterns |
| Security | Existing Spring Security config; extend with role-based data filtering |
| Database migrations | Flyway or Liquibase (whichever the project already uses) |

---

## 7. Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Provider API rate limits during collection | Missing usage data | Implement pagination, respect rate limits, schedule during off-peak |
| Provider API schema changes | Broken collection | Version provider clients; integration tests against provider sandboxes |
| Cost calculation drift from actual bills | Incorrect cost reporting | Reconciliation process comparing calculated vs. actual provider invoices |
| Dashboard query performance on large datasets | Slow UI | Pre-aggregation; never query raw records for summary views |
| Clock skew in distributed collection | Duplicate or missing records | Idempotent ingestion keyed on provider-specific record identifiers |

---

## 8. Summary

The architecture follows a **pipeline pattern** with five clearly bounded layers: Collection, Normalization, Storage, Calculation, and Reporting. Each layer has well-defined inputs and outputs, enabling independent development and testing. The suggested build order starts with the canonical data model and foundation services, then builds the collection pipeline, then the reporting/dashboard layer, and finally extends horizontally to additional providers. This order ensures that each phase produces testable, demonstrable value while maintaining clean dependency flow.
