# Ordra Frontend Megaplan

Exhaustive execution plan for the **ORDRA Frontend Complete Pack v1**. One source of truth
for the F0 → F9 frontend track: screen coverage, required states, navigation correctness,
and the frontend QA gate. Update the status table after each phase gate; do not advance
with open blockers.

Visual and component-level spec is owned by `docs/plan/MEGAPLAN_UIUX.md` (UI/UX Pack v1.1
is canonical where they diverge); data/wiring spec is owned by
`docs/plan/MEGAPLAN_INTEGRATION.md`. This file does not duplicate them — it tracks the
frontend pack's own phase sequence and gates.

## How to use this megaplan

1. Read the frontend pack docs in order:
   `docs/FRONTEND_BIBLE.md` → `docs/FRONTEND_RULES.md` → `docs/SCREEN_INVENTORY.md` →
   `docs/plan/SCREEN_STATE_MATRIX.md` → `docs/plan/FRONTEND_PHASES.md` →
   `docs/qa/FRONTEND_QA.md` → `docs/qa/SCREEN_ACCEPTANCE.md`.
2. UIUX pack governs look and feel (`docs/uiux/*` + `docs/plan/MEGAPLAN_UIUX.md`);
   contracts govern data shapes (`docs/contracts/*` + `docs/plan/MEGAPLAN_INTEGRATION.md`).
   Product/engineering rules still govern: `docs/BIBLE.md`, `docs/RULES.md`,
   `docs/TECH_STACK.md`.
3. Kick off each F-phase with `prompts/START_FRONTEND.md`; run the full audit at F9 via
   `prompts/FULL_FRONTEND_AUDIT.md`.
4. Execute ONE frontend phase at a time: Plan → Implement → Preview → Test → Audit →
   Repair → Verify → Commit → STOP and report. Never start the next phase after a gate
   passes.
5. `mockup/` mirrors the live UI (`docs/RULES.md` → UI mockup consistency). Every screen,
   component, flow, or token change updates the mockup in the same change. Drift = defect.

## Hard rules (per `docs/FRONTEND_RULES.md`)

- Android only; Kotlin + Jetpack Compose + Material 3.
- Navigation Compose with one canonical route graph; predictable back, never duplicate Home.
- UI talks to ViewModels/state — never directly to Room/DAO, Retrofit, WorkManager, or
  NotificationListenerService.
- Every screen defines Loading, Content, Empty and Error states where applicable; no
  placeholder screen may be reported complete.
- Destructive actions require confirmation or undo; preserve state across
  rotation/process recreation where reasonable.
- Design tokens only — no scattered raw colors/dimensions. Dark and light themes mandatory.
- Offline-first: no fake online dependency for offline features; Groq UI must show
  offline/failure fallback.
- AI/OCR import must show a review screen before permanent save.
- Notification access is optional with a why-explainer; rider/message detection gets its
  own consent explainer. Uncertain detection matches never mutate silently — ambiguous
  matches route to parcel-pick review.
- Friendly errors only — no stack traces, technical exception names, or raw API failures.
- Each screen needs preview/sample states and QA acceptance criteria.

## Status key

DONE / IN PROGRESS / PENDING / DEFERRED / BLOCKED — a phase is DONE only when its scope
passes `docs/qa/FRONTEND_QA.md` and the applicable critical flows of
`docs/qa/SCREEN_ACCEPTANCE.md` with executed evidence.

## Phase status board

