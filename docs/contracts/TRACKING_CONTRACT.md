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
   Marketplace Notification / Screenshot OCR / Shared Content / Manual
        ↓
   Platform Parser (per source package / detected platform)
        ↓
   StatusNormalizer  (raw text → canonical ShipmentStatus)
        ↓
   Shipment matching + Confidence Engine
        ↓
   TrackingSuggestion → auto-apply | confirm | suggest-only
        ↓
   Room (shipment + tracking event) → Shipment Timeline → local notification
```

## Data model (per DOMAIN_MODELS.md)
- `Shipment`: id, orderId, courier?, customCourierName?, trackingNumber?,
  shipmentStatus, statusSource, statusConfidence, lastStatusAt?, lastDetectedAt?,
  createdAt, updatedAt.
- `TrackingEvent`: id, shipmentId, shipmentStatus, title, description?, occurredAt,
  detectedAt, source, confidence, rawReference?. Events are immutable and ordered;
  duplicate detections of the same transition must not create duplicate events.
- `TrackingStatusSource`: MANUAL, MARKETPLACE_NOTIFICATION, SCREENSHOT_OCR,
  SMART_IMPORT, SHARED_CONTENT, FUTURE_OFFICIAL_PROVIDER.
- `DetectionConfidence`: LOW | MEDIUM | HIGH (internal numeric 0.0–1.0 allowed).
- `TrackingSuggestion`: transient proposed update before confirm/apply.

## Detection sources
1. **Marketplace notifications (primary automatic source).**
   `NotificationListenerService` with explicit optional permission. Filter to supported
   source packages (Shopee, Lazada, TikTok Shop, other explicitly supported apps).
   Pipeline: notification → supported-package check → text extraction →
   platform-specific parser → detect tracking number / order reference / courier /
   status / product hints → match local shipment → confidence → normalize →
   suggestion/event → apply or confirm. Never process unrelated notifications; never
   persist raw notification content beyond the minimal `rawReference`.
   Ordra must work normally with notification access revoked.
2. **Screenshot OCR.** ML Kit OCR on screenshots/imports detects courier, tracking
   number, shipment status, events, order info and ETA. Fully offline; Groq may
   optionally improve Smart Import interpretation but basic extraction never needs it.
3. **Shared content / Smart Import.** Shared text/images route into the same
   normalization + matching pipeline.
4. **Manual.** Always available regardless of permissions; creates events with
   `source = MANUAL`.

## StatusNormalizer
Dedicated layer: raw status text → `StatusNormalizer` → `ShipmentStatus`.
Per-platform wording ("Parcel is on the way", "Package departed sorting facility")
normalizes to canonical values (IN_TRANSIT, OUT_FOR_DELIVERY, …). Do NOT scatter
status string matching across ViewModels, Composables, or repositories. Unrecognized
text maps to UNKNOWN rather than a wrong status.

## Matching & confidence
Match signals in priority order: exact tracking number → platform/order reference →
courier → marketplace → product/order hints → expected delivery window → existing
shipment state. Exact tracking-number matches give strong confidence. Never use weak
fuzzy matching to silently update an unrelated shipment.

Behavior by confidence:
- HIGH (e.g. exact tracking number + recognized marketplace + unambiguous
  transition): may auto-apply only when the user enabled automatic updates; create
  the event + optional local notification. Otherwise show a confirmation.
- MEDIUM: present a confirmation card (matched order/courier, proposed status,
  reason, [Confirm] [Ignore]).
- LOW: never modify the shipment; show as suggestion only if useful.

Invalid transitions (e.g. DELIVERED → IN_TRANSIT without a documented reopening rule)
are rejected or flagged for confirmation rather than applied.

## Local notifications
Trusted status changes may raise a local notification ("Your parcel is out for
delivery"). Dedupe by tracking event — one event, one notification. Distinct from
reminder notifications; separate channel.

## TrackingProvider (future extension point)
Keep a `TrackingProvider` boundary in the architecture so future official courier
integrations (e.g. SpxTrackingProvider, JntTrackingProvider, LbcTrackingProvider,
FlashTrackingProvider) could plug in without redesigning Order/Shipment/
TrackingEvent, UI, ViewModels, or repositories. DO NOT implement providers in v1;
`FUTURE_OFFICIAL_PROVIDER` exists only so a future provider result has a source value.

## Privacy
Do not persist raw notification bodies or full OCR text. Store the parsed result
(status, courier, tracking number, timestamps, source, confidence) and at most a
minimal `rawReference`. See `docs/specs/PRIVACY_SECURITY.md`.
