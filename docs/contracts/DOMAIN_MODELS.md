# Canonical Domain Models

These are conceptual contracts. Exact Kotlin implementation may use data classes, value classes, sealed interfaces, and enums.

## Order
- id
- platform: Platform
- customPlatformName?
- sellerName?
- status: OrderStatus
- paymentMethod: PaymentMethod
- paymentStatus: PaymentStatus
- isCod
- currencyCode
- itemSubtotalMinor
- shippingFeeMinor
- discountMinor
- totalAmountMinor
- orderedDate?
- estimatedDeliveryStart?
- estimatedDeliveryEnd?
- deliveredAt?
- notes?
- productImageUri?
- archivedAt?
- createdAt
- updatedAt

Invariant: total values must be non-negative unless a specifically documented adjustment model permits otherwise. `isCod` must be consistent with payment semantics.

Courier and tracking number are NOT stored on Order. They live on `Shipment` so one order can carry multiple parcels. UI list/card surfaces may show the primary/latest shipment via a repository join; they never persist courier/tracking fields back onto Order.

## OrderItem
- id
- orderId
- name
- variant?
- quantity
- unitPriceMinor?
- lineTotalMinor?
- imageUri?
- notes?

## Shipment
First-class domain model for one physical parcel belonging to an order.
- id
- orderId
- courier: Courier?
- customCourierName?
- trackingNumber?
- shipmentStatus: ShipmentStatus
- statusSource: TrackingStatusSource
- statusConfidence: DetectionConfidence
- lastStatusAt?
- lastDetectedAt?
- createdAt
- updatedAt

One Order may have many Shipments even if the common UI creates exactly one. `statusSource`/`statusConfidence` describe how the current `shipmentStatus` was established. Manual updates always exist and record `statusSource = MANUAL`.

## TrackingEvent
Immutable timeline entry belonging to a Shipment (not directly to an Order).
- id
- shipmentId
- shipmentStatus
- title
- description?
- occurredAt
- detectedAt
- source: TrackingStatusSource
- confidence: DetectionConfidence
- rawStatus?
- sourceDetail?
- rawReference?

`rawStatus` is a short raw status classification label (e.g. the detected phrase's
normalized key), `sourceDetail` is a minimal provenance key such as the source
app/package identifier (e.g. `shopee`, `spx`, `sms`) — both are small structured
fields for notification-derived events. `rawReference` is an optional minimal
pointer (e.g. local screenshot URI), never the full raw notification/OCR text —
do not persist sensitive raw content.

## Platform
SHOPEE, TIKTOK_SHOP, LAZADA, TEMU, AMAZON, FACEBOOK, INSTAGRAM, OTHER

## Courier
SPX_EXPRESS, JNT_EXPRESS, NINJA_VAN, FLASH_EXPRESS, LBC, JRS_EXPRESS, GOGO_XPRESS, ENTREGO, TWO_GO, DHL, OTHER, UNKNOWN. `OTHER` requires `customCourierName`; `UNKNOWN` is for unidentifiable couriers. The set is extensible; add values only for couriers that are actually relevant to supported flows.

## OrderStatus
Overall purchase/order lifecycle only — not courier logistics states.
DRAFT, ORDERED, PROCESSING, PACKED, SHIPPED, DELIVERED, CANCELLED, RETURNED, REFUNDED

Parcel movement states such as OUT_FOR_DELIVERY live on `ShipmentStatus`, not here.

## ShipmentStatus
Physical parcel movement.
UNKNOWN, PREPARING, PICKED_UP, IN_TRANSIT, ARRIVED_AT_HUB, OUT_FOR_DELIVERY, DELIVERED, DELIVERY_FAILED, RETURNING, RETURNED

Raw status text from notifications/OCR/imports is normalized into this enum by the `StatusNormalizer` layer (`docs/contracts/TRACKING_CONTRACT.md`); raw strings never become a ShipmentStatus directly.