| F phase | Scope | Master map | UI map | Status | Notes |
|---|---|---|---|---|---|
| F0 | Design foundation | 0–1A | UI-0/UI-1 | DONE | `core/ui/theme` matches `theme_tokens.json` byte-for-byte — `docs/qa/TOKEN_AUDIT_EVIDENCE.md` |
| F1 | Core offline UI | 1A–1C | UI-1..UI-3 | DONE | Onboarding/Home/Orders/Add-Edit/Detail/Timeline shipped through 1E; v1.1 redesign deltas tracked under UI-3/UI-4 |
| F2 | Discovery & organization | 1D | UI-4 | DONE | Search/filters/sort/categories/COD committed (`d728a30`); progressive-disclosure deltas under UI-4 |
| F3 | Delivery experience | 2 | UI-5 | DONE | Calendar + reminders + notification permission committed (`32284ff`); dedicated reminder-config surfaces under UI-5 |
| F4 | Smart Import | 3–4 | UI-6 | DONE | OCR engine/parser/draft/screen + Groq enhancement + offline fallback shipped (`eddcf8b`, `141b15e`) |
| F5 | Smart Parcel Status assistance | 5 | UI-7 | DONE | Share target, optional listener, confidence-gated suggestions + review (`2b0adf8`) |
| F6 | Insights | 6 | UI-8 | DONE | Spending analytics + monthly history (`a85a64b`) |
| F7 | After-purchase | 7 | UI-8 | DONE | Returns/refunds/warranties/receipts + rating (`2883923`) |
| F8 | Data portability | 8 | UI-9 | DONE | Versioned backup/restore + CSV via SAF (`2069037`) |
| F9 | Polish | 9 | UI-10 | IN PROGRESS | Release hardening done (`0e154b9`) + code-level audit PASS; device pass pending owner |

## F0 — Design foundation

**Objective:** token-driven theme and reusable-component base before screens scale up.

Deliverables:
- Theme tokens, typography, shapes, icons, state conventions per `docs/uiux/DESIGN_SYSTEM.md`
  (canonical) — verify existing `core/ui/theme/` against it.
- Navigation shell conventions: central Add/Scan action, settings route, back-stack rules.
- Component conventions: preview/sample states for every reusable component.

Acceptance: tokens centralized (no scattered raw values); light/dark verified;
`docs/qa/FRONTEND_QA.md` theme/token rows pass.

## F1 — Core offline UI

**Objective:** the everyday offline loop — first run through order lifecycle.

Deliverables (screens per `docs/SCREEN_INVENTORY.md`):
- Splash + first-run Onboarding (skip, no account).
- Home: arriving soon, pending, COD-to-prepare, upcoming parcels, recent orders,
  first-order empty state.
- Orders: list + OrderCard; archive/delete/restore with confirmation.
- Add/Edit Order: sectioned form (platform → items → pricing → payment → delivery →
  attachments → notes), required minimum (name, platform, total), review and save,
  unsaved-change guard.
- Order Detail: hero, items/pricing, payment/COD, ETA, shipment section (courier,
  tracking, status, timeline entry), edit/status/archive/restore/delete actions.
- States: Loading/Content/Empty/Error on all screens (`SCREEN_STATE_MATRIX.md` rows).

Acceptance: critical flows 1–3 of `docs/qa/SCREEN_ACCEPTANCE.md` pass; friendly
validation preserves entered data; offline = normal state.

## F2 — Discovery & organization

**Objective:** find and group orders fast.

Deliverables:
- Search across product/seller/tracking/platform/courier/notes.
- Filters: platform/status/payment/COD/category/date; active-filter count + clear-all.
- Sort: newest, oldest, delivery soonest, highest/lowest total.
- Categories/tags management; archive/history as filtered experience.
- COD summary surfaces (today / next 3 days / all pending).

Acceptance: filtered-empty vs content-empty states distinct; sort/filter deterministic;
flow 2 of `docs/qa/SCREEN_ACCEPTANCE.md` passes.

## F3 — Delivery experience

**Objective:** calendar + local reminder surfaces.

Deliverables:
- Calendar: month view, day indicators, day sheet of expected orders, COD total per day,
  exact-vs-estimated date distinction.
- Reminders: per-order settings + global defaults (same day / 1 day / 2 days / custom),
  notification permission explainer + channel guidance, permission off/on states.

Acceptance: permission flow never blocks core use; flows 6–7 of
`docs/qa/SCREEN_ACCEPTANCE.md` pass; reminder/offline states graceful.

## F4 — Smart Import

**Objective:** scan landing → image pick → OCR → optional Groq → review → save.

