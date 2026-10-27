# Ordra UI/UX Megaplan

Exhaustive execution plan for the **ORDRA UI/UX Complete Pack v1.1**. One source of truth for every
UI phase, screen, component, state, and QA gate. Update the status table after each phase gate;
do not advance with open blockers.

## Official design direction (v1.1)

ORDRA uses a hybrid design system — **70% Minimalism + Material-inspired UI** (clean surfaces,
strong hierarchy, efficient whitespace, rounded components, subtle borders, restrained
shadows), **20% Bento grid** (Home/dashboards, delivery statistics, quick actions, alerts,
recent activity only), **10% restrained glassmorphism + gradient + aurora** (splash,
onboarding, empty-state illustration areas, floating nav/controls, modal overlays, selected
premium highlights). The app must feel like a polished consumer parcel app — modern, clean,
fast, trustworthy, organized, easy to scan — never a generic enterprise dashboard. No other
major UI styles without explicit UX justification.

UX priority order when goals conflict: usability > beauty; readability > branding;
performance > animation; Ordra's system > marketplace branding. Canonical tokens and rules:
`docs/uiux/UI_UX_BIBLE.md` + `docs/uiux/DESIGN_SYSTEM.md`.

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
| UI-0 | Design foundation | Phase 0 | F0 | IN PROGRESS | Theme/tokens exist in `core/ui/theme` and are used app-wide; DESIGN_SYSTEM.md extraction audit not yet run |
| UI-1 | Navigation shell | Phase 1A | F0/F1 | DONE | NavHost + routes shipped; spec conflict note may be stale — verify in polish pass |
| UI-2 | Core order components | Phase 1A/1B | F1 | DONE | OrderCard/StatusChip/form components in `core/ui` + orderedit |
| UI-3 | Order management screens | Phase 1B/1C | F1 | DONE | Add/Edit (review-before-save), Detail, archive/restore/delete |
| UI-4 | Home + search + COD | Phase 1C/1D | F1/F2 | DONE | Dashboard metrics, Orders screen, COD overview |
| UI-5 | Calendar + reminders | Phase 2 | F3 | DONE | Calendar + reminder chips + notification permission shipped (`32284ff`) |
| UI-6 | Smart Import | Phase 3/4 | F4 | DONE | Pick→OCR→review→save + Groq path shipped (`eddcf8b`,`141b15e`) |
| UI-7 | Smart Parcel Status assistance | Phase 5 | F5 | DONE | Listener opt-in, suggestion review + parcel pick (`2b0adf8`) |
| UI-8 | Insights + advanced lifecycle | Phase 6/7 | F6/F7 | DONE | Analytics + post-purchase sections (`a85a64b`,`2883923`) |
| UI-9 | Settings + backup + about | Phase 1E/8 | F2/F8 | DONE | Settings incl. backup/detections/AI toggles + About (`2069037`) |
| UI-10 | Polish & regression | Phase 9 | F9 | PENDING | Full-screen regression pass not yet run |

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

## Canonical component set (per `docs/uiux/COMPONENT_LIBRARY.md`, v1.1)

Implement once in `core/ui/` (or equivalent), reuse everywhere — variants over per-screen
duplicates:

- Chrome: `OrdraTopAppBar`, `OrdraBottomBar` (central emphasized Add/Scan; restrained
  `GlassSurface` allowed), `OrdraNavRail` (medium+ window sizes), `SectionHeader`.
- Actions/inputs: `AppButton` family (`PrimaryButton`, `SecondaryButton`, `TextButton`,
  `DestructiveButton`), `AppIconButton`, `AppInput`/`LabeledField`/`MoneyField`/
  `DateRangeField`, `AppDropdown`, `ConfirmationDialog`, `BottomSheet`, `Snackbar`/Toast
  conventions. Interactive components support default/hover/focus/pressed/selected/
  disabled/loading/error states.
- Badges/chips: `StatusChip` (icon + text + semantic color — never color alone),
  `ShipmentStatusChip`, `PlatformBadge` (marketplace/source), `CourierBadge` (neutral),
  `CodChip`, `PaidChip`, `PaymentStatusChip`, `ConfidenceField`, `FilterChip`, `MoneyText`.
- Parcel display: **`ParcelCard` — one shared primitive, five variants** (compact, standard,
  featured, delivered, problem) carrying PlatformBadge -> name -> courier + tracking number
  -> StatusChip -> TrackingProgress -> latest event -> ETA -> primary action. `OrderCard`
  and `UpcomingParcelCard` (ETA + COD first) compose the same primitive; no duplicated card
  implementations.
