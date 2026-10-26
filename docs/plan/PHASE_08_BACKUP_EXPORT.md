# Phase 8 — Backup, Restore & Export

## Implement
- Local export with explicit schema/version metadata.
- Restore preview, validation and duplicate handling.
- CSV export for user-readable order history.
- Optional encrypted backup design. Backup is local export/import only (JSON/ZIP package; CSV for tabular exports). (OUT OF CURRENT SCOPE / FUTURE CONSIDERATION: cloud sync — a separate owner decision, never assumed.)

## Exit criteria
[ ] Round-trip backup/restore passes tests.
[ ] Bad/old backups fail safely with useful messages.
[ ] Export excludes data the user did not choose where applicable.