Deliverables (per `docs/contracts/SMART_IMPORT_CONTRACT.md`):
- Scan landing/entry: select screenshot or receive shared image; privacy note.
- Processing states: real stages only (preparing → reading → interpreting → review
  prep); offline skips Groq gracefully; no fake progress.
- Review: editable detected fields, confidence indicators on uncertain values, detected
  platform shown separately if uncertain, duplicate warning, save through the same
  `OrderDraft` validation as manual entry.
- Failure surfaces: no text, unreadable image, offline, timeout, malformed response —
  friendly per `docs/contracts/ERROR_VALIDATION_CONTRACT.md`.

Acceptance: flows 4–5 of `docs/qa/SCREEN_ACCEPTANCE.md` pass; review-before-save holds;
offline scan path complete.

## F5 — Smart Parcel Status assistance

**Objective:** share-to-Ordra UX + optional notification-assistance surfaces
(`docs/contracts/TRACKING_CONTRACT.md`).

Deliverables:
- Share-intent receiving UX landing in the safe import flow.
- Notification-access education explainer (why, privacy, continue/not-now); separate
  rider/message-detection consent explainer.
- Suggestion cards: source, matched order/shipment, proposed status, confidence/reason,
  Confirm/Ignore; ambiguous multi-match → parcel-pick review; detection history where
  useful (outcomes only, no raw content).
- Status-change notification settings: marketplace/courier/rider toggles, automatic
  high-confidence updates, ask-before-ambiguous, per-marketplace toggles.
- Status wording always shows source + freshness — never "live"/"real-time" tracking.

Acceptance: permission optional + revocable path explained; no silent mutation path in
UI; flow 10 of `docs/qa/SCREEN_ACCEPTANCE.md` passes.

## F6 — Insights

**Objective:** local spending dashboard and purchase history.

Deliverables:
- Spending: month selector, total/pending/delivered value, outstanding COD,
  platform/category breakdown, order count, average order value, local-only note.
- History: delivered/cancelled/returned/refunded with search/filter — a filtered
  experience, not a duplicated Orders implementation.
- Empty state explains data appears after orders exist; textual summaries for charts.

Acceptance: currency-grouped totals only (no mixed-currency sums); metrics arrive
pre-computed from use cases — UI does not aggregate.

## F7 — After-purchase

**Objective:** returns/refunds, warranty, receipts/attachments on orders.

Deliverables:
- Return/Refund records: reason, requested date, deadline, amount, status, notes,
  attachments, source-order link.
- Warranty: coverage dates, provider, notes, proof attachment, expiry reminder entry.
- Receipts: local attachment list, add/view/remove, preview metadata, privacy note,
  no auto-upload.

Acceptance: all records linked to their order; amount/date validation per contract;
states per `SCREEN_STATE_MATRIX.md`.

## F8 — Data portability

**Objective:** backup, restore, export UX with explicit destructive confirmations.

Deliverables:
- Backup: create local backup, explain destination via Android picker, exporting/success
  states.
- Restore: pick file → validate → preview manifest metadata → explicit replace/merge
  warning → progress → result; invalid-file and failure states.
- Export CSV/JSON via picker; explicit success/failure feedback.

Acceptance: restore preview mandatory before destructive restore; flow 9 of
`docs/qa/SCREEN_ACCEPTANCE.md` passes.

## F9 — Polish

**Objective:** production-grade consistency pass across the whole frontend.

Deliverables:
- Accessibility sweep: 48dp targets, scalable text without clipped prices/statuses,
  content descriptions, no color-only meaning, focus order.
- Adaptive layouts where practical (window size classes), process-death/state
  restoration, rotation.
- Dark/light QA on every screen; performance pass (efficient lists, limited blur,
  no heavy shadows).
- `mockup/` synced to final UI.
- Run `prompts/FULL_FRONTEND_AUDIT.md`: screen-by-screen matrix, duplicate components/
  routes, hardcoded colors, direct DAO access, technical errors, navigation traps,
  missing offline fallbacks, placeholder UI. Repair before the Phase 9/10 gate.

Acceptance: `docs/qa/FRONTEND_QA.md` full pass; `docs/qa/SCREEN_COVERAGE_MATRIX.md`
all rows marked with evidence.

