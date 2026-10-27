# Ordra Megaplan

Merged execution roadmap across the Foundation Pack, Complete Project Pack (v2), Frontend Pack,
UI/UX Complete Pack and Integration & Data Contract Pack.
One source of truth for phase order and status. Update status after each phase gate.

All canonical docs live under `docs/` (see `docs/README.md` for the index). README.md and
AGENTS.md remain at the repo root.

## Pack megaplans

- `docs/plan/MEGAPLAN_FRONTEND.md` — exhaustive frontend execution plan (phases F0 → F9:
  design foundation, core offline UI, discovery, delivery experience, Smart Import,
  parcel-status assistance, insights, after-purchase, data portability, polish).
- `docs/plan/MEGAPLAN_UIUX.md` — exhaustive UI/UX execution plan (phases UI-0 → UI-10:
  design foundation, nav shell, components, all screens, states, a11y, polish).
- `docs/plan/MEGAPLAN_INTEGRATION.md` — exhaustive integration/data-contract plan
  (phases I0 → I9: contract alignment, orders slice, analytics, reminders, OCR/Groq import,
  assistance, advanced records, backup, full audit).

Pack coverage (source zips in `docs/zip/`): Foundation Pack v1 is superseded by the
Complete Project Pack v2 (merged — its 4 phase files were renumbered into `PHASE_01A`,
`PHASE_02`, `PHASE_03`, `PHASE_05`); Complete Project Pack v2 is this umbrella board plus
`docs/plan/MASTER_PLAN.md` + `PHASE_*` files; Frontend Pack v1 → MEGAPLAN_FRONTEND.md;
UI/UX Pack v1 → MEGAPLAN_UIUX.md; Integration & Data Contract Pack v1 →
MEGAPLAN_INTEGRATION.md. Pull-from work order: `docs/plan/IMPLEMENTATION_QUEUE.md`.

Both track the same phases below through their own lenses: UI-x covers screens/components/states;
I-x covers contracts/wiring/data correctness. A master phase is fully DONE only when its product,
UI and integration gates all pass.

## Rules of execution
- Workflow per phase: Plan → Implement → Test → Audit → Repair → Verify → Commit.
- No phase proceeds on a failed gate. No future-phase features early.
- Architecture: Android-only, Kotlin + Compose M3, MVVM/Clean, Room source of truth,
  Hilt, DataStore, WorkManager, Nav Compose, Coil. Local-only: no backend/accounts/cloud.
- Only online service: direct Groq API (Phase 4+), key in git-ignored `secrets.properties`.
  Tracking is local-first Smart Parcel Status — no courier APIs, third-party trackers,
  or scraped pages; detection sources are marketplace/courier/messaging notifications
  (messaging via notifications only, never SMS inbox), OCR, shared content, manual;
  shipments/events live in Room per `docs/contracts/TRACKING_CONTRACT.md`.
- `mockup/` mirrors the live UI; update it with any UI change (drift = defect).

## Status key
DONE / IN PROGRESS / PENDING / DEFERRED / BLOCKED

| Phase | Scope | Frontend map | Status | Notes |
|---|---|---|---|---|
| 0 | Product & branding foundation | F0 (partial) | DONE | Name, tagline, tokens, light/dark theme, adaptive icon, system splash, launcher branding |
| 1A | Android foundation + persistence | F0 | DONE | Contract-aligned domain models, Room entities/DAO/db, repository + use cases, settings DS, nav shell, AppError model, 10 unit tests green, assembleDebug PASS |
| 1B | Manual order + shipment management | F1 | DONE | Add/edit multi-section form, shipments with courier/tracking/status, review-before-save, archive/restore/delete (commit `d7a3548`) |
| 1C | Home & order details | F1 | DONE | Summary metrics + upcoming/recent, OrderCard+StatusChip, detail hero/shipment/items/payment, timeline screen, mark-delivered, track-external |
| 1D | Search/filters/categories/COD | F2 | DONE | RawQuery search (title/seller/notes/items/tracking), platform/status/COD/category filters, 5 sorts, categories+junction (v3), COD overview (today/3d/all), 7 query tests |
| 1E | Settings, onboarding, polish, core QA | F2 | DONE | Theme/currency/default platform+payment settings, 3-page onboarding gate, About w/ privacy+licenses, editor prefilled defaults |
| 2 | Reminders, calendar, notifications | F3 | DONE | Reminder model+Room v4, WorkManager sync on save/status/archive, delivery-day/-1/-2 chips, daily summary worker+toggle+POST_NOTIFICATIONS, delivery calendar screen |
| 3 | Screenshot Smart Import (local) | F4 | IN PROGRESS | Photo picker, ML Kit OCR, deterministic parser, OrderDraft + shipment detection, confidence, review — OCR engine/parser/draft/screen in working tree; review→save path open |
| 4 | Groq-assisted extraction | F4 | PENDING | Direct app→Groq (Retrofit/OkHttp/kotlinx), strict schema, graceful offline fallback |
| 5 | Smart Parcel Status assistance | F5 | PENDING | 5A Smart Notification Detection (marketplace/courier/messaging sources, rider messages, matching+confidence, ambiguous review) → 5B normalization+courier registry → 5C provider abstraction+update pipeline → 5D optional external API |
| 6 | Spending analytics & history | F6 | PENDING | Monthly/weekly totals, platform/category breakdown, local-only |
| 7 | Post-purchase tools | F7 | PENDING | Returns/refunds, warranties, receipts/attachments |
| 8 | Backup, restore & export | F8 | PENDING | JSON/ZIP backup, CSV export, restore validation |
| 9 | Hardening & release prep | F9 | PENDING | Security/privacy/perf audit, a11y, migrations, release config; Groq key decision for channel |
| 10 | Beta & launch | F9 | PENDING | Needs owner: signing, store listing, beta rollout |

## Current run scope
Offline core through Phase 2 committed; current head is Phase 3 (Smart Import, in-flight
in working tree). Pull next items from `docs/plan/IMPLEMENTATION_QUEUE.md`.
Deferred screens until their phase: spending, returns/warranty, backup,
notification assistance.

## Deferred decisions (owner)
- Public distribution → Groq key architecture reassessment (Phase 9).
- minSdk 26 confirmed; revisit to 24 only if install-base demands it.
- Navigation spec conflict: `docs/NAVIGATION.md` (Scan/More tabs) vs
  `docs/uiux/NAVIGATION_UX.md` (Home|Orders|Calendar|Insights + central Add/Scan) —
  UIUX pack treated as canonical pending confirmation.

## Cross-pack phase tracks

| Master phase | UI track (`docs/plan/MEGAPLAN_UIUX.md`) | Integration track (`docs/plan/MEGAPLAN_INTEGRATION.md`) |
|---|---|---|
| 0–1A | UI-0, UI-1 | I0, I1 |
| 1B–1D | UI-2, UI-3, UI-4 | I1, I2 |
| 1E | UI-9 (settings part) | I2 |
| 2 | UI-5 | I3 |
| 3–4 | UI-6 | I4, I5 |
| 5 | UI-7 | I6 |
| 6–7 | UI-8 | I2, I7 |
| 8 | UI-9 | I8 |
| 9–10 | UI-10 | I9 |