## TrackingStatusSource
Where a shipment status/event came from.
MANUAL, MARKETPLACE_NOTIFICATION, COURIER_APP_NOTIFICATION, MESSAGE_NOTIFICATION,
SCREENSHOT_OCR, SMART_IMPORT, SHARED_CONTENT, FUTURE_OFFICIAL_PROVIDER

Notification sources split by app category so settings, labels and priority stay
distinct: marketplace apps (Shopee/Lazada/TikTok Shop), courier apps (SPX, J&T,
Flash, Ninja Van, LBC…), and messaging-app notifications carrying rider/delivery
messages (MESSAGE_NOTIFICATION — notification-based only, never SMS inbox reads).
`FUTURE_OFFICIAL_PROVIDER` is an architectural extension point only — it does NOT
mean a courier API is implemented in v1.

## DetectionConfidence
LOW, MEDIUM, HIGH

Confidence decides behavior: HIGH may auto-apply when the user enabled automatic updates; MEDIUM requires explicit Confirm/Ignore; LOW never modifies the shipment and may appear as suggestion only; ambiguous multi-candidate detections always go to user review. Internal implementations may store a normalized 0.0–1.0 score — the exact scoring algorithm is fixed during implementation/testing, not in docs; the band above is the canonical contract level.

## TrackingSuggestion
A proposed shipment update produced by detection (marketplace notification, screenshot OCR, shared content) before it is confirmed or applied. Supersedes the former order-level `NotificationSuggestion`.
- id
- detectionSource: TrackingStatusSource
- candidateShipmentId?
- candidateShipmentIds? (populated for ambiguous multi-match review — the user
  picks the parcel; never auto-applied)
- candidateOrderId?
- proposedShipmentStatus?
- proposedTrackingNumber?
- confidence: DetectionConfidence
- explanation
- matchedSignals
- requiresConfirmation

Suggestions are transient: once confirmed/ignored they resolve into a `TrackingEvent` (or nothing); they are not the shipment timeline itself.

## DetectionRecord
Lightweight persisted detection-history entry (Phase 5A). Records what the
detection pipeline did with each candidate notification for transparency and the
detection-history UI — structured fields only, never raw notification bodies.
- id
- detectedAt
- source: TrackingStatusSource
- sourceDetail?
- outcome: DETECTED | MATCHED | NEEDS_REVIEW | IGNORED | DUPLICATE | REJECTED | FAILED
- candidateShipmentId?
- proposedShipmentStatus?
- confidence?
- reason?

## ExtractedSignals
Structured candidate signals produced by the notification/OCR parser before
matching (transient, not persisted raw):
- trackingNumber?
- marketplaceOrderId?
- courier?
- marketplace?
- codAmountMinor?
- orderAmountMinor?
- productHint?
- rawStatusPhrase?
- deliveryDateHint?
- riderIndicator
- locationHint?

## PaymentMethod
COD, EWALLET, CARD, BANK_TRANSFER, CASH, OTHER, UNKNOWN

## PaymentStatus
UNPAID, PARTIALLY_PAID, PAID, REFUNDED, UNKNOWN

## Category
id, name, createdAt, updatedAt

## Reminder
id, orderId?, type, scheduledAt, enabled, notificationId?, createdAt

## Receipt
id, orderId, localUri, displayName?, mimeType?, createdAt

## Warranty
id, orderId, title?, startDate?, endDate?, notes?, reminderEnabled

## ReturnRecord
id, orderId, status, requestedAt?, deadline?, reason?, notes?

## RefundRecord
id, orderId, amountMinor, currencyCode, status, requestedAt?, completedAt?, notes?

## OrderDraft
Temporary normalized data used by manual entry and Smart Import. It is not a Room source of truth until validated and saved.

Contains nullable candidate fields corresponding to Order/OrderItem/Shipment (courier, tracking number, detected shipment status/events) plus extraction metadata.

## ExtractionField<T>
- value?
- confidence: 0.0..1.0
- source: OCR | LOCAL_PARSER | GROQ | USER
- evidence?
- needsReview

## SmartImportResult
- detectedPlatform?
- draft
- warnings
- overallConfidence
- usedGroq
- raw OCR text retained only according to privacy/storage rules
