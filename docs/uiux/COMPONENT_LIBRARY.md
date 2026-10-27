# ORDRA Component Library

Canonical reusable Compose components:

- OrdraTopAppBar: title, optional back, search/settings actions.
- OrdraBottomBar: Home, Orders, Calendar, Insights with central Add/Scan affordance.
- PrimaryButton / SecondaryButton / TextButton / DestructiveButton.
- OrderCard: image, title, platform, total/payment, ETA, status chip; compact and regular variants.
- UpcomingParcelCard: prioritizes ETA and COD.
- StatusChip: semantic status only. ShipmentStatusChip for parcel status (distinct mapping).
- ShipmentCard: courier, tracking number, shipment status, detected-at + source attribution.
- ShipmentTimelineItem: event status, occurred/detected time, source label; for Shipment Timeline.
- TrackingSuggestionCard: matched order/shipment, proposed status, confidence, Confirm/Ignore.
- CourierBadge: courier label without copying courier visual identity.
- TrackExternallyAction: opens the courier's official tracking page; never scrapes.
- PlatformBadge: platform label/icon without copying marketplace visual identity.
- CodChip / PaidChip / PaymentStatusChip.
- MoneyText: locale/currency-safe price display.
- SummaryMetricCard: Active, Arriving Soon, COD, monthly spend.
- SearchBar + FilterChipRow + SortSheet.
- EmptyState: icon/illustration, title, guidance, primary action.
- ErrorState: friendly message, retry/recovery action.
- OfflineBanner: non-blocking; explain Groq unavailable while local functions remain.
- LoadingSkeleton: only where waiting is real; avoid fake long loading.
- FormSection / LabeledField / MoneyField / DateRangeField / PlatformPicker / StatusPicker / PaymentPicker.
- ProductImagePicker and ReceiptAttachmentCard.
- ConfidenceField: high/medium/low extraction confidence with review emphasis.
- SmartImportResultCard: extracted field + confidence + correction state.
- PermissionExplainer: reason, privacy note, continue/not-now.
- ConfirmationDialog for destructive operations.
- Snackbar for reversible/non-critical feedback.
- TimelineItem for order status/history (shipment timelines use ShipmentTimelineItem).
- InsightCard for local analytics.

Rules: components must support light/dark theme, large font sizes, loading/disabled/error states, and Compose previews where practical.
