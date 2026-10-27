# Smart Parcel Status Contract

Canonical contract for Ordra shipment tracking. Ordra v1 has NO courier APIs, no
third-party parcel-tracking APIs, no scraped courier websites, no reverse-engineered
marketplace APIs, and no browser automation. Parcel status awareness is local-first.

## Final tracking decision
- Forbidden sources: AfterShip, 17TRACK, EasyPost, Shippo, Parcel Monitor, unofficial
  courier APIs, scraped courier tracking websites, reverse-engineered marketplace APIs,
  browser automation against courier sites.
- A tracking number is an identifier and matching signal. It does NOT contain live
  shipment status, and Ordra never pretends otherwise.
- Never label the feature "live tracking" or "real-time tracking". Canonical wording:
  Smart Parcel Status, Parcel Updates, Shipment Timeline, Smart Status Detection.
  Notification-derived updates are "detected parcel updates", never guaranteed
  real-time courier data.
- Ordra may open an official courier tracking page ("Track Externally") for the user.
  The returned page is never scraped; the user can then update Ordra manually or
  screenshot/share the result back in. Do not hardcode unreliable unofficial tracking sites.

## Architecture pipeline
```
Ordra Order
   └── Shipment[]                       (first-class model; 1 order → N shipments)
        ├── trackingNumber / courier   (identifier + matching signal)
        ├── shipmentStatus             (canonical ShipmentStatus)
        └── TrackingEvent[]            (timeline)

Smart Status Detection:
   Marketplace / Courier-App / Messaging Notification,
   Screenshot OCR, Shared Content, Manual
        ↓
   Source registry (supported package check — others dropped, never stored)
        ↓
   Delivery-relevance check (is this notification about a parcel at all?)
        ↓
   Local deterministic parser → extracted signals
        ↓
   Signal extraction → ShipmentMatcher → Confidence Engine
        ↓
   StatusNormalizer  (raw text → canonical ShipmentStatus)
        ↓
   TrackingSuggestion → auto-apply | confirm | review | ignore
        ↓
   Use case → ShipmentRepository → Room (shipment + tracking event)
        ↓
   Shipment Timeline → optional local notification → UI
```

Detection, parsing, matching, normalization, persistence and presentation stay
separate layers. UI never talks to NotificationListenerService or parsers directly.

## Data model (per DOMAIN_MODELS.md)
- `Shipment`: id, orderId, courier?, customCourierName?, trackingNumber?,
  shipmentStatus, statusSource, statusConfidence, lastStatusAt?, lastDetectedAt?,
  createdAt, updatedAt.
- `TrackingEvent`: id, shipmentId, shipmentStatus, title, description?, occurredAt,
  detectedAt, source, confidence, rawStatus?, sourceDetail?, rawReference?. Events
  are immutable and ordered; duplicate detections of the same transition must not
  create duplicate events.
- `TrackingStatusSource`: MANUAL, MARKETPLACE_NOTIFICATION, COURIER_APP_NOTIFICATION,
  MESSAGE_NOTIFICATION, SCREENSHOT_OCR, SMART_IMPORT, SHARED_CONTENT,
  FUTURE_OFFICIAL_PROVIDER.
- `DetectionConfidence`: LOW | MEDIUM | HIGH (internal numeric 0.0–1.0 allowed; the
  exact scoring algorithm is fixed during implementation/testing, not in docs).
- `TrackingSuggestion`: transient proposed update before confirm/apply.
- `DetectionRecord`: lightweight detection-history entry (Phase 5A) recording
  outcome + provenance without raw content.

## Detection sources & source registry
Supported sources are registered in a source registry (package name → parser
adapter + source classification). The registry is configuration-driven so new
supported apps can be added without redesigning the pipeline.

1. **Marketplace notifications.** Shopee, Lazada, TikTok Shop, other explicitly
   supported marketplace apps. `source = MARKETPLACE_NOTIFICATION`.
2. **Courier app notifications.** SPX Express, J&T Express, Flash Express,
   Ninja Van, LBC and other explicitly supported courier apps.
   `source = COURIER_APP_NOTIFICATION`.
