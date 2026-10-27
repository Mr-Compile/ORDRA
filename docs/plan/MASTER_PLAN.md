# Ordra — Complete Master Plan

## Mission
Ordra is an Android-only, local-first universal order and parcel manager. Users can manually save purchases from any marketplace, manage per-order shipments with tracking timelines, scan screenshots for assisted autofill and parcel-status detection, receive delivery/COD reminders, and optionally let supported marketplace notifications suggest shipment status updates. Tracking is local Smart Parcel Status — no courier APIs, third-party trackers, or scraping.

## Delivery sequence
- Phase 0 — Product, branding, repository and engineering foundations
- Phase 1A — Android architecture and persistence foundation
- Phase 1B — Manual order + shipment management (orders, items, shipments, tracking events)
- Phase 1C — Home dashboard, order details and shipment timeline
- Phase 1D — Search, filters, categories and COD overview
- Phase 1E — Settings, accessibility, UX polish and core QA
- Phase 2 — Reminders, delivery calendar and notification infrastructure
- Phase 3 — Screenshot Smart Import: image selection, ML Kit OCR, order + shipment detection, parsing and review
- Phase 4 — Optional Groq-assisted extraction (direct app → Groq, local config key, graceful offline fallback)
- Phase 5 — Smart Parcel Status: share-to-Ordra, marketplace notification assistance, status normalization, confidence-gated matching
- Phase 6 — Spending analytics, purchase history and insights
- Phase 7 — Returns, refunds, warranties, receipts and post-purchase tools
- Phase 8 — Backup, restore and export
- Phase 9 — Security/privacy hardening, performance and release readiness
- Phase 10 — Beta, Play Store launch and post-launch maintenance

## Global gate
Every phase follows: Plan → Implement → Test → Audit → Repair → Verify → Commit → Next phase. A phase is not complete because files exist. All acceptance criteria must pass and no critical TODO/stub may remain in its scope.
