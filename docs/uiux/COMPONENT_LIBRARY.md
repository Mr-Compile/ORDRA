# ORDRA Component Library

Canonical reusable Compose components. Build once in `core/ui/` (or `core/ui/components/`),
reuse everywhere — variants over per-screen duplicates. The editor's current private helpers
(`FormSection`, `MoneyField`, `EnumDropdown`, `DateField`, `FieldError`) get promoted here
during UI-2 rather than re-implemented per screen.

## Chrome & navigation
- OrdraTopAppBar: title, optional back, search/settings actions.
- OrdraBottomBar: Home, Orders, Calendar, Insights + central emphasized Add/Scan affordance;
  restrained `GlassSurface` treatment allowed.
- OrdraNavRail: NavigationRail/sidebar variant for medium+ window sizes.
- SectionHeader: section title + optional trailing action/count.

## Actions & inputs
- AppButton family: PrimaryButton / SecondaryButton / TextButton / DestructiveButton —
  states: default, hover(ripple), focus, pressed, selected, disabled, loading.
- AppIconButton: icon-only actions with mandatory contentDescription.
- AppInput / LabeledField / MoneyField / DateRangeField: labels, helper text, error state,
  required marker.
- EnumDropdown -> promoted to `AppDropdown` (select/picker fields).
- SearchBar: query field with clear action; debounced; shows active state.
- FilterChip / FilterChipRow / SortSheet.

## Badges & chips
- StatusChip: canonical status display = icon + text + semantic color token (see
  `docs/uiux/DESIGN_SYSTEM.md` → status system). Tonal container, never full-card fill.
  `ShipmentStatusChip` keeps its distinct shipment mapping.
- PlatformBadge (marketplace/source): small tonal badge, platform label; subdued brand tint,
  never dominant.
- CourierBadge: neutral outlined chip with courier name.
- CodChip / PaidChip / PaymentStatusChip.
- ConfidenceField: high/medium/low extraction confidence with review emphasis.

## Parcel & order cards
`ParcelCard` is the flagship component — one shared primitive, five variants, never
duplicated implementations:
- compact — list rows (marketplace badge, name, status chip, ETA line)
- standard — Orders/Search lists (badge, name, courier · tracking no., status chip,
  latest event line, primary action)
- featured — Home hero for the most important current shipment (largest card, tracking
  progress, ETA emphasis)
- delivered — completed state (muted emphasis, delivered date, no progress rail needed)
- problem — delayed/failed/attention state (warning accent, explicit reason + action)

Card content hierarchy (top to bottom): PlatformBadge -> product/order name ->
CourierBadge + tracking number -> StatusChip -> TrackingProgress (compact horizontal) ->
latest event -> estimated delivery -> primary action (Track/Details).
`OrderCard` composes the same primitive for order-first contexts; `UpcomingParcelCard`
prioritizes ETA + COD; `ShipmentCard` is the detail-screen parcel summary (courier,
tracking number + copy, status, detected-at + source attribution).

## Tracking
- TrackingProgress: reusable stage indicator — Ordered -> Preparing -> Shipped ->
  In Transit -> Out for Delivery -> Delivered. Horizontal compact mode inside ParcelCard;
  vertical mode on tracking detail. States per stage: completed / current / upcoming /
  problem-exception.
- ShipmentTimelineItem / TrackingEventRow: status title, occurred/detected time, location,
  description, source label ("Shopee notification", "Screenshot scan", "Manual"). Newest
  event visually distinct from history.
- TrackingSuggestionCard: matched order/shipment, proposed status, confidence, reason,
  Confirm/Ignore.
- TrackExternallyAction: opens the courier's official tracking page; never scrapes.

## Dashboard (Bento)
- BentoGrid + BentoCard: span-sized cards for Home/Insights only. Largest span reserved for
  the most important current shipment. Suggested set: Active Parcels, Arriving Today,
  In Transit, Out for Delivery, Delivered, Delayed/Attention, COD to Prepare, Recent Orders,
  Quick Track, Add Parcel, Delivery Alerts.
- SummaryMetricCard / StatCard: single metric (Active, Arriving Soon, COD, monthly spend).
- InsightCard: local analytics tiles.
- Bento stays off forms, tracking detail, settings and other information-heavy screens.

## States
- EmptyState: icon/illustration + title + guidance + one primary action. Required variants:
  no parcels, no active deliveries, nothing delivered yet, no search results,
  no notifications/detections, no tracking events, tracking unavailable, offline.
- ErrorState: friendly taxonomy-mapped message + recovery action.
- OfflineBanner: non-blocking; local functions remain available.
- SkeletonCard / LoadingSkeleton: shaped placeholders matching real layouts; no full-screen
  spinner unless a real blocking operation exists.

## Overlays & feedback
- BottomSheet (Add/Scan sheet, sort, pickers), ConfirmationDialog/Modal for destructive
  actions, Snackbar/Toast for reversible feedback.
- PermissionExplainer: reason, privacy note, continue/not-now.
- Offline/AI-unavailable surfaces reuse EmptyState/ErrorState patterns.

## Import / post-purchase
- ProductImagePicker, ReceiptAttachmentCard, SmartImportResultCard.
- TimelineItem (order history — shipment timelines use ShipmentTimelineItem).
- OrderItemRow, PriceBreakdown, AttachmentTile (from Frontend pack naming — fold into the
  canonical set, don't fork).

## Rules
Every component: light+dark themes, large-font tolerance, loading/disabled/error variants
where applicable, 48dp touch targets, TalkBack semantics, Compose previews where practical.
Component states to support: default, hover, focus, pressed, selected, disabled, loading,
error, success.

## ORDRA AI companion (implemented — `core/ui/ai/`)
- `CompanionState` enum: IDLE, GREETING, HAPPY, THINKING, WORKING, SUCCESS, DELIVERY,
  ATTENTION, WARNING, EMPTY, ERROR, OFFLINE. All states render today; states must only ever
  be driven by real app state (never fake intelligence to change expressions).
- `CompanionVariant`: AVATAR 40dp, COMPACT 56dp, EMPTY_STATE 88dp, BANNER 104dp,
  ONBOARDING 120dp.
- `CompanionAvatar(state, variant, size, painter?, animated)`: procedural 2D Ordi (rounded
  parcel body, brand indigo→violet gradient, signal antenna, expressive eyes/mouth, one
  state accessory). Passing a `Painter` replaces the built-in art — final SVG/PNG/Lottie
  assets can land later without touching call sites. Asset chain: painter → procedural
  art → callers may still degrade to the Ordra icon / text-only surface.
- `OrdraAiBanner(data, onViewParcels, onAskAi?, onAddOrder?)`: Home glass banner.
  `CompanionBannerData`/`CompanionBannerKind` + pure `resolveCompanionBanner()` own the
  message priority; `onAskAi` is nullable so the CTA only exists when a real AI surface
  is wired (Phase 4+).
- Reuse rule: companion visuals go through `CompanionAvatar`; do not draw a second mascot.
- Used in: Home banner + Home empty state (EMPTY_STATE variant), Orders empty state,
  Onboarding heroes (ONBOARDING), About screen (COMPACT).
