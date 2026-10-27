# Reminders & Notification Assistance Contract

## Local reminders
Order data -> reminder use case -> ReminderRepository -> WorkManager/Android notification scheduling.

Reminder schedules must be reproducible from persisted reminder/order data. Device restart/time changes should be handled according to Android platform behavior and chosen scheduling guarantees.

## Notification channels
Define stable channels such as delivery reminders, parcel-status updates, and optional marketplace assistance. Parcel-status notifications are deduplicated per tracking event — one event, one notification.

## Shipment status-change notifications
When a trusted `ShipmentStatus` change is applied (auto-applied high confidence, user-confirmed, or manual), Ordra may raise a local notification such as "Your Mechanical Keyboard is now Out for Delivery." Do not notify on rejected/low-confidence detections, and never duplicate a notification for the same TrackingEvent.

## NotificationListenerService (Smart Notification Detection)
Optional assistance and the primary automatic parcel-status source in v1:

Notification -> source-registry filter -> delivery-relevance check -> source-specific
parser -> extracted signals (tracking number, order reference, courier, marketplace,
COD/order amounts, raw status text, product hints, rider/delivery indicator)
-> StatusNormalizer -> local shipment match -> confidence -> TrackingSuggestion ->
apply or request confirmation -> use case -> repository -> TrackingEvent.

Supported source categories (registry-driven, extensible):
- Marketplace apps: Shopee, Lazada, TikTok Shop, other documented sources.
- Courier apps: SPX Express, J&T Express, Flash Express, Ninja Van, LBC, other
  documented couriers.
- Messaging apps (Rider Message Detection): SMS/messaging notifications carrying
  delivery-related rider texts. Notification-based only — Ordra never reads the
  SMS inbox directly; direct inbox access is a separate future capability needing
  its own privacy/permission/platform review.

Safety rules:
- Notification access is optional; core app works without it and the permission can be revoked at any time.
- Filter to explicitly supported source packages only; unrelated notifications are ignored and never stored.
- Deterministic local parsing first; ORDRA AI (Groq) is an optional, separately toggleable fallback for ambiguous delivery-related text only — never every notification.
- Do not store unrelated notification content; never persist raw notification bodies — store parsed fields + minimal provenance only.
- Do not silently overwrite trusted data on weak matches: HIGH may auto-apply only if the user enabled it, MEDIUM asks Confirm/Ignore, ambiguous matches go to parcel-pick review, LOW stays a suggestion.
- Rider/message detection is separately controllable and requires its own privacy explainer before enabling — never enabled silently.
- Every generated status/event records `TrackingStatusSource` and `DetectionConfidence`.
- The full detection/matching/normalization contract lives in `docs/contracts/TRACKING_CONTRACT.md`.
