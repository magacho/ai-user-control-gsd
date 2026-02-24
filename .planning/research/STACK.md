# Stack Research: AI Usage Management & Cost Control

> **Research Question:** What's the standard 2025 stack for building internal admin dashboards that integrate with multiple third-party APIs (Claude/Anthropic, GitHub, Cursor) and Google Workspace for enterprise usage tracking and cost management?
>
> **Context:** Subsequent milestone research — adding AI tool usage tracking, user management, and cost reporting features to an existing Spring Boot 3.4.2 / Java 21 application. Base infrastructure (Spring Security, JPA, PostgreSQL) already exists.

---

## 1. Existing Stack (Do NOT Re-Research)

| Layer | Technology | Version |
|-------|-----------|---------|
| Runtime | Java | 21 (LTS) |
| Framework | Spring Boot | 3.4.2 |
| Security | Spring Security | 6.x (via Boot 3.4.2) |
| Persistence | Spring Data JPA / Hibernate | 6.x |
| Database | PostgreSQL | 16+ |
| Build | Gradle (Kotlin DSL) | 8.x |

---

## 2. API Integration Layer

### 2.1 Anthropic/Claude API Client

| Recommendation | Details |
|---------------|---------|
| **Library** | `com.anthropic:anthropic-java` (Official Anthropic Java SDK) |
| **Version** | 1.x (latest stable — verify on Maven Central before adoption) |
| **Confidence** | HIGH |
| **Rationale** | Anthropic released an official Java SDK in early 2025. It provides typed request/response models, automatic retries, streaming support, and handles authentication. Using the official SDK ensures compatibility with API changes and proper token counting for cost tracking. |
| **Alternative considered** | Spring RestClient / WebClient with manual DTOs — rejected because maintaining API compatibility manually is error-prone, and Anthropic's API evolves quickly. |
| **What NOT to use** | Do NOT use unofficial community wrappers (e.g., `langchain4j` Anthropic module) for direct API integration — they add unnecessary abstraction layers and lag behind official API features. LangChain4j is appropriate for LLM orchestration, not for usage tracking/admin. |

### 2.2 GitHub API Client

| Recommendation | Details |
|---------------|---------|
| **Library** | `org.kohsuke:github-api` |
| **Version** | 1.326+ (latest stable) |
| **Confidence** | HIGH |
| **Rationale** | The de facto standard Java library for GitHub API integration. Mature (10+ years), well-maintained, covers the full GitHub REST API including organization management, Copilot usage endpoints, and billing APIs. Actively maintained with releases every few weeks. |
| **Alternative considered** | Spring WebClient with raw GitHub REST calls — rejected because GitHub's API is enormous and the library handles pagination, rate limiting, and authentication (OAuth, GitHub Apps, PATs) out of the box. |
| **What NOT to use** | Do NOT use `eclipse/egit-github` — it is abandoned and does not support GitHub's modern API features (Copilot, Actions billing). |

### 2.3 Cursor API Client

| Recommendation | Details |
|---------------|---------|
| **Library** | Spring RestClient (manual integration) |
| **Version** | N/A (part of Spring Web 6.x) |
| **Confidence** | MEDIUM |
| **Rationale** | Cursor does not have an official public API or Java SDK as of early 2026. Integration requires reverse-engineering their billing/usage endpoints or using their admin dashboard data. Use Spring's `RestClient` (the modern replacement for `RestTemplate` in Spring 6.1+) with manual DTOs. RestClient is synchronous, declarative, and fits the Spring ecosystem. |
| **Alternative considered** | Spring WebClient — rejected for this use case because Cursor API calls are simple synchronous requests, and WebClient's reactive model adds unnecessary complexity. |
| **What NOT to use** | Do NOT use `RestTemplate` — it is in maintenance mode as of Spring 6.1. All new code should use `RestClient` for synchronous HTTP or `WebClient` for reactive/async. |

### 2.4 Google Workspace Admin SDK

