# Repository & Use Case Contract

## OrderRepository
Conceptual responsibilities:
- observeOrder(id)
- observeOrders(filter, sort, query)
- createOrder(draft/domain input)
- updateOrder(...)
- archiveOrder(id)
- restoreOrder(id)
- deleteOrderPermanently(id)
- updateStatus(id, status)
- add/update/remove items
- expose aggregate queries needed by dashboard/COD/analytics

## ShipmentRepository / tracking boundary
Dedicated boundary for parcels and timelines — do not grow `OrderRepository` into a God repository.

Conceptual operations:
- observeShipments(orderId)
- observeShipment(shipmentId)
- createShipment(input)
- updateShipment(input)
- updateShipmentStatus(shipmentId, status, source, confidence)
- deleteShipment(id)
- addTrackingEvent(event)
- observeTrackingTimeline(shipmentId)
- findShipmentByTrackingNumber(trackingNumber)
- matchDetectionToShipment(extracted detection signals)
- applyTrackingSuggestion(suggestion) / dismissTrackingSuggestion(suggestionId)
- resolveAmbiguousMatch(detectionId, chosenShipmentId) — user-picked parcel from the
  ambiguous-match review applies the suggestion to that shipment only
- observeDetectionHistory() / recordDetectionOutcome(record) — Phase 5A detection
  history (DetectionRecord); structured fields only, no raw notification bodies

Manual status updates go through `updateShipmentStatus` with `source = MANUAL`, which
also writes the corresponding `TrackingEvent`.

## Supporting repositories
Use focused repositories where justified:
- ReminderRepository
- ReceiptRepository
- WarrantyRepository
- ReturnRefundRepository
- SettingsRepository
- BackupRepository
- SmartImportRepository/Service boundary

Avoid a single God repository.

## Use cases
Use cases own application rules, validation orchestration, and multi-repository coordination.

Examples:
- CreateOrderUseCase
- UpdateOrderUseCase
- ArchiveOrderUseCase
- GetOrderDetailsUseCase
- ObserveOrdersUseCase
- UpdateOrderStatusUseCase
- CalculateCodSummaryUseCase
- ScheduleDeliveryReminderUseCase
- AnalyzeScreenshotUseCase
- ConfirmSmartImportUseCase
- CreateReturnUseCase
- RestoreBackupUseCase
- CreateShipmentUseCase
- UpdateShipmentStatusUseCase
- AddTrackingEventUseCase
- NormalizeDetectedStatusUseCase
- MatchDetectionToShipmentUseCase
- ApplyTrackingSuggestionUseCase
- ProcessDetectedNotificationUseCase (Phase 5A: source filter → relevance → parse →
  match → confidence → suggestion/event, incl. dedupe + transition validation)
- InterpretAmbiguousNotificationUseCase (Phase 5A: optional ORDRA AI fallback when
  deterministic parsing is insufficient and AI-assisted detection is enabled)
- NotifyShipmentStatusChangeUseCase

## Return types
Prefer explicit success/failure domain results for operations that can fail. Do not expose SQLite, Retrofit, ML Kit, or Groq exceptions to ViewModels.

## Flow
Use Flow for observable local data. One-shot operations may be suspend functions.
