# Ordra UI/UX Megaplan

Exhaustive execution plan for the **ORDRA UI/UX Complete Pack v1**. One source of truth for every
UI phase, screen, component, state, and QA gate. Update the status table after each phase gate;
do not advance with open blockers.

## How to use this megaplan

1. Read the canonical sources first, in this order:
   `docs/uiux/UI_UX_BIBLE.md` → `docs/uiux/DESIGN_SYSTEM.md` → `docs/uiux/COMPONENT_LIBRARY.md`
   → `docs/uiux/SCREEN_SPECS.md` → `docs/uiux/NAVIGATION_UX.md` → `docs/uiux/ACCESSIBILITY.md`
   → `docs/uiux/MOTION_AND_FEEDBACK.md` → `docs/plan/UI_IMPLEMENTATION_PHASES.md`
   → `docs/qa/UI_UX_QA.md` → `prompts/START_UI_IMPLEMENTATION_PROMPT.md`.
2. Product/engineering rules still govern: `docs/BIBLE.md`, `docs/RULES.md`,
   `docs/FRONTEND_BIBLE.md`, `docs/FRONTEND_RULES.md`, `docs/BRANDING.md`,
   `docs/uiux/BRANDING_UI.md`.
3. Data/shape contracts govern what the UI renders: `docs/contracts/` (especially
   `docs/contracts/UI_STATE_EVENT_CONTRACT.md`, `docs/contracts/ERROR_VALIDATION_CONTRACT.md`).
   See `docs/plan/MEGAPLAN_INTEGRATION.md`.
4. Execute ONE UI phase at a time: Inspect → Implement → Preview → Test → UI Audit → Repair
   → Verify → Commit → STOP and report. Never start the next phase after a gate passes.
5. `mockup/` mirrors the live UI (`docs/RULES.md` → UI mockup consistency). Every screen,
   component, flow, or token change updates the mockup in the same change. Drift = defect.

## Status key

DONE / IN PROGRESS / PENDING / DEFERRED / BLOCKED — a phase is DONE only when every screen in
scope passes `docs/qa/UI_UX_QA.md` and `docs/qa/SCREEN_COVERAGE_MATRIX.md` is updated with evidence.

## Phase status board

| UI phase | Scope | Master map | Frontend map | Status | Notes |
|---|---|---|---|---|---|
| UI-0 | Design foundation | Phase 0 | F0 | IN PROGRESS | Theme/tokens partially exist (`core/ui/theme`); verify vs `docs/uiux/DESIGN_SYSTEM.md` |
| UI-1 | Navigation shell | Phase 1A | F0/F1 | PENDING | Nav spec conflict open — see Conflicts |
| UI-2 | Core order components | Phase 1A/1B | F1 | PENDING | Depends on UI-0 tokens |
| UI-3 | Order management screens | Phase 1B/1C | F1 | PENDING | Add/Edit, Review, Detail, archive |
| UI-4 | Home + search + COD | Phase 1C/1D | F1/F2 | PENDING | Dashboard metrics via contracts |
| UI-5 | Calendar + reminders | Phase 2 | F3 | PENDING | Local notifications only |
| UI-6 | Smart Import | Phase 3/4 | F4 | PENDING | Offline fallback mandatory |
| UI-7 | Smart Parcel Status assistance | Phase 5 | F5 | PENDING | Optional notification access; confidence-gated suggestions |
| UI-8 | Insights + advanced lifecycle | Phase 6/7 | F6/F7 | PENDING | Analytics is local-only |
| UI-9 | Settings + backup + about | Phase 1E/8 | F2/F8 | PENDING | No account/cloud sections |
| UI-10 | Polish & regression | Phase 9 | F9 | PENDING | Full-screen regression pass |

## Canonical navigation model (per `docs/uiux/NAVIGATION_UX.md`)

- Bottom destinations: **Home, Orders, Calendar, Insights**.
- Central **Add/Scan** is an action opening a modal bottom sheet — Add Order Manually /
  Scan Screenshot / Import Image — not a back-stack destination.
- Settings opens from top-level app bars. Order Detail pushes from Home/Orders/Calendar/History.
- Edit Order pushes from Detail and returns to the same order after save. Smart Import Review
  returns to the created Order Detail after save.