| Recommendation | Details |
|---------------|---------|
| **Library** | `com.google.apis:google-api-services-admin-directory` + `com.google.apis:google-api-services-admin-reports` |
| **Version** | `directory_v1-rev20241015-2.0.0` / latest revision (check Maven Central) |
| **Confidence** | HIGH |
| **Rationale** | Official Google client libraries for Java. Required for pulling user directory data, group memberships, and audit/usage reports from Google Workspace. The Admin SDK Reports API provides per-user usage data for Workspace apps. |
| **Supporting libraries** | `com.google.auth:google-auth-library-oauth2-http:1.30.0+` for service account authentication, `com.google.api-client:google-api-client:2.7.0+` as the base client. |
| **What NOT to use** | Do NOT use older `com.google.api-client:google-api-client:1.x` — the 2.x line uses `java.net.http.HttpClient` and is required for Java 21 compatibility. |

---

## 3. Scheduling & Background Jobs

### 3.1 Usage Data Collection Scheduler

| Recommendation | Details |
|---------------|---------|
| **Library** | Spring Scheduling (`@Scheduled` / `@EnableScheduling`) |
| **Version** | Part of Spring Boot 3.4.2 |
| **Confidence** | HIGH |
| **Rationale** | For a single-instance application, Spring's built-in scheduling is sufficient for periodic API polling (e.g., hourly usage sync, daily cost aggregation). It's zero-dependency, well-understood, and integrates with Spring's transaction management. |

### 3.2 Distributed Lock (if scaling beyond single instance)

| Recommendation | Details |
|---------------|---------|
| **Library** | ShedLock (`net.javacrumbs.shedlock:shedlock-spring`) |
| **Version** | 6.2.0+ |
| **Confidence** | HIGH |
| **Rationale** | If the app needs to run multiple instances, ShedLock ensures scheduled tasks execute only once across the cluster. Uses the existing PostgreSQL database as the lock provider (no additional infrastructure). Minimal configuration — just annotate `@SchedulerLock` on existing `@Scheduled` methods. |
| **Provider** | `shedlock-provider-jdbc-template` (reuses existing DataSource) |
| **What NOT to use** | Do NOT use Quartz Scheduler — it's significantly more complex (cluster tables, XML config) for what amounts to simple periodic polling. Quartz is appropriate for complex job workflows, not for "fetch API data every hour." |

---

## 4. Caching Layer

| Recommendation | Details |
|---------------|---------|
| **Library** | Spring Cache with Caffeine (`com.github.ben-manes.caffeine:caffeine`) |
| **Version** | Caffeine 3.1.8+ |
| **Confidence** | HIGH |
| **Rationale** | API rate limits on Anthropic, GitHub, and Google make caching essential. Caffeine is the highest-performance JVM cache, integrates natively with Spring Cache (`@Cacheable`), and supports TTL/size-based eviction. No external infrastructure needed. Cache user profiles, org data, and rate limit status. |
| **What NOT to use** | Do NOT add Redis for caching at this stage — it's infrastructure overhead for a single-instance internal admin tool. Move to Redis only if horizontal scaling becomes a requirement. |

---

## 5. Metrics & Observability

### 5.1 Application Metrics

| Recommendation | Details |
|---------------|---------|
| **Library** | Micrometer (`io.micrometer:micrometer-core`) |
| **Version** | 1.14.x (ships with Spring Boot 3.4.2 via Actuator) |
| **Confidence** | HIGH |
| **Rationale** | Already included via Spring Boot Actuator. Use Micrometer to instrument API call counts, latencies, error rates, and cost accumulation as custom metrics. These metrics serve dual purpose: operational monitoring AND business cost tracking. |

### 5.2 Metrics Export

| Recommendation | Details |
|---------------|---------|
| **Library** | `micrometer-registry-prometheus` |
| **Version** | 1.14.x (match Micrometer core) |
| **Confidence** | HIGH |
| **Rationale** | Prometheus format is the industry standard for metrics collection. Exposes `/actuator/prometheus` endpoint. If the organization runs Grafana, dashboards can visualize cost trends, usage patterns, and budget alerts. |

### 5.3 Structured Logging

| Recommendation | Details |
|---------------|---------|
| **Library** | Logback (default in Spring Boot) + `net.logstash.logback:logstash-logback-encoder` |
| **Version** | logstash-logback-encoder 8.0+ |
| **Confidence** | MEDIUM |
| **Rationale** | JSON-structured logs enable querying API interaction history (which user, which API, tokens consumed, cost). Only add if centralized logging (ELK/Loki) is in place; otherwise standard Logback is sufficient. |

