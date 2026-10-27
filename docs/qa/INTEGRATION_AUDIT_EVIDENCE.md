# Integration Audit Evidence — 2026-10-27

Run per `prompts/INTEGRATION_AUDIT_PROMPT.md` at commit `2bd65df`.

## Executive status: **PASS**

## Checks executed

| Check | Result |
|---|---|
| Room entities leaking outside `data/` | PASS — zero `data.local.entity` imports in `feature/`, `domain/`, `app/` |
| Direct DAO calls from Compose/ViewModels | PASS — zero DAO references in `feature/`; all access via repositories |
| Committed/hardcoded secrets | PASS — zero `gsk_`/literal keys; `BuildConfig.GROQ_API_KEY` only, sourced from git-ignored `secrets.properties` |
| TODO/FIXME/dead code markers | PASS — none in `app/src` |
| StatusNormalizer single layer | PASS — `domain/service/StatusNormalizer.kt` is the sole normalizer; parser/matcher/progression separate |
| Silent status apply | PASS — `DetectionRepositoryImpl.ingest(autoApplyHigh)` only writes when user-enabled flag + HIGH confidence; all else → pending suggestion |
| Raw exception messages in UI | PASS — no `e.message`/exception.toString() in `feature/`; `AppError` sealed types only |
| Money as integer minor units | PASS — `*Minor` fields throughout; `MoneyFormat` for display |
| Room migrations | PASS — v1→v6 explicit migrations, schemas exported, no destructive fallback |
| Back-stack/nav | PASS — NavHost routes typed; AppIntake one-shot routing for share |
| Offline behavior | PASS — Groq default-off, detection default-off, all core flows local |
| Backup round-trip | PASS — `BackupCodecTest` (round-trip, invalid, newer-schema reject) |
| Doc drift | PASS — all 4 status boards synced `2bd65df` |

## Findings (non-blocking)

- **LOW** — `DetectionSuggestionEntity` serialized via `@Serializable` but excluded
  from `BackupDocument` (pending suggestions intentionally not backed up — correct).
- **LOW** — `OrdraNotificationListenerService` declared `exported=false` (correct);
  listener permission requested via system settings only (correct).

## Build/test results

- `:app:assembleDebug` — BUILD SUCCESSFUL (last run Phase 9)
- `:app:testDebugUnitTest` — all tests pass (40+)
- `:app:assembleRelease` — BUILD SUCCESSFUL, lintVital + R8 clean

## Release-readiness recommendation

All engineering gates pass. Remaining: owner-gated release signing + store listing
(Phase 10), plus optional UI-10 full-screen regression pass on device.
