# Ordra Documentation Index

All project documentation lives under `docs/`. Only `README.md` and `AGENTS.md` remain at the
repo root. Start with `docs/START_HERE_PROMPT.md`.

## Canonical product & engineering docs (docs/)

| File | Purpose |
|---|---|
| `docs/BIBLE.md` | Product bible — identity, principles, statuses, platform values, scope |
| `docs/RULES.md` | Non-negotiable product, engineering and AI-coding rules |
| `docs/TECH_STACK.md` | Approved stack (Kotlin, Compose M3, Room, Hilt, WorkManager, Groq-optional) |
| `docs/BRANDING.md` | Brand identity, colors, status semantics, logo/splash direction |
| `docs/MEGAPLAN.md` | Umbrella merged roadmap across all packs + phase status board |
| `docs/START_HERE_PROMPT.md` | Copy/paste bootstrap prompt + mandatory reading order |
| `docs/FRONTEND_BIBLE.md` | Frontend product promises and UI principles |
| `docs/FRONTEND_RULES.md` | Frontend-specific rules (states, nav, offline, review-before-save) |
| `docs/NAVIGATION.md` | Original route list (superseded note → `docs/uiux/NAVIGATION_UX.md`) |
| `docs/SCREEN_INVENTORY.md` | Original full screen inventory (canonical specs → `docs/uiux/SCREEN_SPECS.md`) |

## docs/plan/ — phase plans & megaplans

| File | Purpose |
|---|---|
| `docs/plan/MASTER_PLAN.md` | Master phase sequence 0 → 10 |
| `docs/plan/IMPLEMENTATION_QUEUE.md` | Pull-from-here execution queue across all tracks |
| `docs/plan/MEGAPLAN_FRONTEND.md` | Exhaustive frontend execution plan (phases F0 → F9) |
| `docs/plan/MEGAPLAN_UIUX.md` | Exhaustive UI/UX execution plan (phases UI-0 → UI-10) |
| `docs/plan/MEGAPLAN_INTEGRATION.md` | Exhaustive contract/integration plan (phases I0 → I9) |
| `docs/plan/PHASE_00_PRODUCT_BRANDING.md` … `docs/plan/PHASE_10_BETA_LAUNCH.md` | Per-phase implementation plans (00, 01A–01E, 02–10) |
| `docs/plan/FRONTEND_PHASES.md` | Frontend phase track F0 → F9 |
| `docs/plan/UI_IMPLEMENTATION_PHASES.md` | UI/UX Pack phase track UI-0 → UI-10 |
| `docs/plan/INTEGRATION_PHASES.md` | Contract Pack phase track I0 → I9 |
| `docs/plan/SCREEN_STATE_MATRIX.md` | Required states per screen |

## docs/qa/ — gates, matrices, audits

`docs/qa/MASTER_QA_CHECKLIST.md`, `docs/qa/PHASE_01_QA.md`, `docs/qa/RELEASE_QA.md`,
`docs/qa/CODEBASE_AUDIT.md`, `docs/qa/SMART_IMPORT_QA.md`, `docs/qa/TRACKING_QA.md`,
`docs/qa/FRONTEND_QA.md`,
`docs/qa/SCREEN_ACCEPTANCE.md`, `docs/qa/UI_UX_QA.md`, `docs/qa/SCREEN_COVERAGE_MATRIX.md`,
`docs/qa/INTEGRATION_TEST_MATRIX.md`.

## docs/specs/ — specifications

`docs/specs/ARCHITECTURE.md`, `docs/specs/DATA_MODEL.md`, `docs/specs/PRIVACY_SECURITY.md`,
`docs/specs/UX_DESIGN_SYSTEM.md`, `docs/specs/ASSET_MANIFEST.md` (required brand assets from
the UI/UX Pack).

## docs/uiux/ — UI/UX Complete Pack v1.1

Canonical UI spec docs (hybrid direction: 70% Minimal/Material, 20% Bento, 10% glass/
gradient/aurora). Read in the order listed in `docs/uiux/README.md`:
`docs/uiux/UI_UX_BIBLE.md`, `docs/uiux/DESIGN_SYSTEM.md`, `docs/uiux/COMPONENT_LIBRARY.md`,
`docs/uiux/SCREEN_SPECS.md`, `docs/uiux/NAVIGATION_UX.md`, `docs/uiux/ACCESSIBILITY.md`,
`docs/uiux/MOTION_AND_FEEDBACK.md`, `docs/uiux/BRANDING_UI.md`.
Execution: `docs/plan/MEGAPLAN_UIUX.md` (includes screen audit + migration checklist).

## docs/contracts/ — Integration & Data Contract Pack v1

Canonical wiring contracts (Compose → ViewModel → UseCase → Repository → Room/services).
Read in the order listed in `docs/contracts/README.md`: `docs/contracts/CONTRACT_BIBLE.md`,
`docs/contracts/DOMAIN_MODELS.md`, `docs/contracts/DATABASE_CONTRACT.md`,
`docs/contracts/TRACKING_CONTRACT.md` (Smart Parcel Status — no-API tracking architecture),
`docs/contracts/REPOSITORY_USECASE_CONTRACT.md`, `docs/contracts/UI_STATE_EVENT_CONTRACT.md`,
`docs/contracts/SMART_IMPORT_CONTRACT.md`, `docs/contracts/REMINDERS_NOTIFICATION_CONTRACT.md`,
`docs/contracts/ANALYTICS_CONTRACT.md`, `docs/contracts/BACKUP_RESTORE_CONTRACT.md`,
`docs/contracts/ERROR_VALIDATION_CONTRACT.md`.
Execution: `docs/plan/MEGAPLAN_INTEGRATION.md`.

## docs/zip/ — source archives

Original pack zips, kept as provenance: Foundation v1, Complete Project v2, Frontend v1,
UI/UX Complete v1, Integration & Data Contract v1.

## Other doc-bearing folders

- `prompts/` — copy/paste agent prompts: `prompts/PHASE_EXECUTION_PROMPT.md`,
  `prompts/FULL_CODEBASE_AUDIT_PROMPT.md`, `prompts/FULL_FRONTEND_AUDIT.md`,
  `prompts/START_FRONTEND.md`, `prompts/START_UI_IMPLEMENTATION_PROMPT.md`,
  `prompts/UI_AUDIT_PROMPT.md`, `prompts/START_INTEGRATION_PROMPT.md`,
  `prompts/INTEGRATION_AUDIT_PROMPT.md`.
- `design/DESIGN_SYSTEM.md` — Frontend-pack design tokens (superseded by
  `docs/uiux/DESIGN_SYSTEM.md`, kept for reference).
- `mockup/README.md` — interactive HTML mockup mirror of the live UI.
- `branding/theme_tokens.json`, `assets/` — brand tokens and artwork.
