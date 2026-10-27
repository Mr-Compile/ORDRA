# Integration Implementation Phases

## I0 — Contract alignment
Audit current code/docs against this pack. Resolve naming/schema conflicts before feature expansion.

## I1 — Core vertical slice
Wire Orders list, Add/Edit, Order Details through ViewModel/use cases/repository/Room. Verify CRUD, archive/restore, search/filter/sort.

## I2 — Dashboard/COD/analytics
Connect Room aggregate queries/use cases to Home and Insights.

## I3 — Reminders/calendar
Connect persisted delivery/reminder data to WorkManager and local notifications.

## I4 — OCR Smart Import
Connect image picker -> ML Kit -> local parser -> OrderDraft -> review -> save.

## I5 — Groq enhancement
Add optional Groq interpretation with DTO validation, field confidence, offline fallback, and secret hygiene.

## I6 — Marketplace assistance
Connect Android share intents and optional NotificationListenerService to normalized suggestions and user-confirmed updates.

## I7 — Advanced records
Integrate receipts, returns/refunds, warranties, and their reminders.

## I8 — Backup/restore/export
Implement versioned local backup and integrity-tested restore.

## I9 — Full integration audit
Run build/tests, migration tests, offline tests, permission tests, navigation regression, privacy/security review. Repair before release QA.

Each phase: PLAN -> IMPLEMENT -> TEST -> AUDIT -> REPAIR -> VERIFY -> COMMIT.
