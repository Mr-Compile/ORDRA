# Phase 5 — Smart Parcel Status Assistance

## Goal
Automatically assist parcel-status awareness from on-device signals — supported
marketplace, courier-app and messaging-app notifications, screenshots/shared
content — without courier APIs, third-party trackers, or scraping. Contract:
`docs/contracts/TRACKING_CONTRACT.md`. QA: `docs/qa/TRACKING_QA.md`.

Phase 5 is split into four sub-phases. 5A delivers the core user value; 5B–5D
strengthen and extend it. None of them introduce courier APIs into v1, and Phase
5D is never required for Smart Notification Detection to function.

## Phase 5A — Smart Notification Detection
The detection pipeline end to end:

- NotificationListenerService with explicit onboarding and settings shortcut;
  Ordra works fully without it.
- Source registry/filter: supported marketplace apps (Shopee, Lazada, TikTok
  Shop…), courier apps (SPX Express, J&T Express, Flash Express, Ninja Van,
  LBC…), and messaging-app notifications carrying rider/delivery texts.
  Unrelated notifications are ignored and never stored.
- Messaging apps are read through notifications only — never the SMS inbox.
- Delivery-relevance check before parsing (rider/delivery keyword dictionaries
  supporting mixed English/Filipino wording).
- Local deterministic parser + signal extraction (tracking number, order ID,
  courier, marketplace, COD/order amount, product hint, status phrase, delivery
  date, rider indicator, location hint).
- `ShipmentMatcher` + confidence engine (signal tiers; never silently fuzzy-match;
  ambiguous multi-match → user review, never arbitrary pick).
- `TrackingSuggestion` → confidence gates: HIGH auto-applies only if user-enabled,
  MEDIUM shows Confirm/Ignore, ambiguous shows parcel-pick review, LOW stays a
  suggestion. Applied/confirmed results write a `TrackingEvent` carrying
  `TrackingStatusSource` + `DetectionConfidence` + provenance fields.
- Event deduplication (composite key, not text equality) and status-transition
  validation (no regressing DELIVERED on stale detections).
- Optional ORDRA AI (Groq) fallback for ambiguous delivery text — separately
  toggleable, minimized payload, never every notification.
- Smart Detection settings: marketplace notifications, courier notifications,
  rider/SMS notification detection (separate toggle), automatic high-confidence
  updates, ask-before-ambiguous updates, AI-assisted detection (separate toggle).
- Permission UX: education screen before Android settings; separate
  message-privacy explainer before enabling rider/message detection.
- Ambiguous-match review UI ("Possible Parcel Update" → select parcel / ignore)
  and lightweight detection history.
- Local status-change notification per applied update (dedupe per event).
- Manual status update always exists regardless of detection state.

## Phase 5B — Status Normalization + Courier Detection
- `StatusNormalizer`: centralized phrase dictionaries → canonical
  `ShipmentStatus`; unrecognized → UNKNOWN. Includes Filipino/English rider
  phrasing; not dependent on one exact phrase.
- Courier registry: per-courier tracking-number patterns, keyword detection,
  package-name mapping.
- Status progression rules: canonical forward progression, exception branches,
  regression protection (stale detections never move a parcel backward; valid
  history may still be recorded without replacing current status).

## Phase 5C — Tracking Provider + Update Pipeline
- `TrackingProvider` abstraction kept as an extension point (no provider built).
- `TrackingSyncManager`-style boundary: detection → validation → Room write →
  pending-sync/retry semantics that stay idempotent and offline-tolerant.
  - v1 is local-only (no backend/accounts per RULES); "sync" means the durable
    local write + retry pipeline. Any future server sync requires the deferred
    owner-approved cloud decision and must enforce auth → ownership → validity.
- Provider-independent tracking architecture: all sources emit the same
  normalized `TrackingSuggestion`/`TrackingEvent` shapes.

## Phase 5D — Optional External Tracking API
- External official provider integration behind `TrackingProvider`
  (`FUTURE_OFFICIAL_PROVIDER` results), provider adapter pattern, API quota
  management, authoritative tracking where available, fallback between supported
  tracking sources. Requires explicit owner approval; never a v1 blocker.

## Phase 5A-remaining shared rules
- Android Sharesheet receiver for images/text/URLs where useful; shared tracking
  content routes into the detection pipeline (StatusNormalizer → match →
  suggestion).
- "Track Externally" action where a safe official courier tracking destination
  is known — opens the courier's page; never scrapes the result.
- Do not read private app storage, automate marketplace credentials, bypass
  platform protections, or claim official integration unless it actually exists.
  Never label this "live"/"real-time" tracking; show detection source and
  freshness ("Detected 9:42 AM · Shopee notification", "Last detected update:
  12 minutes ago"). Do not persist raw notification bodies or full OCR text.

## Exit criteria (5A)
[ ] Revoking notification access leaves Ordra usable.
[ ] Unknown/unsupported notifications are ignored safely and not stored.
[ ] Parser changes are tested with sanitized fixtures (incl. real J&T/SPX/Filipino
    samples).
[ ] Rider/SMS notification detection is separately toggleable with its own
    privacy explainer; Ordra never reads the SMS inbox.
[ ] No silent low/medium-confidence or ambiguous shipment changes.
[ ] High-confidence auto-apply happens only when the user enabled it.
[ ] Exact tracking-number match produces strong confidence; ambiguous matches
    (e.g. identical COD across parcels) ask first with a parcel picker.
[ ] Stale notifications cannot regress a delivered parcel.
[ ] Duplicate/reposted notifications do not create duplicate timeline events or
    notifications.
[ ] ORDRA AI fallback is optional, minimized, and never sees non-candidate
    notifications; offline detection works without it.
[ ] Detection history records outcomes without raw content.
[ ] Manual shipment status always works without any permission.