- Back behavior: no duplicate Home, no loops, no lost form state. Deep links/share intents enter
  a safe import flow and return normally.
- Conflict: `docs/NAVIGATION.md` lists tabs Home|Orders|Scan|Calendar|More — treat
  `docs/uiux/NAVIGATION_UX.md` as canonical until owner resolves (see Conflicts).

## Canonical component set (per `docs/uiux/COMPONENT_LIBRARY.md`)

Implement once in `core/ui/` (or equivalent), reuse everywhere — no per-screen duplicates:

- Chrome: `OrdraTopAppBar`, `OrdraBottomBar` (with central Add/Scan affordance).
- Actions: `PrimaryButton`, `SecondaryButton`, `TextButton`, `DestructiveButton`,
  `ConfirmationDialog`, `Snackbar` conventions.
- Order display: `OrderCard` (compact + regular), `UpcomingParcelCard`, `StatusChip`,
  `ShipmentStatusChip`, `PlatformBadge`, `CourierBadge`, `CodChip`, `PaidChip`,
  `PaymentStatusChip`, `MoneyText`, `TimelineItem` (tracking events with source label).
- Dashboard: `SummaryMetricCard` (Active, Arriving Soon, COD, monthly spend), `InsightCard`.
- Discovery: `SearchBar`, `FilterChipRow`, `SortSheet`.
- States: `EmptyState`, `ErrorState`, `OfflineBanner` (non-blocking), `LoadingSkeleton`
  (only where waiting is real).
- Forms: `FormSection`, `LabeledField`, `MoneyField`, `DateRangeField`, `PlatformPicker`,
  `StatusPicker`, `PaymentPicker`.
- Import/assist: `ProductImagePicker`, `ReceiptAttachmentCard`, `ConfidenceField`,
  `SmartImportResultCard`, `PermissionExplainer`.
- Tracking: `ShipmentCard` (courier, tracking number, status, detected-at + source),
  `ShipmentTimelineItem` (status, timestamp, source attribution), `TrackingSuggestionCard`
  (matched order, proposed status, confidence, Confirm/Ignore), `TrackExternallyAction`.

Every component: light+dark themes, large-font tolerance, loading/disabled/error variants where
applicable, Compose previews where practical, 48dp touch targets, semantics for TalkBack.

## UI-0 — Design foundation

**Objective:** one token-driven Material 3 theme; no scattered raw values.

Deliverables:
- Color roles exactly per `docs/uiux/DESIGN_SYSTEM.md` light/dark tables (verify existing
  `app/src/main/java/com/ordra/core/ui/theme/` — reconcile `Color.kt`/`Theme.kt`/`Type.kt`
  against canonical tokens; `design/DESIGN_SYSTEM.md` points here as canonical).
- Status→color mapping (Ordered=Indigo … Refunded=Teal) exposed as semantic tokens; status
  color on chip/icon/accent only, never full-card fill.
- Typography: M3 type scale mapped Display→hero numbers, Headline→summaries, Title→cards,
  Body→content, Label→chips/metadata; respect system font scaling.
- Spacing tokens on 4dp grid (4/8/12/16/20/24/32); screen padding 16dp compact, 24dp wide.
- Shape tokens: chips 8–12dp, inputs/buttons 12–16dp, cards 16–20dp, M3 sheets.
- Elevation policy: tonal surfaces; reserved elevation for FAB/sheets/dialogs.
- Icon rules: Material Symbols for function icons; custom SVG only for Ordra mark
  (see `docs/specs/ASSET_MANIFEST.md`); no emoji as production icons.
- Theme preview harness: light/dark preview of tokens + core components.

Acceptance: zero hardcoded hex/dp outside theme/components; both themes render correctly;
`docs/qa/UI_UX_QA.md` design-system row passes.

## UI-1 — Navigation shell

**Objective:** app skeleton — splash, onboarding, main shell, Add/Scan sheet, settings route.

Deliverables:
- Splash: Ordra mark + wordmark, system-aware light/dark, no artificial delay
  (`docs/uiux/BRANDING_UI.md`).
- First-run onboarding: ≤3 pages (Organize orders / Smart screenshot import / Local-first
  reminders+privacy), Skip, no account creation; persisted first-run flag (DataStore).