## Screen audit (against current code)

Verdict vs `app/src/main/java/com/ordra/` as of 2026-10-27. Screens are from
`docs/qa/SCREEN_COVERAGE_MATRIX.md`; visual-treatment verdicts (KEEP/IMPROVE/REDESIGN)
live in `docs/plan/MEGAPLAN_UIUX.md` → Screen audit.

| Screen | Verdict | Evidence / gap |
|---|---|---|
| Splash | IMPLEMENTED | system splash + launcher branding (Phase 0) |
| Onboarding | IMPLEMENTED | `feature/onboarding/OnboardingScreen.kt` |
| Home | IMPLEMENTED | `feature/home/` — v1.1 Bento redesign under UI-4 |
| Orders | IMPLEMENTED | `feature/orders/` |
| Add Order / Edit Order | IMPLEMENTED | `feature/orderedit/` |
| Order Review | PARTIAL | folded into Add/Edit save path — verify explicit review surface at F9 audit |
| Order Detail | IMPLEMENTED | `feature/detail/` |
| Shipment Timeline | IMPLEMENTED | `feature/timeline/` |
| Search/Filters/Sort | IMPLEMENTED | in `feature/orders/` |
| Archive/History | IMPLEMENTED | archive flows in Orders/Detail |
| COD Overview | IMPLEMENTED | `feature/cod/` |
| Calendar | IMPLEMENTED | `feature/calendar/` |
| Reminders | PARTIAL | engine + editor chips + settings toggle done (Phase 2); dedicated config surfaces per UI-5 open |
| Smart Import Entry/Processing/Review | IN FLIGHT | `feature/smartimport/` + `data/ocr/` + `core/ocr/` uncommitted in working tree; review/save path open |
| Notification Assistance Permission | MISSING | Phase 5 scope |
| Status Suggestion Review | MISSING | Phase 5 scope |
| Insights | MISSING | Phase 6 scope |
| Return/Refund | MISSING | Phase 7 scope |
| Warranty | MISSING | Phase 7 scope |
| Receipts | MISSING | Phase 7 scope |
| Backup / Restore / Export | MISSING | Phase 8 scope |
| Settings | IMPLEMENTED | `feature/settings/SettingsScreen.kt` |
| About | IMPLEMENTED | `feature/settings/AboutScreen.kt` |

## Cross-track mapping

| F phase | Master phase | UI megaplan | Integration megaplan |
|---|---|---|---|
| F0 | 0–1A | UI-0, UI-1 | I0, I1 |
| F1 | 1A–1C | UI-1, UI-2, UI-3 | I1 |
| F2 | 1D | UI-4 | I1, I2 |
| F3 | 2 | UI-5 | I3 |
| F4 | 3–4 | UI-6 | I4, I5 |
| F5 | 5 | UI-7 | I6 |
| F6 | 6 | UI-8 | I2 |
| F7 | 7 | UI-8 | I7 |
| F8 | 8 | UI-9 | I8 |
| F9 | 9 | UI-10 | I9 |

## Known conflicts & open decisions

- **Navigation**: `docs/NAVIGATION.md` (Home|Orders|Scan|Calendar|More) superseded —
  canonical is `docs/uiux/NAVIGATION_UX.md` (Home|Orders|Calendar|Insights + central
  Add/Scan sheet + Settings from app bar).
- **Design system**: `design/DESIGN_SYSTEM.md` (Frontend pack) superseded — canonical is
  `docs/uiux/DESIGN_SYSTEM.md`; the design/ file carries a pointer note.
- **Screen inventory vs specs**: `docs/SCREEN_INVENTORY.md` remains the original full
  inventory; canonical per-screen spec is `docs/uiux/SCREEN_SPECS.md`. Where they
  diverge, follow the UIUX pack.
- **Track relationship**: this F-track and the UI-track cover the same screens through
  different lenses — F covers functional completion + frontend QA gate; UI-x covers
  v1.1 visual treatment. Both must pass for a master phase to be fully DONE.
- Record owner decisions here when conflicts are resolved; do not silently rewrite.
