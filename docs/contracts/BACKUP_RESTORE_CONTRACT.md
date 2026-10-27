# Backup, Restore & Export Contract

## Backup
Local-only backup package should contain a manifest with:
- schemaVersion
- appVersion
- exportedAt
- data payload version
- records necessary to reconstruct relationships

The payload must include `shipments` and `tracking_events` alongside orders/items —
with status source, status confidence and all timestamps — so that
Order → Shipment → TrackingEvent relationship integrity is preserved on restore.

Attachments may be included in a ZIP backup if implemented; references (including
`rawReference` pointers on tracking events) must remain valid after restore or be
re-linked/dropped with a documented rule.

## Restore
Restore flow:
Select file -> validate type -> parse manifest -> validate version -> preview summary -> conflict strategy -> transactional restore -> integrity verification -> result.

Never partially restore a multi-table backup without a defined recovery strategy.

## Conflict strategy
Document one or more explicit strategies:
- replace all local data
- merge using stable IDs
- import as new where safe

Do not guess silently.

## Export
CSV is for human/tabular export, not necessarily full-fidelity restore. JSON/ZIP is preferred for complete backup.

## Security
Do not export Groq credentials or transient secrets.