- Tracking: `TrackingProgress` (horizontal compact + vertical stepper; completed/current/
  upcoming/problem-exception states), `ShipmentCard` (courier, tracking number + copy,
  status, detected-at + source), `ShipmentTimelineItem` (status, date/time, location,
  description, source attribution; newest event emphasized), `TrackingSuggestionCard`
  (matched order, proposed status, confidence, Confirm/Ignore), `TrackExternallyAction`.
- Dashboard (Bento on dashboard/summary surfaces only): `BentoGrid`, `BentoCard`,
  `SummaryMetricCard`/`StatCard` (Active Parcels, Arriving Today, In Transit, Out for
  Delivery, Delivered, Delayed, COD to Prepare, monthly spend), `InsightCard`; the featured
  `ParcelCard` takes the largest span.
- Discovery: `SearchBar`, `FilterChipRow`, `SortSheet`.
- States: `EmptyState` (icon + title + guidance + one action), `ErrorState` (friendly copy +
  retry), `OfflineBanner` (non-blocking), `SkeletonCard`/`LoadingSkeleton` shaped like real
  content, `GlassSurface` (floating nav/controls, overlays, sheets only).
- Forms: `FormSection`, `PlatformPicker`, `StatusPicker`, `PaymentPicker` — promote the
  Add/Edit screen's private helpers (`FormSection`, `MoneyField`, `EnumDropdown`,
  `DateField`, `FieldError`) rather than re-implementing them.
- Import/assist/history: `ProductImagePicker`, `ReceiptAttachmentCard`,
  `SmartImportResultCard`, `PermissionExplainer`, `TimelineItem`, `OrderItemRow`,
  `PriceBreakdown`, `AttachmentTile`.

Every component: light+dark themes, large-font tolerance, loading/disabled/error variants where
applicable, Compose previews where practical, 48dp touch targets, semantics for TalkBack.

## UI-0 — Design foundation

**Objective:** one token-driven Material 3 theme; no scattered raw values.

Deliverables:
- Color roles exactly per `docs/uiux/DESIGN_SYSTEM.md` light/dark tables (verify existing
  `app/src/main/java/com/ordra/core/ui/theme/` — reconcile `Color.kt`/`Theme.kt`/`Type.kt`
  against canonical tokens; `design/DESIGN_SYSTEM.md` points here as canonical).
- Status→color mapping (Ordered=Indigo … Refunded=Teal) exposed as semantic tokens; status
  color on chip/icon/accent only, never full-card fill. Every status pairs icon + text +
  color (see DESIGN_SYSTEM.md status system).
- Typography: M3 type scale mapped Display→hero numbers, Headline→summaries, Title→cards,
  Body→content, Label→chips/metadata; respect system font scaling.
- Spacing tokens on 4dp grid (4/8/12/16/20/24/32); screen padding 16dp compact, 24dp wide.
- Shape tokens (radius): small 8dp (chips), medium 12dp (inputs/buttons), large 16dp
  (standard cards), xl 20–24dp (featured/Bento), M3 sheets.
- Elevation policy: tonal surfaces + subtle borders; reserved elevation for
  FAB/nav/sheets/dialogs.
- `GlassSurface` token: surface ~80–88% alpha + outline ~20% + optional cheap blur; floating
  nav/controls/overlays/sheets only; solid fallback required.
- Motion tokens: micro 100–150ms, standard 200–300ms, emphasis <=400ms; reduced-motion
  fallbacks.
- Breakpoint/adaptivity policy: Android window size classes (compact <600dp bottom nav +
  1-col; medium 600–840dp rail + up to 2-col Bento; expanded >840dp rail/sidebar +
  multi-column, list+detail split where useful). Android only — no desktop/web targets.
- Icon rules: Material Symbols Rounded for function icons; custom SVG only for Ordra mark
  (see `docs/specs/ASSET_MANIFEST.md`); no emoji as production icons.
- Theme preview harness: light/dark preview of tokens + core components.
- `branding/theme_tokens.json` + `mockup/styles.css` tokens stay in sync
  (docs/RULES.md mockup consistency).

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

**Objective:** build the reusable order/parcel-display and state components before the screens.

Deliverables:
- `ParcelCard` shared primitive + variants (compact, standard, featured, delivered, problem);
  `OrderCard` and `UpcomingParcelCard` (ETA + COD first) built from it.
