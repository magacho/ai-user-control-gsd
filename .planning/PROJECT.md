# AI User Control

## What This Is

A centralized management dashboard for controlling and monitoring AI tool usage across the company. Tracks Claude Code, Cursor, and GitHub Copilot accounts, unifies user identification via corporate email (@bemobi.com), and provides usage metrics (last access date, token consumption, estimated costs, frequency) to eliminate waste and maintain cost visibility. Serves both administrators (who manage all accounts) and developers (who can view their own usage).

## Core Value

Transparent AI tool costs with zero wasted spending on orphaned accounts.

## Requirements

### Validated

- ✓ Spring Boot 3.4.2 application structure with layered architecture — existing
- ✓ PostgreSQL database with Flyway migrations — existing
- ✓ JWT-based authentication with Spring Security — existing
- ✓ RESTful API with OpenAPI/Swagger documentation — existing
- ✓ Thymeleaf templates for web UI — existing
- ✓ Docker containerization ready — existing

### Active

- [ ] User directory (read-only, sourced from Google Workspace) showing GWS users and their tool licenses
- [ ] AI tool catalog (Claude Code, Cursor, GitHub Copilot)
- [ ] Account linking: map user email to tool-specific identifiers
- [ ] GitHub username resolution via Google Workspace custom properties
- [ ] Automated daily metrics collection from provider APIs
- [ ] Usage dashboard showing last access, tokens, estimated costs, frequency
- [ ] Admin view: full visibility across all users and tools
- [ ] Inactive account detection (identify orphaned accounts after employee offboarding)
- [ ] Cost estimation and reporting by user, tool, and time period
- [ ] Integration with Claude API (Anthropic)
- [ ] Integration with GitHub Copilot API
- [ ] Integration with Cursor API
- [ ] Integration with Google Workspace API for user data sync
- [ ] Scheduled background jobs for nightly metric collection
- [ ] Alert system for unusual usage patterns or inactive accounts

### Out of Scope

- Real-time usage tracking — Daily batch collection is sufficient for cost control
- Automatic account provisioning — Manual provisioning with centralized tracking is enough
- Multi-tenancy for other companies — Built specifically for Bemobi's needs
- Mobile app — Web dashboard covers admin and developer use cases
- Automated offboarding — Manual offboarding process remains, system provides visibility only

## Context

**Current State:**
- Each AI tool provider managed independently through separate admin panels
- No unified view of who has access to what
- Costs are untracked and growing without clear attribution
- When developers leave, accounts often remain active and consuming resources
- Manual offboarding across multiple systems is error-prone

**Technical Environment:**
- Corporate email domain: @bemobi.com
- Google Workspace with custom user properties (includes github_username field)
- Existing API access to all three providers (Claude, Cursor, GitHub)
- Development team already uses these tools daily

**User Mapping Challenge:**
- GitHub identifies users by username (not email)
- Developers may use personal GitHub accounts for work
- Solution: Google Workspace custom property stores github_username
- This enables mapping: user@bemobi.com → github_username → GitHub Copilot usage

**Success Metrics:**
- Complete cost visibility: Know exactly what each developer costs per tool per month
- Zero orphaned accounts: All active accounts belong to current employees
- Self-service usage: Developers can check their own consumption without filing tickets

## Constraints

- **Tech Stack**: Java 21 + Spring Boot 3.4.2 + PostgreSQL + Thymeleaf — Already established
- **Database**: PostgreSQL 16 required for production — Infrastructure team requirement
- **Authentication**: JWT tokens with Spring Security — Security standard already implemented
- **Update Frequency**: Daily batch updates acceptable — Real-time not required for cost reporting
- **API Rate Limits**: Must respect provider API rate limits during metric collection
- **Data Retention**: Keep historical metrics for at least 12 months for cost trend analysis

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Email as primary user key | Corporate email uniquely identifies employees, survives across tools | — Pending |
| Google Workspace custom property for GitHub mapping | GitHub uses usernames, not emails; custom property bridges the gap | — Pending |
| Daily scheduled collection instead of real-time | Cost control doesn't need second-by-second updates; reduces API load | — Pending |
| Separate views for admins vs developers | Admins need full visibility, devs only need own usage; privacy + simplicity | — Pending |
| Manual offboarding with system detection | System detects inactive accounts but doesn't auto-disable; safety first | — Pending |
| Thymeleaf server-side rendering | Simpler than SPA, adequate for internal dashboard, leverages Spring Boot integration | ✓ Good |
| Domain-driven vertical slices (user/aitool/usage) | Clear boundaries, independent evolution of features | ✓ Good |

---
*Last updated: 2026-02-24 after initialization*
