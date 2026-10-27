# Ordra Implementation Queue

Single pull-from-here execution queue across the plan tracks. Order is dependency-driven.
A queue item is DONE only when its master + frontend + UI + integration gates for that
scope all pass with executed evidence (`docs/MEGAPLAN.md` → rules of execution).

## Source audit (2026-10-27)

- All 5 pack zips in `docs/zip/` verified exported into the repo. Foundation Pack v1's
  4 old phase files were renumbered into the Complete Project Pack v2 (`PHASE_01_FOUNDATION`
  → `PHASE_01A`, `PHASE_02_REMINDERS` → `PHASE_02_REMINDERS_CALENDAR`,
  `PHASE_03_SMART_IMPORT` → `PHASE_03_SCREENSHOT_OCR`, `PHASE_04_PLATFORM_ASSISTANCE`
  → `PHASE_05_PLATFORM_ASSISTANCE`) — merges, not gaps.
- Megaplan coverage: UIUX → `MEGAPLAN_UIUX.md`; Integration → `MEGAPLAN_INTEGRATION.md`;
  Complete Project v2 + Foundation v1 → umbrella `docs/MEGAPLAN.md`; Frontend →
  `MEGAPLAN_FRONTEND.md` (added this audit); Foundation v1 superseded by v2.
- Status corrections applied: Phase 1B → DONE (`d7a3548`), Phase 2 → DONE (`32284ff`),
  Phase 3 + F4 + UI-6 + I4 → IN PROGRESS (uncommitted work in tree), UI-5 → IN PROGRESS.

## Rules

- Pull the HEAD item only. Never work two items or skip ahead.
- Per item: run its entry prompt, implement only that scope, then run its QA gates.
- One item, one commit; push `origin main`; update all status boards + this queue in the
  same commit; then STOP and report (`AGENTS.md`).
- Phase 3 code is uncommitted in the working tree — item 1 continues it; do not revert,
  do not bundle it into a docs or unrelated commit.

## Queue

| # | Item | Master | Frontend | UI | Integration | Entry prompt | Gate docs | Status |
|---|---|---|---|---|---|---|---|---|
| 1 | Screenshot Smart Import — finish in-flight: image pick → ML Kit → deterministic parser → `OrderDraft` → review → save; shipment detections route through StatusNormalizer → `TrackingSuggestion` (never silent); works fully offline | Phase 3 | F4 | UI-6 | I4 | `prompts/PHASE_EXECUTION_PROMPT.md` (+ `prompts/START_FRONTEND.md`, `prompts/START_UI_IMPLEMENTATION_PROMPT.md` lenses) | `docs/plan/PHASE_03_SCREENSHOT_OCR.md`, `docs/qa/SMART_IMPORT_QA.md`, `docs/qa/TRACKING_QA.md`, `docs/qa/SCREEN_ACCEPTANCE.md` flows 4–5 | DONE |
| 2 | Groq-assisted extraction — optional online layer over the local pipeline; Retrofit/OkHttp + kotlinx DTOs, strict schema, `secrets.properties` key only, graceful offline/4xx/5xx/rate-limit fallback | Phase 4 | F4 | UI-6 | I5 | `prompts/PHASE_EXECUTION_PROMPT.md` | `docs/plan/PHASE_04_GROQ_SMART_EXTRACTION.md`, `docs/qa/SMART_IMPORT_QA.md`, `docs/qa/INTEGRATION_TEST_MATRIX.md` Groq rows | DONE |
| 3 | Smart Parcel Status assistance — share target, optional NotificationListenerService, source registry + parsers, StatusNormalizer, confidence-gated matching, suggestion review | Phase 5 | F5 | UI-7 | I6 | `prompts/PHASE_EXECUTION_PROMPT.md` | `docs/plan/PHASE_05_PLATFORM_ASSISTANCE.md`, `docs/contracts/TRACKING_CONTRACT.md`, `docs/qa/TRACKING_QA.md`, flow 10 | DONE |
| 4 | Spending analytics & history — monthly/weekly totals, platform/category breakdown, currency-grouped | Phase 6 | F6 | UI-8 | I2 | `prompts/PHASE_EXECUTION_PROMPT.md` | `docs/plan/PHASE_06_ANALYTICS_HISTORY.md`, `docs/contracts/ANALYTICS_CONTRACT.md`, matrix analytics rows | DONE |
| 5 | Post-purchase tools — returns/refunds, warranties, receipts/attachments | Phase 7 | F7 | UI-8 | I7 | `prompts/PHASE_EXECUTION_PROMPT.md` | `docs/plan/PHASE_07_POST_PURCHASE.md`, `docs/contracts/DOMAIN_MODELS.md` record types, matrix return/warranty rows | DONE |
| 6 | Backup, restore & export — versioned manifest, preview + explicit conflict strategy, transactional restore, CSV export | Phase 8 | F8 | UI-9 | I8 | `prompts/PHASE_EXECUTION_PROMPT.md` | `docs/plan/PHASE_08_BACKUP_EXPORT.md`, `docs/contracts/BACKUP_RESTORE_CONTRACT.md`, flows 8–9 | DONE |
| 7 | Hardening & release prep + full audits — security/privacy/perf, a11y, migrations, release config; Groq key decision for distribution | Phase 9 | F9 | UI-10 | I9 | `prompts/FULL_CODEBASE_AUDIT_PROMPT.md` + `prompts/FULL_FRONTEND_AUDIT.md` + `prompts/UI_AUDIT_PROMPT.md` + `prompts/INTEGRATION_AUDIT_PROMPT.md` | `docs/qa/MASTER_QA_CHECKLIST.md`, `docs/qa/RELEASE_QA.md` | DONE |
| 8 | Beta & launch — signing, store listing, rollout | Phase 10 | — | — | — | owner decision required | `docs/plan/PHASE_10_BETA_LAUNCH.md` | BLOCKED-OWNER |

## Standing obligations (every item)

- `mockup/` updated with any UI change; `branding/theme_tokens.json` stays in sync —
  drift = defect (`docs/RULES.md`).
- Status boards kept truthful in the same commit: `docs/MEGAPLAN.md`,
  `docs/plan/MEGAPLAN_FRONTEND.md`, `docs/plan/MEGAPLAN_UIUX.md`,
  `docs/plan/MEGAPLAN_INTEGRATION.md`, and this queue.
- UI-0 token-migration checklist items land inside their mapped phases
  (`docs/plan/MEGAPLAN_UIUX.md` → migration checklist) — not separate queue items.
- Commit convention: one item, one commit, clear message, push `origin main`
  (`AGENTS.md`).
- No PASS/DONE marked without executed build + test evidence
  (`docs/qa/MASTER_QA_CHECKLIST.md`).