- `StatusChip`/`ShipmentStatusChip` (icon + text + semantic color), `PlatformBadge`,
  `CourierBadge`, `CodChip`/`PaidChip`/`PaymentStatusChip`, `MoneyText`
  (locale/currency-safe, minor-unit input), `TimelineItem`, `SummaryMetricCard`/`StatCard`.
- `TrackingProgress` horizontal-compact + vertical modes.
- `SearchBar`, `FilterChip`/`FilterChipRow`, `SortSheet`, `SectionHeader`, `AppButton`
  family, `AppIconButton`, `AppInput`/`MoneyField`/`DateRangeField`/`AppDropdown` promoted
  from the editor's private helpers.
- `BentoGrid` + `BentoCard`, `GlassSurface` (restricted), `EmptyState`, `ErrorState`
  (friendly copy + retry), `OfflineBanner`, `SkeletonCard`/`LoadingSkeleton`.
- Money/date formatting utilities in the UI layer consuming domain values only.
- Compose previews: each component × {light, dark} × {normal, large font} where practical.

Acceptance: components consume domain/UiState models only (no Room entities, no DAO); chips use
semantic status mapping with icon + text (no color-only states); previews compile and render.

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
  courier, tracking number + copy, `ShipmentStatusChip`, detected-at + source attribution,
  actions View Timeline / Update Status / Track Externally; items/pricing, payment/COD,
  notes, attachments, warranty, return/refund entries; actions Edit, status update,
  archive/restore, delete behind `ConfirmationDialog`.
- Tracking Detail (Shipment Timeline): answers "where is my parcel now?" first — current
  `ShipmentStatusChip`, ETA, order + platform/courier badges, vertical `TrackingProgress`,
  then ordered `TrackingEvent` list (status, date/time, location, description, source
  attribution; newest event emphasized), courier info, tracking number + copy, manual
  Update Status entry point. Clean Minimal+Material layout — no Bento on this screen.
- Archive/restore/history entry points with correct list-visibility rules.

Acceptance: states Loading/Content/Empty/Error per screen; destructive actions confirmed;
critical flows 1–3 of `docs/qa/SCREEN_ACCEPTANCE.md` pass.

## UI-4 — Home + search + COD

**Objective:** dashboard answering arriving-soon / pending / COD-to-prepare in one glance.

Deliverables:
- Home: top bar (Ordra + settings), `BentoGrid` of `SummaryMetricCard`s (Active Parcels,
  Arriving Today/Soon, COD to Prepare, optional In Transit / Out for Delivery / Delayed),
  featured `ParcelCard` with largest span for the most important current shipment, sections
  Today/Upcoming, Attention Needed, Recent Orders, Add/Scan always reachable, first-run
  empty state guiding Add Order or Scan. `SkeletonCard`s mirror the Bento layout while
  loading.
- Search/Filters: query across product/seller/tracking number/platform/courier/notes;
  FilterChips (All, Active, In Transit, Out for Delivery, Delivered, Delayed + platform/
  courier/payment/COD/date/category); sort newest/oldest/delivery soonest/highest/lowest;
  active filter count + clear-all; progressive disclosure, not every filter at once.
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
  detected-update `TrackingSuggestionCard`s show source (marketplace / courier / delivery
  message), matched order/shipment, proposed `ShipmentStatus`, confidence/reason,
  Confirm/Ignore; detection history feed (`DetectionHistoryItem`, outcomes only).
