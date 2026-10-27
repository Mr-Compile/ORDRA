# Integration Test Matrix

Every vertical feature should have tests at the cheapest useful layer plus critical end-to-end integration coverage.

| Flow | Required checks |
|---|---|
| Create order | validation, mapper, Room write, observable UI result |
| Edit order | persisted changes, item relationships, totals |
| Archive/restore | list visibility and data preservation |
| Delete | cascades/no orphan records |
| Search/filter/sort | deterministic Room/domain results |
| COD summary | correct minor-unit math and statuses |
| Reminder | persisted schedule -> worker -> notification intent/data |
| OCR import | OCR result -> parser -> draft |
| Groq import | DTO validation, malformed response, offline fallback |
| Smart review | user edit wins over AI suggestion |
| Notification assist | package filter, matching confidence, confirmation |
| Analytics | date/status/refund rules |
| Return/refund | relationships and calculations |
| Warranty | dates/reminder behavior |
| Backup/restore | round trip, version validation, transaction rollback |
| Migration | old schema -> new schema preserves data |
| Navigation | IDs/routes/back behavior |
| Dark/light | key screens render without inaccessible state |

Do not claim PASS without executing the applicable tests/build in the available environment.