---

## 6. Data & Reporting

### 6.1 Database Migrations

| Recommendation | Details |
|---------------|---------|
| **Library** | Flyway (`org.flywaydb:flyway-core` + `flyway-database-postgresql`) |
| **Version** | 10.x (compatible with Spring Boot 3.4.2) |
| **Confidence** | HIGH |
| **Rationale** | Usage tracking requires new tables (usage_events, cost_records, user_quotas, api_keys). Flyway is the Spring Boot default for schema migrations, version-controlled and repeatable. |
| **What NOT to use** | Do NOT use Liquibase unless the team already standardizes on it — Flyway has simpler SQL-based migrations and Spring Boot auto-configures it. |

### 6.2 Reporting / Data Export

| Recommendation | Details |
|---------------|---------|
| **Library** | Apache POI (`org.apache.poi:poi-ooxml`) for Excel exports |
| **Version** | 5.3.0+ |
| **Confidence** | MEDIUM |
| **Rationale** | Enterprise cost reporting typically requires Excel exports for finance teams. Apache POI is the standard for generating .xlsx files in Java. Only add if Excel export is a confirmed requirement. |
| **Alternative** | For simple CSV exports, use OpenCSV (`com.opencsv:opencsv:5.9+`) — lighter weight. |

---

## 7. API Design & Documentation

### 7.1 API Documentation

| Recommendation | Details |
|---------------|---------|
| **Library** | SpringDoc OpenAPI (`org.springdoc:springdoc-openapi-starter-webmvc-ui`) |
| **Version** | 2.7.0+ |
| **Confidence** | HIGH |
| **Rationale** | Auto-generates OpenAPI 3.1 specs from Spring MVC controllers. Provides Swagger UI at `/swagger-ui.html` for the admin dashboard's REST API. Essential for frontend-backend contract documentation. |
| **What NOT to use** | Do NOT use `springfox` — it is abandoned and does not support Spring Boot 3.x. |

### 7.2 API Validation

| Recommendation | Details |
|---------------|---------|
| **Library** | `spring-boot-starter-validation` (Jakarta Bean Validation / Hibernate Validator) |
| **Version** | Part of Spring Boot 3.4.2 |
| **Confidence** | HIGH |
| **Rationale** | Already in the Spring ecosystem. Use `@Valid`, `@NotNull`, `@Min`, `@Max` on DTOs for request validation (e.g., quota limits, budget thresholds). |

---

## 8. Security Additions

### 8.1 API Key Management

| Recommendation | Details |
|---------------|---------|
| **Library** | Spring Security (existing) + custom `ApiKeyAuthenticationFilter` |
| **Version** | Part of Spring Security 6.x |
| **Confidence** | HIGH |
| **Rationale** | Third-party API keys (Anthropic, GitHub, Google) must be stored encrypted. Use Spring Security's existing infrastructure with a custom filter for internal API authentication. Store API keys encrypted in PostgreSQL using JPA `@Convert` with AES-256 encryption. |

### 8.2 Secrets Encryption at Rest

| Recommendation | Details |
|---------------|---------|
| **Library** | Jasypt (`com.github.ulisesbocchio:jasypt-spring-boot-starter`) |
| **Version** | 3.0.5+ |
| **Confidence** | MEDIUM |
| **Rationale** | Encrypts sensitive configuration properties (master encryption keys, service account credentials) in `application.yml`. Alternative: use environment variables or a vault, but Jasypt is simpler for a first iteration. |
| **Alternative for production** | HashiCorp Vault via `spring-cloud-vault` — better for production but adds infrastructure dependency. Evaluate based on existing infrastructure. |

---

## 9. Testing Additions

### 9.1 API Integration Testing

