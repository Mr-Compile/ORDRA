# Complete Screen Inventory

> Note: original full inventory. Canonical per-screen specs live in
> `docs/uiux/SCREEN_SPECS.md` (v1.1); where they diverge — e.g. bottom-nav destinations,
> Add/Scan sheet — follow the UIUX pack. Screen audit verdicts:
> `docs/plan/MEGAPLAN_UIUX.md` → Screen audit.

## Entry
- Splash: Ordra branding, fast handoff.
- First-run Onboarding: local-only promise, smart import, reminders, privacy; skip/continue.

## Main shell
Bottom navigation: Home, Orders, Scan, Calendar, More.
Scan is a primary action and may be visually emphasized.

## Home
- Greeting/header and quick Add Order.
- Active orders count.
- Arriving today/soon.
- Outstanding COD total.
- Upcoming parcel cards.
- Recent orders.
- Smart Import shortcut.
- Empty state for first order.

## Orders
- All/Active/Delivered/Archived tabs or filter chips.
- Search.
- Platform/status/payment/category filters.
- Sort: newest, oldest, delivery soonest, highest/lowest total.
- Order cards with image, title, platform, total, payment, ETA, status.
- Multi-select optional later.

## Add/Edit Order
Step-based or sectioned form:
- Platform/custom store.
- Seller/order reference.
- One or multiple order items.
- Product image, name, variant, quantity.
- Item price, shipping, discount/voucher, computed total with manual override rules.
- Payment method/status and COD.
- Shipments: one or more parcels — courier/custom courier, tracking number, manual shipment status.
- Order date, ETA start/end, delivered date.
- Category/tags.
- Notes.
- Review and Save.
Required minimum: name/order label, platform, total.

## Order Detail
- Hero summary and status chip.
- Items and pricing breakdown.
- Payment/COD.
- Delivery ETA.
- Shipment section (one per parcel): courier, tracking number, shipment status chip, detected-at + source attribution. Actions: View Timeline, Update Status, Track Externally (official courier page, never scraped).
- Notes/category/tags.
- Attachments/receipt.
- Actions: edit, change status, mark delivered, archive, restore, delete.
- Return/refund/warranty entry points when relevant.

## Shipment Timeline
- Ordered tracking events: status, occurred/detected time, source attribution (Shopee notification / Screenshot scan / Manual).
- Manual Update Status entry point.

## Search & Filter
- Query suggestions/recent searches optional.
- Platform, status, payment, COD, category, date range.
- Clear all/apply.

## Scan / Smart Import
- Choose screenshot/image.
- Permission/photo picker state.
- OCR processing.
- Detected platform.
- Extracted fields and confidence indicators (order fields + courier/tracking/shipment status/events).
- Groq smart-analysis state when online.
- Offline fallback.
- Error/retry.
- Duplicate warning.
- Review Import screen with editable fields.
- Save as Order (shipment detection on existing orders produces a reviewable suggestion).

## Calendar
- Month/week/list modes as feasible.
- Delivery markers.
- Day sheet/list of expected parcels.
- COD total for selected date.

## Reminders
- Reminder overview.
- Per-order reminder settings.
- Global defaults: same day, 1 day before, 2 days before, custom.
- Notification permission/channel guidance.

## Spending
- Month selector.
- Total spending, pending value, delivered value, outstanding COD.
- Platform/category breakdown.
- Order count and average order value.
- Local-only analytics note.

## History
- Delivered/cancelled/returned/refunded history.
- Search/filter and date grouping.

## Returns & Refunds
- Create return/refund record.
- Reason, requested date, deadline, amount, status, notes.
- Related order link.

## Warranty
- Warranty start/end, seller/manufacturer, notes, receipt attachment.
- Expiry reminder.

## Receipts / Attachments
- Local image/document list.
- Add/view/remove attachment.
- Privacy warning for sensitive screenshots.

## Backup & Export
- Create local backup.
- Restore backup with validation/confirmation.
- Export CSV/JSON where supported.
- Explain local storage destination using Android picker.

## Smart Parcel Status (Notification Assistance)
- Explain optional Notification Access before Android settings.
- Supported source status across marketplace (Shopee, Lazada, TikTok Shop…),
  courier (SPX, J&T, Flash, Ninja Van, LBC…) and messaging/rider notifications.
- Separate rider/message-detection consent with its own privacy explainer.
- Detected shipment update suggestions with source, matched order/shipment, proposed status, confidence.
- Confirm/ignore update; ambiguous multi-match review lets the user pick the parcel; high-confidence auto-apply only when user-enabled.
- Detection history where useful (outcomes only, no raw content).
- Manual status fallback always available.

## Settings
- Theme: system/light/dark.
- Currency (PHP default).
- Default platform/payment.
- Reminder defaults.
- Smart Import settings/Groq availability.
- Smart Parcel Detection: marketplace + courier + rider/SMS notification detection (separate toggles), automatic high-confidence updates, ask-before-ambiguous, AI-assisted detection, status-change notifications.
- Privacy/data controls.
- Backup/export.
- About.

## About
- Ordra name/tagline/meaning.
- Version/build.
- Local-only/offline-first statement.
- Privacy summary.
- Open-source licenses.
