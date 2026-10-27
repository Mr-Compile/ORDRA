# Reminders & Notification Assistance Contract

## Local reminders
Order data -> reminder use case -> ReminderRepository -> WorkManager/Android notification scheduling.

Reminder schedules must be reproducible from persisted reminder/order data. Device restart/time changes should be handled according to Android platform behavior and chosen scheduling guarantees.

## Notification channels
Define stable channels such as delivery reminders and optional marketplace assistance.

## Marketplace NotificationListenerService
This is optional assistance:
Notification -> source adapter/parser -> normalized candidate -> matcher -> NotificationSuggestion -> user confirmation where uncertain -> use case -> repository.

## Safety rules
- Notification access is optional.
- Core app works without it.
- Do not store unrelated notification content.
- Filter supported source packages.
- Do not silently overwrite trusted order fields on weak matches.
- Track source of generated tracking events/status suggestions.
