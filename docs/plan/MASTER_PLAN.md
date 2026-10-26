# Ordra — Complete Master Plan

## Mission
Ordra is an Android-only, local-first universal order and parcel manager. Users can manually save purchases from any marketplace, scan screenshots for assisted autofill, receive delivery/COD reminders, and optionally use platform notifications to suggest status updates.

## Delivery sequence
- Phase 0 — Product, branding, repository and engineering foundations
- Phase 1A — Android architecture and persistence foundation
- Phase 1B — Manual order management
- Phase 1C — Home dashboard and order details
- Phase 1D — Search, filters, categories and COD overview
- Phase 1E — Settings, accessibility, UX polish and core QA
- Phase 2 — Reminders, delivery calendar and notification infrastructure
- Phase 3 — Screenshot Smart Import: image selection, ML Kit OCR, parsing and review
- Phase 4 — Optional Groq-assisted extraction (direct app → Groq, local config key, graceful offline fallback)
- Phase 5 — Android Share-to-Ordra and marketplace notification assistance
- Phase 6 — Spending analytics, purchase history and insights
- Phase 7 — Returns, refunds, warranties, receipts and post-purchase tools
- Phase 8 — Backup, restore and export
- Phase 9 — Security/privacy hardening, performance and release readiness
- Phase 10 — Beta, Play Store launch and post-launch maintenance

## Global gate
Every phase follows: Plan → Implement → Test → Audit → Repair → Verify → Commit → Next phase. A phase is not complete because files exist. All acceptance criteria must pass and no critical TODO/stub may remain in its scope.
