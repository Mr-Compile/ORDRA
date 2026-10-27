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
- courier?
- trackingNumber?
- notes?
- productImageUri?
- archivedAt?
- createdAt
- updatedAt

Invariant: total values must be non-negative unless a specifically documented adjustment model permits otherwise. `isCod` must be consistent with payment semantics.

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

## Platform
SHOPEE, TIKTOK_SHOP, LAZADA, TEMU, AMAZON, FACEBOOK, INSTAGRAM, OTHER

## OrderStatus
DRAFT, ORDERED, PROCESSING, PACKED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED, REFUNDED

## PaymentMethod
COD, EWALLET, CARD, BANK_TRANSFER, CASH, OTHER, UNKNOWN

## PaymentStatus
UNPAID, PARTIALLY_PAID, PAID, REFUNDED, UNKNOWN

## Category
id, name, createdAt, updatedAt

## TrackingEvent
id, orderId, status?, title, description?, occurredAt, source

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

Contains nullable candidate fields corresponding to Order/OrderItem plus extraction metadata.

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

## NotificationSuggestion
- sourcePackage
- candidateOrderId?
- proposedStatus?
- proposedTrackingNumber?
- confidence
- explanation
- requiresConfirmation