3. **Messaging-app notifications (Rider Message Detection).** SMS/messaging-app
   notifications containing delivery-related messages — rider texts exposed
   through the Android notification shade. `source = MESSAGE_NOTIFICATION`.
   Ordra does NOT read the SMS inbox directly: the only supported path is
   `Messaging app → Android notification → NotificationListenerService → parser`.
   If a rider message generates no notification, Ordra never sees it. Direct SMS
   inbox access is a separate future capability requiring its own technical,
   privacy, permission, platform-policy and distribution review.
4. **Screenshot OCR.** ML Kit OCR on screenshots/imports detects courier, tracking
   number, shipment status, events, order info and ETA. Fully offline; Groq may
   optionally improve interpretation but basic extraction never needs it.
5. **Shared content / Smart Import.** Shared text/images route into the same
   normalization + matching pipeline.
6. **Manual.** Always available regardless of permissions; creates events with
   `source = MANUAL`.

All notification sources require `NotificationListenerService` with explicit
optional permission. Pipeline per source: notification → registry/package check →
text extraction → source-specific parser → signal extraction → match local
shipment → confidence → normalize → suggestion/event → apply or confirm. Never
process unrelated notifications; never persist raw notification content beyond the
minimal provenance fields below. Ordra must work normally with notification access
revoked.

## Signal extraction
The parser extracts candidate signals — all treated as unvalidated until matched:

- tracking number (per-courier patterns)
- marketplace order ID / order reference
- courier identity
- marketplace identity
- COD amount / order amount
- product or order hint
- delivery status phrase
- delivery date / estimated arrival
- rider/delivery indicator
- location or hub when present

Example: `"Your SPX rider will deliver SPXPH123456789 today. Please prepare
₱1,249."` → courier SPX Express, tracking number `SPXPH123456789`, COD ₱1,249,
status phrase → `OUT_FOR_DELIVERY`, delivery indication "today".

## Local-first parsing & AI fallback
Deterministic local parsing runs first and is the default: regex/known
tracking-number patterns, currency/amount parsing, marketplace and courier keyword
dictionaries, status-phrase dictionaries, order-number patterns, package-name
source detection.

```
Notification → source filter → delivery-relevance check → local parser → signals
   confidence sufficient?
      ├── YES → match/normalize locally
      └── NO  → optional ORDRA AI (Groq) interpretation, same privacy rules as
                Smart Import (SMART_IMPORT_CONTRACT.md)
```

This ordering minimizes AI usage, cost, latency, network dependency and privacy
exposure. Rules:
- Never send every notification to Groq — only the single ambiguous
  delivery-relevant candidate, only when local confidence is insufficient and the
  user enabled AI-assisted detection.
- Before AI: minimize the payload to the fields needed for classification/
  extraction. Do not send unrelated notification history, conversation history,
  contact details, phone numbers, addresses, or other notifications.
- AI output is a structured candidate, never authoritative: it cannot invent
  tracking events, guess tracking numbers, change a shipment without validation,
  or pick between multiple plausible parcels without sufficient evidence.
- Groq cannot run offline. Offline devices still do deterministic parsing,
  matching and Room persistence; the AI leg is skipped, not blocking.
- The Groq call path follows the Phase 4 architecture (direct app → Groq with a
  local git-ignored key for personal use). Groq must never be called with a
  production secret embedded in a distributed build; a controlled backend/proxy
  stays a deferred owner decision per `docs/BIBLE.md` scope rules.
- Backend AI quotas/abuse protection apply only if a proxy is ever approved —
  they are not a v1 requirement.

## ShipmentMatcher
`ShipmentMatcher` decides which existing shipment a detection belongs to,
designed for users holding many active parcels (e.g. 12 active shipments across
Shopee/TikTok/Lazada/manual). Candidate narrowing order: tracking number →
courier → marketplace → order ID → COD amount → product hint → timing/status.
Never update the most recent order by default.

Signal strength tiers:

| Tier | Signals |
|---|---|
| Very strong | exact tracking number; exact marketplace order ID |
| Strong | tracking number + courier; order ID + marketplace |
| Medium | courier + exact COD; marketplace + exact COD; marketplace + product/order hint; courier + delivery date + amount |
| Weak | price alone; marketplace alone; courier alone; generic delivery wording |

