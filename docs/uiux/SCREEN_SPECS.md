# ORDRA Screen Specifications

## Splash
Ordra mark + wordmark. Fast transition; no artificial delay. System-aware light/dark background.

## Onboarding
3 concise pages maximum: Organize orders; Smart screenshot import; Local-first reminders/privacy. Skip available. No account creation.

## Home
Top bar with Ordra/title and settings. Summary metrics: Active, Arriving Soon, COD to Prepare. Sections: Today/Upcoming, Attention Needed, Recent Orders. Primary Add/Scan action always reachable. Empty state guides first order or screenshot import.

## Orders
Search at top. Filter chips for status/platform/payment. Sort action. Scrollable OrderCard list. Archive access. Empty/filter-no-result states distinct.

## Add / Edit Order
Progressive sections: Platform; Order Items; Pricing; Payment; Delivery; Attachments; Notes. Minimum required input: recognizable order/product name, platform, total (unless draft behavior is explicitly enabled). Sticky Save/Review action where appropriate. Preserve entered data across navigation/config changes.

## Order Detail
Hero: title/platform/status/total. Sections: ETA + courier/tracking, items/pricing breakdown, payment/COD, timeline/status, notes, attachments/receipt, warranty, return/refund. Actions: Edit, status update, archive; destructive delete behind confirmation.

## Search / Filters
Search product, seller, tracking, platform, notes. Filters: platform, status, payment/COD, date, category. Sort: newest, oldest, delivery soonest, highest/lowest price. Show active filter count and clear-all.

## COD Overview
Today, next 3 days, all pending COD. List associated parcels. Do not imply exact arrival when ETA is only estimated.

## Calendar
Month/week-friendly delivery view. Days show order indicators; selecting a day lists expected orders. Clear distinction between exact and estimated dates.

## Reminders
Per-order and default reminder settings. Delivery-day, 1 day before, 2 days before, custom where supported. Explain local notifications and permission state.

## Smart Import Entry
Actions: select screenshot/image; optionally receive shared image. Privacy note: OCR is local; Groq may be used for optional interpretation. Recent scan draft only if locally stored.

## Smart Import Processing
Show actual stages: Preparing image -> Reading text -> Interpreting (online only) -> Preparing review. Cancel where safe. Offline skips Groq gracefully.

## Smart Import Review
Editable detected fields with confidence emphasis. Highlight uncertain/missing values, not every field. Show source platform guess separately if uncertain. Save uses same OrderDraft validation as manual entry.

## Notification Assistance
Permission explainer first. Detected marketplace update cards show source, matched order, proposed change, confidence/reason, Confirm/Ignore. Never present uncertain match as fact.

## Insights
Local-only analytics: monthly/weekly spend, platform/category breakdown, order count, average order value, pending/delivered value, outstanding COD. Empty state explains insights appear after orders exist.

## History
Delivered/cancelled/returned/refunded history with search/filter. Avoid duplicating Orders implementation; this is a filtered experience.

## Return / Refund
Status, reason, dates, amount, notes, attachments. Link to source order. No assumption of marketplace API synchronization.

## Warranty
Coverage dates, provider/seller, notes, proof/receipt attachment, expiration reminder.

## Receipts
Local attachment management. Preview metadata, add/remove, privacy note. Do not upload automatically.

## Backup / Restore / Export
Separate cards for Create Backup, Restore Backup, Export CSV/compatible data. Before restore, preview backup metadata and warn about replacement/merge behavior according to data contract. Success/failure feedback must be explicit.

## Settings
Appearance, currency, default platform/payment, reminders, Smart Import/Groq configuration, notification assistance, backup/export, privacy, About. No account/cloud-sync sections.

## About
Ordra name, tagline, version, local-first description, privacy summary, licenses, project information.

## Required states for every relevant screen
Loading, Content, Empty, Error, Offline, Permission Required, Disabled/Unavailable. Only use states that make sense for that screen; never create dead-end states.