| Recommendation | Details |
|---------------|---------|
| **Library** | WireMock (`org.wiremock:wiremock-standalone`) |
| **Version** | 3.10.0+ |
| **Confidence** | HIGH |
| **Rationale** | Essential for testing third-party API integrations without hitting real endpoints. Mock Anthropic, GitHub, and Google API responses. WireMock 3.x supports JUnit 5 natively and can simulate rate limiting, error responses, and pagination — all critical for cost-tracking logic. |
| **What NOT to use** | Do NOT use MockServer — WireMock is more mature, better documented, and has wider community adoption in the Spring ecosystem. |

### 9.2 Database Testing

| Recommendation | Details |
|---------------|---------|
| **Library** | Testcontainers (`org.testcontainers:postgresql`) |
| **Version** | 1.20.x+ |
| **Confidence** | HIGH |
| **Rationale** | Test against real PostgreSQL in Docker containers. Critical for testing Flyway migrations and complex cost-aggregation queries. Spring Boot 3.4.x has first-class Testcontainers support via `@ServiceConnection`. |

---

## 10. Frontend / Admin Dashboard

### 10.1 Server-Side Rendering Approach

| Recommendation | Details |
|---------------|---------|
| **Library** | Thymeleaf (`spring-boot-starter-thymeleaf`) + HTMX (`htmx.org`) |
| **Version** | Thymeleaf 3.1.x (via Boot), HTMX 2.0.x |
| **Confidence** | MEDIUM-HIGH |
| **Rationale** | For an internal admin dashboard, a full SPA framework (React/Angular) is overkill. Thymeleaf + HTMX delivers dynamic dashboards with server-side rendering, no build toolchain, and leverages the Java team's existing skills. HTMX handles partial page updates (e.g., refreshing usage tables, live cost counters) without JavaScript framework complexity. This is the 2025 "modern server-side" approach gaining significant traction. |

### 10.2 Alternative: REST API + Separate Frontend

| Recommendation | Details |
|---------------|---------|
| **Library** | Build REST API only; frontend as separate project |
| **Confidence** | MEDIUM |
| **Rationale** | If the team has frontend developers or plans to integrate with existing React/Vue dashboards, expose a clean REST API and let the frontend team choose their stack. In that case, SpringDoc OpenAPI (section 7.1) becomes even more critical. |
| **Decision point** | Choose Thymeleaf+HTMX if the Java team owns the full stack. Choose REST-only if a dedicated frontend team exists. |

### 10.3 Charts & Visualization (if using Thymeleaf)

| Recommendation | Details |
|---------------|---------|
| **Library** | Chart.js (CDN) |
| **Version** | 4.4.x |
| **Confidence** | MEDIUM |
| **Rationale** | Lightweight charting library for cost trends, usage graphs, and budget dashboards. Works well with HTMX — update chart data via partial HTML/JSON responses. |

---

## 11. Resilience & Rate Limiting

### 11.1 Circuit Breaker / Retry

| Recommendation | Details |
|---------------|---------|
| **Library** | Resilience4j (`io.github.resilience4j:resilience4j-spring-boot3`) |
| **Version** | 2.2.0+ |
| **Confidence** | HIGH |
| **Rationale** | All three APIs (Anthropic, GitHub, Google) have rate limits and can experience outages. Resilience4j provides circuit breaker, retry with exponential backoff, rate limiter, and bulkhead patterns. Annotation-driven (`@CircuitBreaker`, `@Retry`) and integrates with Micrometer for metrics. This is non-negotiable for production reliability. |
| **What NOT to use** | Do NOT use Netflix Hystrix — it has been in maintenance mode since 2018 and does not support Spring Boot 3.x. |

### 11.2 Outbound Rate Limiting

| Recommendation | Details |
|---------------|---------|
| **Library** | Resilience4j RateLimiter (same dependency) |
| **Confidence** | HIGH |
| **Rationale** | Proactively limit outbound API calls to stay within provider quotas. Configure per-API rate limits (e.g., Anthropic: 1000 req/min, GitHub: 5000 req/hr). Prevents hitting rate limits that would disrupt usage data collection. |

---

## 12. Configuration Management

| Recommendation | Details |
|---------------|---------|
| **Library** | Spring Boot `@ConfigurationProperties` with validation |
| **Version** | Part of Spring Boot 3.4.2 |
| **Confidence** | HIGH |
| **Rationale** | Type-safe configuration for API endpoints, keys, rate limits, and cost parameters. Use `@Validated` on configuration classes to fail fast on misconfiguration. Group by provider: `app.anthropic.*`, `app.github.*`, `app.google.*`, `app.cursor.*`. |