Weak evidence alone can never update a shipment automatically. Sender identity
is not a match signal: riders use unknown numbers, so matching relies on
parcel/order signals, not contacts.

## Confidence & auto-update rule
Conceptual confidence bands (internal numeric score decided in implementation):

- **HIGH** — e.g. tracking number + courier + COD + delivery phrase all agree on
  one shipment. Safe for automatic update *only when the user enabled automatic
  high-confidence updates*; otherwise show confirmation.
- **MEDIUM** — e.g. marketplace + COD + delivery phrase that plausibly identifies
  one active shipment. Always requires explicit Confirm/Ignore review.
- **LOW** — e.g. COD match alone. Never modifies the shipment; suggestion only.
- **Ambiguous** — multiple plausible shipments. Never pick arbitrarily: show the
  Possible Parcel Update review (select parcel / ignore).
- **No match** — ignore, or offer review when appropriate. Never create a false
  tracking event just because a notification looks delivery-related.

Ambiguous example: three active parcels at COD ₱499 each, notification
`"Rider po. Delivery today ₱499."` → Ordra must not guess. Surface
"Possible parcel update detected — ORDRA detected a delivery message for ₱499 but
couldn't confidently identify the parcel" with Review / Ignore and a parcel picker.

High-confidence example: shipment (Shopee, SPX, `SPXPH92738192`, COD ₱1,249,
IN_TRANSIT) + notification `"Your SPX rider will deliver SPXPH92738192 today.
Please prepare ₱1,249."` → HIGH → create `TrackingEvent` with
`shipmentStatus = OUT_FOR_DELIVERY`, `source = MARKETPLACE_NOTIFICATION` (or the
relevant source), `confidence = HIGH`, and update the shipment's current status.

## StatusNormalizer
Dedicated layer: raw status text → `StatusNormalizer` → `ShipmentStatus`.
Per-source wording ("Parcel is on the way", "Package departed sorting facility")
normalizes to canonical values. Do NOT scatter status string matching across
ViewModels, Composables, or repositories. Unrecognized text maps to UNKNOWN rather
than a wrong status. Normalization rules live centrally, keyed by phrase
dictionaries — not by a single exact string.

Reference mappings (extend the dictionary during implementation, incl.
Filipino/English rider phrasing):
- "parcel has been shipped" → SHIPPED (or PREPARING/PICKED_UP per dictionary)
- "departed sorting facility" → IN_TRANSIT
- "arrived at delivery hub" → ARRIVED_AT_HUB (or IN_TRANSIT)
- "rider is on the way" → OUT_FOR_DELIVERY
- "parcel will be delivered today" → OUT_FOR_DELIVERY when context supports it
- "delivery unsuccessful" → DELIVERY_FAILED
- "delivery delayed" → (delayed/attention handling per status model)
- "parcel delivered" → DELIVERED
- "returning to sender" → RETURNING
- "returned" → RETURNED

## Status transitions & regression protection
Canonical happy-path progression: PREPARING → PICKED_UP → IN_TRANSIT →
ARRIVED_AT_HUB → OUT_FOR_DELIVERY → DELIVERED, with exception branches
(DELIVERY_FAILED → retry/OUT_FOR_DELIVERY → DELIVERED or RETURNING → RETURNED).

- A late/stale notification must not regress a shipment: current DELIVERED +
  late IN_TRANSIT detection never auto-applies. Invalid transitions are rejected
  or escalated for confirmation.
- A rejected-for-current-state detection may still be recorded as a historical
  TrackingEvent when it is genuinely valid history — it must not replace the
  current shipment status.

## Deduplication
Marketplace/courier apps repost and update notifications. Dedupe on a composite
key — e.g. shipment + source + normalized status + time window + source event
identifier/hash — never on notification text equality alone. Repeated uploads/
applies of the same detection must be idempotent.

## Provenance & source reliability
Every detected event keeps enough provenance to explain its origin without
storing raw content: shipmentStatus, rawStatus (short raw classification label),
source, sourceDetail (source app/package key), confidence, occurredAt,
detectedAt, shipmentId.

