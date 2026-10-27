# ORDRA Integration & Data Contract Pack v1

This pack is the canonical integration contract for Ordra, an Android-only, local-first personal order and parcel manager.

## Purpose
Prevent drift between Compose UI, ViewModels, domain/use cases, repositories, Room, reminders, OCR, Groq Smart Import, notification assistance, analytics, backup/restore, and advanced order records.

## Hard constraints
- Android only: Kotlin + Jetpack Compose.
- Room/SQLite is the source of truth.
- Core features work offline.
- No accounts, cloud database, cloud sync, or custom backend.
- Groq is the only planned online dependency and is optional.
- ML Kit OCR is local.
- Never commit a real Groq API key.
- UI never talks directly to Room, Retrofit, WorkManager, or NotificationListenerService.

## Canonical flow
Compose -> ViewModel -> Use Case -> Repository -> Data Source/Service
Data changes return through Flow to the UI.

Pack files live in `docs/contracts/`; its phase/test-matrix/prompt files were merged into the
canonical `docs/plan/`, `docs/qa/` and `prompts/` folders. Execution roadmap:
`docs/plan/MEGAPLAN_INTEGRATION.md`.

Read the files in this order:
1. docs/contracts/CONTRACT_BIBLE.md
2. docs/contracts/DOMAIN_MODELS.md
3. docs/contracts/DATABASE_CONTRACT.md
4. docs/contracts/REPOSITORY_USECASE_CONTRACT.md
5. docs/contracts/UI_STATE_EVENT_CONTRACT.md
6. docs/contracts/SMART_IMPORT_CONTRACT.md
7. docs/contracts/REMINDERS_NOTIFICATION_CONTRACT.md
8. docs/contracts/ANALYTICS_CONTRACT.md
9. docs/contracts/BACKUP_RESTORE_CONTRACT.md
10. docs/contracts/ERROR_VALIDATION_CONTRACT.md
11. docs/qa/INTEGRATION_TEST_MATRIX.md
12. docs/plan/INTEGRATION_PHASES.md
13. prompts/START_INTEGRATION_PROMPT.md