- Ambiguous detections render `ParcelMatchReview`/`AmbiguousMatchCard` ("Possible Parcel
  Update" → candidate parcels → Select Parcel / Ignore); never an arbitrary pick.
- Rider/message detection has its own privacy explainer + Enable/Not Now before the
  settings toggle can be switched on.
- Status wording always shows source + freshness ("Detected 8:42 AM · Shopee notification",
  "Last detected update: 12 minutes ago"); never "live"/"real-time" tracking copy; never
  technical words (NotificationListenerService, Groq, regex).
- Settings controls (`NotificationSourceSettings`): marketplace notifications, courier
  notifications, rider/SMS detection (separate), automatic high-confidence updates,
  ask-before-ambiguous, AI-assisted detection (separate), status-change notifications,
  per-source toggles.

Acceptance: permission optional and revocable path explained; no silent shipment mutation
path exists in UI; ambiguous matches always reach review; flow 10 of
`docs/qa/SCREEN_ACCEPTANCE.md` passes.

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
  reduced-motion respect, chart text summaries, glass-surface readability, no color-only
  states.
- Motion sweep per `docs/uiux/MOTION_AND_FEEDBACK.md`: token-bound durations, reduced-motion
  fallbacks, no decorative delays.
- Adaptive/responsive pass: compact bottom nav → medium/expanded `OrdraNavRail`, Bento
  column counts per breakpoint, list+detail split on Orders where useful.
- Light/dark, rotation/process-death state restoration, empty/error/offline/permission states
  on every relevant screen, navigation regression, performance pass (low-end device budget:
  limited blur, no heavy shadows, no constant animation).
- Visual-formula consistency check: ~70/20/10 discipline — no glass on content-heavy
  surfaces, no Bento on forms/details, no gradients behind dense text.
- `mockup/` synced to final UI.
- Update `docs/qa/SCREEN_COVERAGE_MATRIX.md` to PASS/BLOCKED with evidence for all rows;
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

## Screen audit (v1.1 design direction)

KEEP = functionality stays; classifies the required visual treatment. No screen is removed
for style reasons; functionality drives scope.

| Screen | Verdict | Notes |
|---|---|---|
| Splash | IMPROVE | Add subtle brand gradient; keep fast handoff |
| Onboarding | IMPROVE | Gradient/aurora hero accents allowed |
| Home | REDESIGN | Bento dashboard + featured `ParcelCard`; replaces flat list |
| Orders | IMPROVE | SearchBar + FilterChips + standard `ParcelCard` list |
| Add/Edit Order | KEEP | Progressive `FormSection`s already match direction; promote helpers to `core/ui` |
| Order Review | KEEP | Same component set as Add/Edit |
| Order Detail | IMPROVE | Hero + shipment section hierarchy; marketplace vs courier split |
| Tracking Detail (Shipment Timeline) | REDESIGN | "Where is my parcel now?" hierarchy + `TrackingProgress` + emphasized newest event |
| Search/Filters/Sort | IMPROVE | FilterChips incl. status filters + courier/platform; progressive disclosure |
| Archive/History | KEEP | Filtered Orders experience; shares `ParcelCard` variants |
| COD Overview | KEEP | Metric + parcel lists; no Bento needed |
| Calendar | KEEP | Minimal+Material; no Bento |
| Reminders | KEEP | Settings-style layout |
| Smart Import Entry | IMPROVE | Aurora/gradient illustration area allowed |
| Smart Import Processing | KEEP | Real stage progress only |
| Smart Import Review | KEEP | ConfidenceField emphasis on uncertain fields |
| Notification Assistance Permission | KEEP | PermissionExplainer pattern |
| Status Suggestion Review | KEEP | TrackingSuggestionCard + Confirm/Ignore |
| Insights | IMPROVE | Bento `StatCard`/`InsightCard` layout allowed |
| Return/Refund | KEEP | Form + linked record views |
| Warranty | KEEP | Form + expiry reminder |
| Receipts | KEEP | Attachment grid/list |
| Backup / Restore / Export | KEEP | Card actions + preview + warnings |
| Settings | IMPROVE | Sectioned layout; theme picker; no account sections |
| About | KEEP | Brand surface — logo/tagline |
| "Scan" tab (old NAVIGATION.md) | MERGE | Already folded into central Add/Scan sheet |
| "More" tab (old NAVIGATION.md) | REMOVE | Superseded; Settings/secondary items live in app bar/Settings |
| Activity feed (spec suggestion) | MERGE | Detection/activity feed lives inside Smart Parcel Status surfaces; optional future destination |

## Design-direction migration checklist

Runs across the existing UI phases — do not redesign everything at once. Each item lands in
its mapped phase; unchecked items are open work, not new phases.

**Foundation → UI-0 (in progress)**
- [ ] Extract spacing (4-grid), radius (8/12/16/20–24), elevation, motion-duration and
  `GlassSurface` tokens into `core/ui/theme/` alongside colors
- [ ] Status→icon+color+label token table for `StatusChip`/`ShipmentStatusChip`
- [ ] Marketplace badge tints + neutral `CourierBadge` token
- [ ] Window-size-class policy + nav-pattern switch (bar vs rail)
- [ ] Sync new tokens into `branding/theme_tokens.json` and `mockup/styles.css`
  (mockup radius 10/14/20 currently drifts from 8/12/16–24)

**Core components → UI-2**
- [ ] `StatusChip`/`ShipmentStatusChip`: icon + text + semantic color, tonal container
- [ ] `ParcelCard` primitive + 5 variants; migrate `OrderRow` on Home
- [ ] `TrackingProgress` (compact + stepper), `ShipmentTimelineItem` with newest-event emphasis
- [ ] `PlatformBadge`/`CourierBadge`, `FilterChip`, `SearchBar`, `SectionHeader`
- [ ] `AppButton`/`AppIconButton`/`AppInput`/`AppDropdown` promoted from editor helpers
- [ ] `EmptyState`, `ErrorState`, `SkeletonCard`, `OfflineBanner`, `GlassSurface`

**Core parcel experience → UI-3/UI-4**
- [ ] Bento Home: featured `ParcelCard` + metric cards + sections; skeleton mirrors layout
- [ ] Orders: SearchBar + FilterChips + standard ParcelCard list
- [ ] Tracking Detail: status hero → ETA → progress → timeline hierarchy
- [ ] Order Detail: shipment section with courier/tracking copy + source attribution

**Supporting experience → UI-5..UI-9**
- [ ] Calendar, Reminders, Activity/notification-assistance, Insights (Bento allowed),
  Settings/Profile, Onboarding/Auth-free entry, Backup/Export
- [ ] Splash/onboarding gradient + aurora accents within the 10% budget

**Responsive + themes → UI-0/UI-10**
- [ ] Dark theme verified per screen (already a mandatory gate)
- [ ] Compact/medium/expanded layouts; NavRail; Bento columns per breakpoint; list+detail
  split on expanded
- [ ] No desktop/web targets (Android only per `docs/RULES.md`)

**Polish → UI-10**
- [ ] Microinteractions per `docs/uiux/MOTION_AND_FEEDBACK.md`; reduced-motion fallbacks
- [ ] Skeleton loading on dashboard/cards/orders/detail/timeline
- [ ] Accessibility sweep incl. glass readability and focus states
- [ ] Performance pass: limited blur, no heavy shadows, no nested cards, efficient lists
- [ ] Visual-formula consistency check (~70/20/10)

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
- **Theme tokens (v1.1, decided)**: the design-direction spec suggested
  #F7F8FA/#FFFFFF/#111827 (light) and #0D1117/#161B22/#F3F4F6 (dark) as *starting* tokens.
  Ordra keeps the existing palette (#F8F9FD/#FFFFFF/#171821; #101116/#18191F/#F5F5FA) —
  already implemented in `Color.kt`, `theme_tokens.json`, `mockup/` — same intent, no
  migration churn. Owner may still override; if so, update tokens + mockup in one change.
- **Navigation model (v1.1, decided)**: spec suggested Home/Orders/Track-Add/Activity/
  Profile. Canonical stays Home/Orders/Calendar/Insights + central Add/Scan + Settings from
  app bar; "Track/Add" = Add/Scan action, "Activity" = Smart Parcel Status detection feed,
  "Profile" = Settings. See `docs/uiux/NAVIGATION_UX.md`.
- **Status naming (v1.1, decided)**: spec statuses map onto canonical enums
  (`docs/uiux/DESIGN_SYSTEM.md` status system). "Delayed" and "Tracking Issue" are derived
  presentation states — no new domain enums in v1.
- **Desktop/web responsive (v1.1, decided)**: Android-only per `docs/RULES.md`; responsive
  scope = window size classes (compact/medium/expanded), not desktop targets.
- **Current code drift (v1.1 audit, in-flight Phase 1B)**: `Order.courier`/`trackingNumber`
  and `OrderStatus.OUT_FOR_DELIVERY` remain from the pre-tracking schema — already resolved
  in `MEGAPLAN_INTEGRATION.md` (migrate to Shipment when the shipments phase lands).
  Shared components are beginning to land (`core/ui/components/OrderCard.kt` + `StatusChip`,
  `feature/detail/`, `feature/timeline/`); the variant system (`ParcelCard` compact/standard/
  featured/delivered/problem), `TrackingProgress`, badges and icon+text status chips are the
  UI-2 scope. None of this is a new defect.
- **ORDRA AI companion (v1.2, done)**: `core/ui/ai/` ships `CompanionState`, `CompanionVariant`,
  `CompanionTokens`, procedural `CompanionAvatar` (Ordi) and `OrdraAiBanner`; Home shows the
  banner driven by real order/shipment aggregates (`resolveCompanionBanner`), Ordi appears in
  Home/Orders empty states, Onboarding heroes and About. "Ask ORDRA" is intentionally unwired —
  no AI destination exists until Phase 4; the banner's AI_UNAVAILABLE kind is reserved for that.
  Final artwork replaces the procedural painter slot later. See DESIGN_SYSTEM §AI tokens,
  COMPONENT_LIBRARY §ORDRA AI companion, BRANDING_UI §Ordi, MOTION_AND_FEEDBACK §Companion.
- Do not "resolve" these by silently rewriting — record decision in this file when owner rules.