---

## 13. Complete Dependency Summary

### Must-Have (HIGH confidence)

```gradle
// API Integration
implementation 'com.anthropic:anthropic-java:1.+'           // Anthropic official SDK
implementation 'org.kohsuke:github-api:1.326+'              // GitHub API
implementation 'com.google.apis:google-api-services-admin-directory:directory_v1-rev20241015-2.0.0'
implementation 'com.google.apis:google-api-services-admin-reports:reports_v1-rev20241015-2.0.0'
implementation 'com.google.auth:google-auth-library-oauth2-http:1.30.0'

// Resilience
implementation 'io.github.resilience4j:resilience4j-spring-boot3:2.2.0'

// Caching
implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'

// Scheduling (distributed)
implementation 'net.javacrumbs.shedlock:shedlock-spring:6.2.0'
implementation 'net.javacrumbs.shedlock:shedlock-provider-jdbc-template:6.2.0'

// Database Migrations
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'

// API Documentation
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0'

// Metrics
implementation 'io.micrometer:micrometer-registry-prometheus'

// Testing
testImplementation 'org.wiremock:wiremock-standalone:3.10.0'
testImplementation 'org.testcontainers:postgresql'
testImplementation 'org.springframework.boot:spring-boot-testcontainers'
```

### Should-Have (MEDIUM confidence — add based on requirements confirmation)

```gradle
// Admin Dashboard UI
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
// HTMX via WebJars or CDN (no Gradle dependency needed)

// Excel Exports
implementation 'org.apache.poi:poi-ooxml:5.3.0'

// Structured Logging
implementation 'net.logstash.logback:logstash-logback-encoder:8.0'

// Secrets Management
implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5'
```

### Do NOT Add

| Library | Reason |
|---------|--------|
| Redis | Unnecessary infrastructure for single-instance internal tool |
| Quartz Scheduler | Over-engineered for simple periodic API polling |
| Netflix Hystrix | Abandoned, no Spring Boot 3.x support |
| RestTemplate | Maintenance mode — use RestClient instead |
| Springfox | Abandoned — use SpringDoc OpenAPI |
| LangChain4j | Wrong abstraction level for admin/tracking (use for AI features, not tracking) |
| Kafka/RabbitMQ | No event streaming needed — direct DB writes for usage events suffice at this scale |
| Full SPA framework | React/Angular/Vue adds build complexity for an internal tool; Thymeleaf+HTMX is sufficient |

---

## 14. Version Verification Notes

> **IMPORTANT:** All version numbers listed are based on knowledge available up to May 2025. Before adding dependencies:
> 1. Check Maven Central for the latest stable release of each library
> 2. Verify Spring Boot 3.4.2 compatibility (check spring.io dependency management BOM)
> 3. The Anthropic Java SDK (`com.anthropic:anthropic-java`) was released in early 2025 — verify the exact Maven coordinates and latest version on Maven Central, as this is a new library
> 4. Google API client library version revisions change frequently — always use the latest revision suffix

---

## 15. Architecture Decision Record: Why This Stack

**Decision:** Extend the existing Spring Boot monolith rather than building microservices.

**Context:** The application needs to:
1. Poll 4 external APIs (Anthropic, GitHub, Cursor, Google) for usage data
2. Store and aggregate cost/usage records
3. Present dashboards and reports to admins
4. Manage user quotas and budgets

**Rationale:**
- A single Spring Boot application with scheduled jobs is the simplest architecture for an internal admin tool
- The existing PostgreSQL database can handle the usage data volume (thousands of records/day, not millions)
- Adding microservices, message queues, or event sourcing would be premature optimization
- All chosen libraries are designed to work within a Spring Boot monolith
- If scale demands it later, the scheduled jobs can be extracted into a separate service using ShedLock as the coordination mechanism

**Trade-offs accepted:**
- Single point of failure (acceptable for internal tool with no SLA requirement)
- Vertical scaling only (acceptable at projected data volumes)
- Coupled deployment (acceptable for small team)