- `MainShell` with `OrdraBottomBar`: Home, Orders, Calendar, Insights + central Add/Scan action.
- Add/Scan modal bottom sheet: Add Order Manually / Scan Screenshot / Import Image.
- Route graph per `docs/uiux/NAVIGATION_UX.md`; single source of truth for routes; settings +
  about reachable from top app bar.
- Back-stack rules: no duplicate Home; tab reselect may scroll to top; form screens prompt
  before discarding unsaved changes.

Acceptance: every declared route resolves or is explicitly stub-flagged; back behavior verified
on all tab/push paths; `docs/qa/SCREEN_ACCEPTANCE.md` critical flow 1 passes.

## UI-2 — Core order components

**Objective:** build the reusable order-display and state components before the screens.

Deliverables:
- `OrderCard` regular + compact, `UpcomingParcelCard` (ETA + COD first), `StatusChip`,
  `PlatformBadge`, `CodChip`/`PaidChip`/`PaymentStatusChip`, `MoneyText` (locale/currency-safe,
  minor-unit input), `TimelineItem`, `SummaryMetricCard`.
- `EmptyState`, `ErrorState` (friendly copy + retry), `OfflineBanner`, `LoadingSkeleton`.
- Money/date formatting utilities in the UI layer consuming domain values only.
- Compose previews: each component × {light, dark} × {normal, large font} where practical.

Acceptance: components consume domain/UiState models only (no Room entities, no DAO); chips use
semantic status mapping; previews compile and render.

## UI-3 — Order management screens

**Objective:** Orders list, Add/Edit, Review, Order Detail, archive/history interactions.

Deliverables per `docs/uiux/SCREEN_SPECS.md`:
- Orders: search top bar, filter chips (status/platform/payment), sort action, scrollable
  `OrderCard` list, archive access, distinct empty vs no-result states.
- Add/Edit: progressive sections — Platform → Order Items → Pricing → Payment → Delivery →
  Attachments → Notes; minimum required: recognizable name, platform, total; sticky Save/Review;
  preserve input across navigation/config change (`isDirty` guard per
  `docs/contracts/UI_STATE_EVENT_CONTRACT.md`).
- Order Review (pre-save summary) when flow requires explicit review step.
- Order Detail: hero (title/platform/status/total), ETA, **Shipment section** — per parcel:
  courier, tracking number, `ShipmentStatusChip`, detected-at + source attribution,
  actions View Timeline / Update Status / Track Externally; items/pricing, payment/COD,
  notes, attachments, warranty, return/refund entries; actions Edit, status update,
  archive/restore, delete behind `ConfirmationDialog`.
- Shipment Timeline screen: ordered `TrackingEvent` list (status, occurred/detected time,
  source attribution), manual Update Status entry point.
- Archive/restore/history entry points with correct list-visibility rules.

Acceptance: states Loading/Content/Empty/Error per screen; destructive actions confirmed;
critical flows 1–3 of `docs/qa/SCREEN_ACCEPTANCE.md` pass.

## UI-4 — Home + search + COD

**Objective:** dashboard answering arriving-soon / pending / COD-to-prepare in one glance.

Deliverables:
- Home: top bar (Ordra + settings), `SummaryMetricCard`s (Active, Arriving Soon, COD to
  Prepare), sections Today/Upcoming, Attention Needed, Recent Orders, Add/Scan always reachable,
  first-run empty state guiding Add Order or Scan.
- Search/Filters: query across product/seller/tracking/platform/notes; filters platform, status,
  payment/COD, date, category; sort newest/oldest/delivery soonest/highest/lowest; active filter
  count + clear-all.
- COD Overview: Today, next 3 days, all pending COD lists; never imply exact arrival from an
  estimated ETA.
- All metrics arrive pre-computed from use cases (`docs/contracts/ANALYTICS_CONTRACT.md`) —
  UI does not aggregate.

Acceptance: one-glance readability on small + large font; COD totals use minor-unit math
upstream; offline = normal state (no error).

## UI-5 — Calendar + reminders

**Objective:** delivery calendar and local reminder configuration.

Deliverables:
- Calendar: month view (week/list where feasible), day indicators, day sheet listing expected
  orders, COD total per selected day, exact vs estimated date distinction.
