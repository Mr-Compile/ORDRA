# Reminders & Notification Assistance Contract

## Local reminders
Order data -> reminder use case -> ReminderRepository -> WorkManager/Android notification scheduling.

Reminder schedules must be reproducible from persisted reminder/order data. Device restart/time changes should be handled according to Android platform behavior and chosen scheduling guarantees.

## Notification channels
Define stable channels such as delivery reminders, parcel-status updates, and optional marketplace assistance. Parcel-status notifications are deduplicated per tracking event — one event, one notification.

## Shipment status-change notifications
When a trusted `ShipmentStatus` change is applied (auto-applied high confidence, user-confirmed, or manual), Ordra may raise a local notification such as "Your Mechanical Keyboard is now Out for Delivery." Do not notify on rejected/low-confidence detections, and never duplicate a notification for the same TrackingEvent.

## Marketplace NotificationListenerService
Optional assistance and the primary automatic parcel-status source in v1:

Notification -> source-package filter -> platform-specific parser -> extracted signals
(tracking number, order reference, courier, raw status text, order/product hints)
-> StatusNormalizer -> local shipment match -> confidence -> TrackingSuggestion ->
apply or request confirmation -> use case -> repository -> TrackingEvent.

Safety rules:
- Notification access is optional; core app works without it and the permission can be revoked at any time.
- Filter to explicitly supported source packages only (Shopee, Lazada, TikTok Shop, other documented marketplaces); unrelated notifications are ignored.
- Do not store unrelated notification content; never persist raw notification bodies — store parsed fields only.
- Do not silently overwrite trusted data on weak matches: HIGH may auto-apply only if the user enabled it, MEDIUM asks Confirm/Ignore, LOW stays a suggestion.
- Every generated status/event records `TrackingStatusSource` and `DetectionConfidence`.
- The full detection/matching/normalization contract lives in `docs/contracts/TRACKING_CONTRACT.md`.
