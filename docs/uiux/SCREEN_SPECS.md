# ORDRA Screen Specifications

Per-screen canonical specs. Layout follows the visual formula: Minimal/Material everywhere,
Bento only on dashboard/summary surfaces, glass/gradient only on chrome and brand moments.
Every relevant screen covers: Loading, Content, Empty, Error, Offline, Permission Required,
Disabled/Unavailable — only states that make sense for that screen; never dead-end states.

## Splash
Ordra mark + wordmark on a system-aware light/dark background; subtle brand gradient allowed.
Fast transition; no artificial delay.

## Onboarding
3 concise pages maximum: Organize orders; Smart screenshot import; Local-first
reminders/privacy. Gradient/aurora hero accents allowed. Skip available. No account creation.

## Home
Bento dashboard built from `BentoGrid` + `BentoCard`, not a uniform list.
- Top bar with Ordra/title and settings.
- `OrdraAiBanner` directly below the top bar (the one allowed glass/AI surface on Home):
  ORDRA AI eyebrow, time-of-day greeting, highest-priority context message, optional
  secondary line, Ordi (`CompanionAvatar`, BANNER variant) on the trailing side, View
  Parcels CTA (+ Add Order when empty). "Ask ORDRA" renders only when an AI destination
  exists. Priority: attention > arriving today > delivered recently > active > empty —
  see `resolveCompanionBanner` in `core/ui/ai/OrdraAiBanner.kt`.
- Featured `ParcelCard` (featured variant) for the most important current shipment — largest
  visual emphasis, compact `TrackingProgress`, ETA.
- Bento metrics: `SummaryMetricCard`s for Active Parcels, Arriving Today/Soon, COD to
  Prepare; optional In Transit / Out for Delivery / Delivered / Delayed tiles.
- Sections: Today/Upcoming, Attention Needed (delayed/failed/problem cards), Recent Orders.
- Quick actions: Add Parcel / Scan reachable via central Add/Scan affordance.
- Empty state guides first order or screenshot import.

## Orders
SearchBar at top. FilterChips for status/platform/payment/COD + SortSheet. Scrollable
`ParcelCard`/`OrderCard` standard-variant list. Archive access. Distinct
empty vs filter-no-result states. On expanded window sizes a list+detail split view is
allowed.

## Add / Edit Order
Progressive `FormSection`s: Platform; Order Items; Pricing; Payment; Delivery (shipments:
courier, tracking number, shipment status); Attachments; Notes. Minimum required input:
recognizable order/product name, platform, total (unless draft behavior is explicitly
enabled). Sticky Save/Review action. Preserve entered data across navigation/config changes.
Clean Minimal+Material layout — no Bento here.

## Order Detail
Hero: title/platform/status/total. Sections: ETA, **Shipment** (per parcel: courier,
tracking number + copy, `ShipmentStatusChip`, detected-at + source attribution, actions View
Timeline / Update Status / Track Externally), items/pricing breakdown, payment/COD, notes,
attachments/receipt, warranty, return/refund. Actions: Edit, status update, archive;
destructive delete behind `ConfirmationDialog`.

## Tracking Detail (Shipment Timeline)
Answers "where is my parcel right now?" within the first viewport. Hierarchy:
1. Back/navigation + parcel identity
2. Current `ShipmentStatusChip` (icon + text + color) — the hero element
3. Estimated delivery
4. Product/order summary + `PlatformBadge` + `CourierBadge`
5. `TrackingProgress` vertical stepper (completed/current/upcoming/problem)
6. Detailed `ShipmentTimeline` — newest event visually distinct; each event shows status,
   date, time, location, description + source attribution
7. Courier information
8. Tracking number + copy action
9. Additional parcel info; secondary actions (Update Status, Track Externally)
Clean Minimal+Material layout — no Bento cards on this screen. Empty state explains statuses
appear as updates are detected or added; manual update always available. Never uses "live" /
"real-time" wording; shows source + freshness.

## Search / Filters
Search product, seller, tracking number, platform, courier, notes. FilterChips: All, Active,
In Transit, Out for Delivery, Delivered, Delayed, plus platform/courier/payment/COD/date,
category. Sort: newest, oldest, delivery soonest, highest/lowest price. Active filter count +
clear-all. Progressive disclosure — don't show every filter at once.

## COD Overview
Today, next 3 days, all pending COD. List associated parcels. Do not imply exact arrival when
ETA is only estimated.

## Calendar
Month/week-friendly delivery view. Days show order indicators; selecting a day lists expected
orders. Clear distinction between exact and estimated dates.

## Reminders
Per-order and default reminder settings. Delivery-day, 1 day before, 2 days before, custom
where supported. Explain local notifications and permission state.

## Activity / Smart Parcel Status (Notification Assistance)
Detection/activity feed + permission explainer first — why notification access helps, privacy
note, continue/not-now — before linking to Android settings. Detected marketplace update
`TrackingSuggestionCard`s show source, matched order/shipment, proposed ShipmentStatus,
confidence/reason, Confirm/Ignore. Never present an uncertain match as fact; never label
status "live"/"real-time" — show detection source and freshness. Manual update path always
available.

## Smart Import Entry
Actions: select screenshot/image; optionally receive shared image. Privacy note: OCR is
local; Groq may be used for optional interpretation. Recent scan draft only if locally
stored.

## Smart Import Processing
Show actual stages: Preparing image -> Reading text -> Interpreting (online only) ->
Preparing review. Cancel where safe. Offline skips Groq gracefully. No fake progress.

## Smart Import Review
Editable detected fields with `ConfidenceField` emphasis on uncertain/missing values only.
Platform guess shown separately if uncertain; duplicate warning surface; save uses the same
OrderDraft validation as manual entry.

## Insights
Local-only analytics: monthly/weekly spend, platform/category breakdown, order count, average
order value, pending/delivered value, outstanding COD. Bento `InsightCard`/`StatCard` layout
allowed. Empty state explains insights appear after orders exist. Text summaries for charts.

## History
Delivered/cancelled/returned/refunded history with search/filter — a filtered Orders
experience, not a duplicated implementation.

## Return / Refund
Status, reason, dates, amount, notes, attachments. Link to source order. No assumption of
marketplace API synchronization.

## Warranty
Coverage dates, provider/seller, notes, proof/receipt attachment, expiration reminder.

## Receipts
Local attachment management. Preview metadata, add/remove, privacy note. Do not upload
automatically.

## Backup / Restore / Export
Separate cards for Create Backup, Restore Backup, Export CSV/compatible data. Before restore,
preview backup metadata and warn about replacement/merge behavior per
`docs/contracts/BACKUP_RESTORE_CONTRACT.md`. Explicit success/failure feedback.

## Settings
Appearance (system/light/dark), currency, default platform/payment, reminders, Smart
Import/Groq configuration, Smart Parcel Status (marketplace notification detection,
automatic high-confidence updates, status-change notifications, per-marketplace toggles),
backup/export, privacy, About. No account/cloud-sync sections.

## About
Ordra name, tagline, version, local-first description, privacy summary, licenses, project
information.