- Reminders: per-order settings + global defaults (same day / 1 day / 2 days / custom);
  notification permission explainer (`PermissionExplainer`) + channel guidance; local-only copy
  (`docs/contracts/REMINDERS_NOTIFICATION_CONTRACT.md`).
- Reminder/offline states: permission off/on, scheduling disabled states handled gracefully.

Acceptance: calendar correct across month boundaries/timezones policy; permission flow never
blocks core use; states covered in `docs/qa/SCREEN_COVERAGE_MATRIX.md`.

## UI-6 — Smart Import

**Objective:** screenshot → OCR → optional Groq → review → save, all reviewable.

Deliverables per `docs/contracts/SMART_IMPORT_CONTRACT.md`:
- Entry: select screenshot/image, optionally receive shared image; privacy note (OCR local,
  Groq optional); recent draft only if locally stored.
- Processing: real stages — Preparing image → Reading text → Interpreting (online only) →
  Preparing review; cancel where safe; offline skips Groq gracefully; no fake progress.
- Review: editable detected fields, `ConfidenceField` emphasis on uncertain/missing values only,
  platform guess shown separately if uncertain, duplicate warning surface, save goes through the
  same `OrderDraft` validation as manual entry.
- Failure surfaces: no text, unreadable image, no network, timeout, malformed response — all
  friendly per `docs/contracts/ERROR_VALIDATION_CONTRACT.md`.

Acceptance: full offline path works (OCR + manual review); Groq failure/offline states shown;
critical flows 4–5 of `docs/qa/SCREEN_ACCEPTANCE.md` pass.

## UI-7 — Smart Parcel Status assistance

**Objective:** share-to-Ordra UX and optional notification-assistance surfaces for
shipment status detection (`docs/contracts/TRACKING_CONTRACT.md`).

Deliverables:
- Share intent receiving UX: lands in the safe import flow, returns to Ordra normally.
- Notification Assistance: permission explainer first (why, privacy note, continue/not-now);
  detected-update `TrackingSuggestionCard`s show source, matched order/shipment, proposed
  `ShipmentStatus`, confidence/reason, Confirm/Ignore; detection history where useful.
- Status wording always shows source + freshness ("Detected 8:42 AM · Shopee notification");
  never "live"/"real-time" tracking copy.
- Settings controls: marketplace notification detection, automatic high-confidence updates,
  status-change notifications, per-marketplace toggles.

Acceptance: permission optional and revocable path explained; no silent shipment mutation
path exists in UI; flow 10 of `docs/qa/SCREEN_ACCEPTANCE.md` passes.

## UI-8 — Insights + advanced order lifecycle

**Objective:** local analytics plus returns/refunds, warranty, receipts, history.

Deliverables:
- Insights: monthly/weekly spend, platform/category breakdown, order count, average order
  value, pending/delivered value, outstanding COD; local-only note; empty state explains data
  appears after orders exist; textual summaries for charts (`docs/uiux/ACCESSIBILITY.md`).
- History: delivered/cancelled/returned/refunded with search/filter — a filtered experience,
  not a duplicated Orders implementation.
- Return/Refund records: status, reason, dates, amount, notes, attachments, source-order link.
- Warranty: coverage dates, provider, notes, proof attachment, expiry reminder entry.
- Receipts: local attachment management, preview metadata, privacy note, no auto-upload.

Acceptance: currency-grouped totals only (no mixed-currency sums); all records linked to their
order; states per coverage matrix.

## UI-9 — Settings + backup + about

**Objective:** complete settings, local backup/restore/export UX, about page.

Deliverables:
- Settings: appearance (system/light/dark), currency (PHP default), default platform/payment,
  reminder defaults, Smart Import/Groq configuration status, notification assistance toggle,
  backup/export entry, privacy/data controls, About. No account/cloud-sync sections.
- Backup/Restore/Export cards: create backup, restore (preview manifest metadata + explicit
  replace/merge warning per `docs/contracts/BACKUP_RESTORE_CONTRACT.md`), export CSV/JSON via
  Android picker; explicit success/failure feedback.
- About: name, tagline, version, local-first statement, privacy summary, licenses.

Acceptance: theme change applies immediately; restore preview mandatory before destructive
restore; flows 8–9 of `docs/qa/SCREEN_ACCEPTANCE.md` pass.

## UI-10 — Polish & regression

**Objective:** production-grade consistency pass across everything.

