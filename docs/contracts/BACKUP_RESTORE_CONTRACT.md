# Backup, Restore & Export Contract

## Backup
Local-only backup package should contain a manifest with:
- schemaVersion
- appVersion
- exportedAt
- data payload version
- records necessary to reconstruct relationships

Attachments may be included in a ZIP backup if implemented; references must remain valid after restore.

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
