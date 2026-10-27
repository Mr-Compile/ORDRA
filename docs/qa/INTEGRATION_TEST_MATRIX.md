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
| Notification assist | source registry filter (marketplace/courier/messaging), delivery-relevance gate, matching confidence, confirmation |
| Rider message detection | Filipino/English patterns parse as candidates; unknown senders match on parcel signals; inbox never read |
| Ambiguous match | multiple candidates (e.g. identical COD) → review UI, user picks parcel, never arbitrary |
| Shipment CRUD | create/update/delete, multiple shipments per order, cascades |
| Tracking events | shipmentId link, timeline ordering, duplicate prevention (composite dedupe key) |
| Status normalization | raw wording -> ShipmentStatus, unknown -> UNKNOWN |
| Status transitions | valid progression applies; stale/regressing detections rejected or escalated |
| Tracking match | exact tracking number strong match; ambiguous never silently updates |
| Confidence gates | HIGH auto-apply only if enabled; MEDIUM confirm; LOW suggest-only |
| Manual status | always available, writes MANUAL event |
| Status notification | local notification on applied change, dedupe per event |
| AI fallback (notification) | ambiguous text → minimized payload only; offline/AI-off → deterministic path unaffected; AI output never authoritative |
| Detection history | outcomes recorded with structured fields; no raw notification bodies |
| Track Externally | opens official page only, no scrape-back |
| Analytics | date/status/refund rules |
| Return/refund | relationships and calculations |
| Warranty | dates/reminder behavior |
| Backup/restore | round trip incl. shipments + tracking events, version validation, transaction rollback |
| Migration | old schema -> new schema preserves data |
| Navigation | IDs/routes/back behavior |
| Dark/light | key screens render without inaccessible state |

Do not claim PASS without executing the applicable tests/build in the available environment.
