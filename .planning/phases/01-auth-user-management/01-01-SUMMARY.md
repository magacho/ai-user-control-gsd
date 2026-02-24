---
phase: 01-auth-user-management
plan: 01
subsystem: database, auth, config
tags: [oauth2, spring-security, flyway, jpa, postgresql, htmx, thymeleaf]

# Dependency graph
requires: []
provides:
  - OAuth2 client dependency replacing JWT for Google SSO authentication
  - Flyway migrations for users and ai_tools tables with constraints
  - User JPA entity with status (ACTIVE/INACTIVE/OFFBOARDED) and email domain constraint
  - AITool JPA entity with type (CLAUDE/GITHUB_COPILOT/CURSOR/CUSTOM) catalog
  - UserRepository and AIToolRepository with query methods
  - AppProperties with configurable admin email list
  - Session-based auth config (8h timeout, secure cookies)
  - HTMX + Layout Dialect + WebJars dependencies for UI layer
affects: [01-02-auth-flow, 01-03-ui, 02-identity-resolution]

# Tech tracking
tech-stack:
  added: [spring-boot-starter-oauth2-client, htmx-spring-boot-thymeleaf 4.0.2, htmx.org 2.0.4, webjars-locator-lite, thymeleaf-layout-dialect]
  removed: [jjwt-api, jjwt-impl, jjwt-jackson, lombok]
  patterns: [explicit Java code without Lombok, builder pattern for entities, Flyway versioned migrations, ConfigurationProperties binding]

key-files:
  created:
    - src/main/java/com/bemobi/aiusercontrol/model/entity/User.java
    - src/main/java/com/bemobi/aiusercontrol/model/entity/AITool.java
    - src/main/java/com/bemobi/aiusercontrol/enums/UserStatus.java
    - src/main/java/com/bemobi/aiusercontrol/enums/AIToolType.java
    - src/main/java/com/bemobi/aiusercontrol/user/repository/UserRepository.java
    - src/main/java/com/bemobi/aiusercontrol/aitool/repository/AIToolRepository.java
    - src/main/java/com/bemobi/aiusercontrol/config/AppProperties.java
    - src/main/resources/db/migration/V1__create_users_table.sql
    - src/main/resources/db/migration/V2__create_ai_tools_table.sql
  modified:
    - pom.xml
    - src/main/resources/application.yml
    - src/main/resources/application-dev.yml
    - src/main/resources/application-prod.yml
  deleted:
    - src/main/java/com/bemobi/aiusercontrol/security/JwtTokenProvider.java
    - src/main/java/com/bemobi/aiusercontrol/security/JwtAuthenticationFilter.java
    - src/main/java/com/bemobi/aiusercontrol/security/UserDetailsServiceImpl.java
    - lombok.config

key-decisions:
  - "No Lombok per CLAUDE.md: all entities use explicit getters/setters/constructors/builders/equals/hashCode/toString"
  - "Lombok dependency and lombok.config removed from project entirely during this plan per CLAUDE.md instructions"
  - "Added builder pattern to entities manually for convenient construction in future plans"

patterns-established:
  - "No Lombok: all Java code uses explicit methods, no annotation magic"
  - "Entity builder pattern: static Builder inner class on JPA entities"
  - "Timestamp callbacks: @PrePersist/@PreUpdate for createdAt/updatedAt"
  - "ConfigurationProperties binding: app.admin-emails mapped to AppProperties.adminEmails"

requirements-completed: [AUTH-01, AUTH-04, USER-05, USER-06]

# Metrics
duration: 3min
completed: 2026-02-24
---

# Phase 1 Plan 01: Data Foundation Summary

**OAuth2 client replacing JWT, Flyway-managed users/ai_tools schema with domain and status constraints, JPA entities with repositories, and session-based auth config**

## Performance

- **Duration:** 3 min
- **Started:** 2026-02-24T17:27:58Z
- **Completed:** 2026-02-24T17:31:45Z
- **Tasks:** 1
- **Files modified:** 15

## Accomplishments
- Replaced all three JJWT dependencies with spring-boot-starter-oauth2-client for Google SSO
- Added HTMX (htmx-spring-boot-thymeleaf 4.0.2, htmx.org 2.0.4), Layout Dialect, and WebJars for UI layer
- Created Flyway V1 migration with users table (email domain constraint @bemobi.com, status CHECK constraint)
- Created Flyway V2 migration with ai_tools table (tool_type CHECK constraint for CLAUDE/GITHUB_COPILOT/CURSOR/CUSTOM)
- Implemented User and AITool JPA entities with explicit Java code (no Lombok per CLAUDE.md)
- Created UserRepository and AIToolRepository with findByEmail, findByStatus, findByName, findByEnabled, findByToolType
- Created AppProperties with configurable admin-emails list bound from application.yml
- Configured OAuth2 Google client registration, 8h session timeout, secure cookie settings across all profiles
- Removed Lombok dependency, lombok.config, and all Lombok references from maven plugins

