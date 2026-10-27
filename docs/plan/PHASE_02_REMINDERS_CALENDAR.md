# Phase 2 — Reminders, Calendar & Local Notifications

## Goal
Help users prepare for expected deliveries without relying on marketplace APIs.

## Implement
- WorkManager-backed reminder scheduling.
- Delivery-day, 1-day-before, 2-days-before and custom reminders.
- COD amount in reminders when appropriate.
- Daily optional summary of expected parcels and COD.
- Delivery calendar/upcoming timeline.
- Reschedule/cancel work when order dates/statuses change.
- Notification channels and settings, including a parcel-status-update channel used by trusted shipment status changes (dedupe per TrackingEvent; see TRACKING_CONTRACT).
- Handle reboot/timezone/date changes safely.

## Exit criteria
[ ] No reminder fires for delivered/cancelled/refunded orders.
[ ] Editing ETA updates scheduled work.
[ ] Duplicate notifications are prevented.
[ ] Notification permission UX is contextual and optional.
