# Phase 1: Auth & User Management - Context

**Gathered:** 2026-02-24
**Status:** Ready for planning

<domain>
## Phase Boundary

Secure admin-only access to the dashboard via Google SSO, establish the user and AI tool data models, and build the navigation skeleton for the read-only dashboard platform. Users are NOT manually created — they are discovered from AI tool provider APIs in later phases. The platform is read-only (dashboard + history), with the exception of AI tool catalog management.

</domain>

<decisions>
## Implementation Decisions

### Authentication & Login
- Google SSO (OAuth) only — no password fallback
- Restricted to @bemobi.com Google Workspace domain
- Admin-only access — developers never log in (Phase 8 dropped entirely)
- Pre-registered users only: admin must approve/import users before they can access the system
- Initial admin(s) bootstrapped via application config property (list of admin emails)
- Pull name and profile picture from Google Workspace on login
- Google avatar displayed in the UI (navbar)
- 8-hour session timeout (one workday), then re-authenticate via Google
- Deactivated/non-registered users get a friendly rejection message (not a generic error)
- Login page design: Claude's discretion

### Admin Management
- Multiple admins supported, all with equal permissions (no super admin hierarchy)
- Admin emails defined in application config only — adding a new admin requires config update + restart
- No admin management through the UI

### Dashboard & Navigation
- Left sidebar navigation skeleton — ready for future phases to fill in sections
- Clean & minimal visual style (Linear/Vercel aesthetic)
- Tailwind CSS for styling
- Thymeleaf + HTMX (existing decision)
- Desktop only — no responsive/mobile design
- Data table layout for user lists

### User Data Model
- Users are NOT manually created by admins — discovered from AI tool provider APIs
- Primary identifier: Claude's discretion (corporate email or Google Workspace ID)
- Department pulled from Google Workspace directory
- No extra manual fields — only data from APIs (Google Workspace + provider APIs)
- User status determined automatically from data (not manually set by admin)
- "Inactive" = user exists in AI tools but NOT in Google Workspace directory (orphaned account — left the organization)
- Also flag low-usage users: no activity on a specific tool for 60 days (still in org, but potentially wasted license)
- Track account status per AI tool (active/suspended/revoked)

### AI Tool Catalog
- Extensible catalog — designed to support adding new tools beyond Claude/Cursor/GitHub Copilot
- Tool definitions stored in database, managed through dashboard UI (CRUD)
- Admin creates tools through the UI (no pre-seeded defaults)
- Tool entity fields: Claude's discretion (name, type, integration settings, etc.)

### Notifications
- Dashboard alert + email notification when orphaned accounts are detected
- Email notification details (service, frequency): Claude's discretion for later phases

### Empty States
- No setup wizard — admin logs in and sees data immediately (if any exists)
- Provider APIs configured at deployment via application properties, not through UI
- Empty state display approach: Claude's discretion

### Claude's Discretion
- Login page layout and design
- User table columns and presentation
- Primary user identifier choice (email vs Google Workspace ID)
- AI tool entity field design
- Empty state messaging and display
- Loading skeleton and error state design

</decisions>

<specifics>
## Specific Ideas

- Platform is purely a read-only dashboard and history viewer — "just to see the numbers and users"
- Provider API credentials are configured at deployment (application properties), not through the UI
- The core value is detecting orphaned accounts: users who exist in AI tools but have been removed from Google Workspace
- The system should feel like a clean admin panel, not a complex management tool

</specifics>

<deferred>
## Deferred Ideas

- Phase 8 (Developer Self-Service) dropped entirely — platform is admin-only forever
- AUTH-02 (Developer login) removed from requirements
- DEV-01 through DEV-05 (developer dashboard requirements) removed
- USER-02 (Admin creates users) reframed — users come from provider APIs, not manual creation
- USER-03 (Admin updates user info) reframed — read-only, no manual edits
- USER-04 (Admin deactivates user) reframed — status is automatic from data, not manual action
- Email notification system for orphaned accounts — implementation deferred to later phases (Phase 5+)

</deferred>

---

*Phase: 01-auth-user-management*
*Context gathered: 2026-02-24*