## Task Commits

Each task was committed atomically:

1. **Task 1: Replace JWT with OAuth2 dependencies, create data models and Flyway migrations** - `d4380c0` (feat)

## Files Created/Modified
- `pom.xml` - Removed JJWT/Lombok deps, added OAuth2 client + HTMX + Layout Dialect + WebJars
- `src/main/resources/db/migration/V1__create_users_table.sql` - Users table with email domain and status constraints
- `src/main/resources/db/migration/V2__create_ai_tools_table.sql` - AI tools catalog table with tool type constraint
- `src/main/java/com/bemobi/aiusercontrol/model/entity/User.java` - User JPA entity with builder, timestamps, status enum
- `src/main/java/com/bemobi/aiusercontrol/model/entity/AITool.java` - AITool JPA entity with builder, timestamps, type enum
- `src/main/java/com/bemobi/aiusercontrol/enums/UserStatus.java` - ACTIVE, INACTIVE, OFFBOARDED enum
- `src/main/java/com/bemobi/aiusercontrol/enums/AIToolType.java` - CLAUDE, GITHUB_COPILOT, CURSOR, CUSTOM enum
- `src/main/java/com/bemobi/aiusercontrol/user/repository/UserRepository.java` - User JPA repository with email/status queries
- `src/main/java/com/bemobi/aiusercontrol/aitool/repository/AIToolRepository.java` - AITool JPA repository with name/enabled/type queries
- `src/main/java/com/bemobi/aiusercontrol/config/AppProperties.java` - Admin email list config binding
- `src/main/resources/application.yml` - OAuth2 Google registration, session config, admin-emails
- `src/main/resources/application-dev.yml` - Dev profile with session cookie secure=false
- `src/main/resources/application-prod.yml` - Prod profile with session cookie secure=true

## Decisions Made
- **No Lombok:** Followed CLAUDE.md mandate to write all Java code explicitly. Removed Lombok dependency, lombok.config, and cleaned maven-compiler-plugin annotation processors and spring-boot-maven-plugin excludes.
- **Builder pattern:** Added manual static Builder inner classes to User and AITool entities for convenient construction in future plans (auth flow, CRUD operations).
- **CLAUDE.md included in commit:** Added CLAUDE.md to the initial commit as it contains project-wide coding conventions that must be tracked.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Removed Lombok dependency and references per CLAUDE.md**
- **Found during:** Task 1 (pom.xml update)
- **Issue:** CLAUDE.md explicitly mandates no Lombok. Plan referenced Lombok annotations in entity descriptions but CLAUDE.md overrides with explicit code requirement.
- **Fix:** Removed Lombok dependency from pom.xml, deleted lombok.config, removed Lombok from maven-compiler-plugin annotationProcessorPaths and spring-boot-maven-plugin excludes. Wrote all entities with explicit getters/setters/constructors/equals/hashCode/toString.
- **Files modified:** pom.xml, lombok.config (deleted)
- **Verification:** mvn compile -DskipTests succeeds, no Lombok references in pom.xml
- **Committed in:** d4380c0 (Task 1 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking - CLAUDE.md convention enforcement)
**Impact on plan:** Essential deviation to follow project coding standards. No scope creep.

## Issues Encountered
None

## User Setup Required

**External services require manual configuration** before Plan 02 (auth flow) can work end-to-end:
- `GOOGLE_CLIENT_ID` - From Google Cloud Console -> APIs & Services -> Credentials -> OAuth 2.0 Client IDs
- `GOOGLE_CLIENT_SECRET` - From Google Cloud Console -> APIs & Services -> Credentials -> OAuth 2.0 Client IDs
- Google OAuth consent screen must be configured with @bemobi.com domain restriction
- Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

## Next Phase Readiness
- Data foundation complete: entities, migrations, repositories, and config ready for Plan 02 (auth flow)
- Plan 02 will implement SecurityConfig with OAuth2 login, CustomOidcUserService, and login page
- Plan 03 will build the UI layer using the HTMX + Layout Dialect dependencies added here

## Self-Check: PASSED

All 14 source files verified present. Commit `d4380c0` verified in git log. Compilation verified via `mvn compile -DskipTests`.

---
*Phase: 01-auth-user-management*
*Completed: 2026-02-24*