Conceptual source reliability order (highest first): authoritative tracking API
(future) → strong courier/marketplace notification → high-confidence rider
message → AI-inferred detection → manual/user-confirmed. Source authority never
overrides recency alone: evaluate source reliability + event timestamp + current
status + transition validity + confidence together. Newer valid information is
never overwritten by older information just because its source ranks higher.

## Detection history
A lightweight detection history (Phase 5A) records outcomes for transparency:
DETECTED, MATCHED, NEEDS_REVIEW, IGNORED, DUPLICATE, REJECTED, FAILED — stored as
`DetectionRecord` with structured fields only (no raw notification bodies).
Users never inspect technical logs; debug builds may carry deeper diagnostics,
but production logs must not leak notification/message contents.

## Local notifications
Trusted status changes may raise a local notification ("Your parcel is out for
delivery"). Dedupe by tracking event — one event, one notification. Distinct from
reminder notifications; separate channel.

## TrackingProvider & sync (future extension points)
Keep a `TrackingProvider` boundary in the architecture so future official courier
integrations could plug in without redesigning Order/Shipment/TrackingEvent, UI,
ViewModels, or repositories. DO NOT implement providers in v1;
`FUTURE_OFFICIAL_PROVIDER` exists only so a future provider result has a source
value.

v1 is local-only: detections persist to Room first; there is no backend, account,
or cloud sync per `docs/BIBLE.md`/`docs/RULES.md`. If a backend is ever approved,
notification-derived uploads must be idempotent, scoped to the authenticated
user's own shipments, and server-validated (auth → ownership → event validity →
persist). None of that exists in v1.

## Failure conditions
Each condition must fail safely — degrade, review, or ignore; never corrupt data:

| Condition | Required behavior |
|---|---|
| Notification access disabled/revoked | Feature off; manual tracking intact |
| Messaging notification hidden by OS | Nothing to parse; no error surfaced |
| Notification text unavailable/empty | Skip; optionally record DetectionRecord |
| Unsupported source app | Ignore immediately; never store |
| Not delivery-related | Drop before matching; never store |
| No shipment match | Ignore or offer review; no event |
| Multiple shipment matches | Ambiguous-match review; never auto-pick |
| Low confidence | Suggestion only; no mutation |
| AI unavailable/offline/rate-limited | Fall back to deterministic result only |
| Duplicate notification | Deduped; no second event or notification |
| Old/stale notification | Timestamp + transition check; no regression |
| Invalid status transition | Reject or escalate for confirmation |
| Shipment already delivered | Ignore regressing detections |
| Shipment deleted before apply | Discard detection; no orphan event |
| Sync failure (future backend) | Retry with idempotency; never block local |

## Privacy
Do not persist raw notification bodies or full OCR text. Store the parsed result
(status, courier, tracking number, timestamps, source, confidence, minimal
provenance) and at most a minimal `rawReference`. Messaging notification access
requires its own explicit opt-in explainer before enabling — Ordra may inspect
message notifications for delivery-related information; unrelated messages are
dropped by the pipeline. AI interpretation receives only the minimized candidate
text. See `docs/specs/PRIVACY_SECURITY.md`.

## UX wording & source labels
Show detection source and freshness in user-facing copy: "Detected from Shopee
notification", "Detected from courier notification", "Detected from delivery
message", "Added manually". Prefer "Smart Status", "Latest Detected Update",
"Detected Status", "Last detected update: 12 minutes ago". Never expose
implementation words: NotificationListenerService, Groq parser, regex match.
Never "Live Tracking" unless an authoritative live provider actually supplies it.

## Parsing fixtures
Sanitized real-world notification samples (used as parser test fixtures; keep
fixtures anonymized in tests):

- J&T: `"J&T DELIVERY! TN: 635007688159, Rider: <name>. Your COD amount is 0.00.
  You can pay by cash or conveniently online when the rider arrives…"` →
  courier J&T Express, tracking `635007688159`, COD ₱0.00, out-for-delivery signal.
- SPX: `"Magandang Araw <name>, SPX Delivery! Sa iyong pahintulot, kukuha kami ng
  litrato bilang Proof of Delivery. Pakihanda ang iyong bayad na 5.00 PHP…"` →
  courier SPX Express, COD ₱5.00, out-for-delivery signal, Filipino wording.