Deliverables:
- Accessibility sweep per `docs/uiux/ACCESSIBILITY.md`: 48dp targets, font scaling without
  clipped prices/statuses, semantics/TalkBack labels, decorative icons silent, focus order,
  reduced-motion respect, chart text summaries.
- Motion sweep per `docs/uiux/MOTION_AND_FEEDBACK.md`: short functional transitions only.
- Light/dark, rotation/process-death state restoration, empty/error/offline/permission states
  on every relevant screen, navigation regression, performance pass.
- `mockup/` synced to final UI.
- Update `docs/qa/SCREEN_COVERAGE_MATRIX.md` to PASS/BLOCKED with evidence for all 28 rows;
  run `prompts/UI_AUDIT_PROMPT.md` end-to-end audit and repair findings.

## Screen coverage table (target state per `docs/qa/SCREEN_COVERAGE_MATRIX.md`)

| Screen | Content | Empty | Error | Offline | Permission | Dark | Large font | A11y | Nav |
|---|---|---|---|---|---|---|---|---|---|
| Splash | ✓ | n/a | n/a | n/a | n/a | ✓ | ✓ | ✓ | ✓ |
| Onboarding | ✓ | n/a | n/a | n/a | n/a | ✓ | ✓ | ✓ | ✓ |
| Home | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Orders | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Add Order | ✓ | n/a | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Edit Order | ✓ | n/a | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Order Review | ✓ | n/a | ✓ | n/a | n/a | ✓ | ✓ | ✓ | ✓ |
| Order Detail | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Shipment Timeline | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Search/Filters/Sort | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Archive/History | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| COD Overview | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Calendar | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Reminders | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Smart Import Entry | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Smart Import Processing | ✓ | n/a | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Smart Import Review | ✓ | n/a | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Notification Assistance Permission | ✓ | n/a | n/a | n/a | ✓ | ✓ | ✓ | ✓ | ✓ |
| Status Suggestion Review | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Insights | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Return/Refund | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Warranty | ✓ | ✓ | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| Receipts | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Backup | ✓ | n/a | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Restore | ✓ | n/a | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Export | ✓ | n/a | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Settings | ✓ | n/a | ✓ | ✓ | n/a | ✓ | ✓ | ✓ | ✓ |
| About | ✓ | n/a | n/a | n/a | n/a | ✓ | ✓ | ✓ | ✓ |

## Cross-track mapping

| UI phase | Master phase (`docs/plan/MASTER_PLAN.md`) | Frontend phase (`docs/plan/FRONTEND_PHASES.md`) | Contract phase (`docs/plan/MEGAPLAN_INTEGRATION.md`) |
|---|---|---|---|
| UI-0 | 0 | F0 | I0/I1 |
| UI-1 | 1A | F0–F1 | I1 |
| UI-2 | 1A–1B | F1 | I1 |
| UI-3 | 1B–1C | F1 | I1 |
| UI-4 | 1C–1D | F1–F2 | I1/I2 |
| UI-5 | 2 | F3 | I3 |
| UI-6 | 3–4 | F4 | I4/I5 |
| UI-7 | 5 | F5 | I6 |
| UI-8 | 6–7 | F6–F7 | I2/I7 |
| UI-9 | 1E, 8 | F2, F8 | I8 |
| UI-10 | 9 | F9 | I9 |

## Known conflicts & open decisions

- **Navigation**: `docs/NAVIGATION.md` (Home|Orders|Scan|Calendar|More) vs
  `docs/uiux/NAVIGATION_UX.md` (Home|Orders|Calendar|Insights + central Add/Scan). Treat the
  UIUX pack as canonical pending owner confirmation.
- **Design system docs**: `design/DESIGN_SYSTEM.md` (Frontend pack) vs
  `docs/uiux/DESIGN_SYSTEM.md` (UIUX pack) — UIUX is canonical; the design/ file carries a
  pointer note.
- **Screen inventory vs specs**: `docs/SCREEN_INVENTORY.md` is the older full inventory;
  `docs/uiux/SCREEN_SPECS.md` is the canonical per-screen spec. Where they diverge
  (e.g. Insights tab, Add/Scan sheet), follow the UIUX pack.
- Do not "resolve" these by silently rewriting — record decision in this file when owner rules.
