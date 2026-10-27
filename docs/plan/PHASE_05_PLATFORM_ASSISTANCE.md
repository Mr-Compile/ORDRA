# Phase 5 — Smart Parcel Status Assistance

## Goal
Automatically assist parcel-status awareness from on-device signals — supported marketplace
notifications, screenshots/shared content — without courier APIs, third-party trackers, or
scraping. Contract: `docs/contracts/TRACKING_CONTRACT.md`. QA: `docs/qa/TRACKING_QA.md`.

## Implement
- Android Sharesheet receiver for images/text/URLs where useful; shared tracking content routes
  into the detection pipeline (StatusNormalizer → match → suggestion).
- Optional NotificationListenerService with explicit onboarding and settings shortcut;
  Ordra works fully without it.
- Source-package filter: only explicitly supported marketplace apps (Shopee, Lazada,
  TikTok Shop, other documented sources); unrelated notifications are ignored and never stored.
- Per-platform parsers extracting tracking number, order reference, courier, raw status text
  and order/product hints.
- `StatusNormalizer`: raw status wording → canonical `ShipmentStatus`; unrecognized → UNKNOWN.
- Shipment matching: exact tracking number first, then order reference, courier, marketplace,
  product hints, expected delivery window; never silently fuzzy-match an unrelated shipment.
- `TrackingSuggestion` → confidence gates: HIGH auto-applies only if user-enabled, MEDIUM
  shows Confirm/Ignore, LOW stays a suggestion. Applied/confirmed results write a
  `TrackingEvent` carrying `TrackingStatusSource` + `DetectionConfidence`.
- Local status-change notification per applied update (dedupe per event).
- "Track Externally" action where a safe official courier tracking destination is known —
  opens the courier's page; never scrapes the result.
- Settings: marketplace notification detection toggle, automatic high-confidence updates,
  status-change notifications, per-marketplace enable/disable.
- Manual status update always exists regardless of detection state.

## Rules
Do not read private app storage, automate marketplace credentials, bypass platform
protections, or claim official integration unless it actually exists. Never label this
"live"/"real-time" tracking; show detection source and freshness. Do not persist raw
notification bodies or full OCR text.

## Exit criteria
[ ] Revoking notification access leaves Ordra usable.
[ ] Unknown/unsupported notifications are ignored safely and not stored.
[ ] Parser changes are tested with sanitized fixtures.
[ ] No silent low/medium-confidence shipment changes.
[ ] High-confidence auto-apply happens only when the user enabled it.
[ ] Exact tracking-number match produces strong confidence; ambiguous matches ask first.
[ ] Duplicate detections do not create duplicate timeline events or notifications.
[ ] Manual shipment status always works without any permission.
