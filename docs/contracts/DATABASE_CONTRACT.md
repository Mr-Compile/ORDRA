# Room Database Contract

## Core tables
At minimum:
- orders
- order_items
- shipments
- tracking_events
- categories
- order_categories
- reminders
- receipts
- warranties
- returns
- refunds

Add tables only when a real feature requires them.

## Relationships
- Order 1 -> many OrderItem
- Order 1 -> many Shipment
- Shipment 1 -> many TrackingEvent
- Order many <-> many Category through order_categories
- Order 1 -> many Reminder
- Order 1 -> many Receipt
- Order 1 -> zero/many Warranty records as defined by implementation
- Order 1 -> zero/many ReturnRecord
- Order 1 -> zero/many RefundRecord

`tracking_events.shipment_id` is required — events belong to a parcel, not the order
directly. `shipments.order_id` is required and indexed. Multiple shipments per order
must be supported by schema even where UI commonly creates one.

## Shipments & tracking schema notes
- `shipments`: id, order_id (FK), courier?, custom_courier_name?, tracking_number?,
  shipment_status, status_source, status_confidence, last_status_at?,
  last_detected_at?, created_at, updated_at. Index order_id and tracking_number.
- `tracking_events`: id, shipment_id (FK), shipment_status, title, description?,
  occurred_at, detected_at, source, confidence, raw_reference?. Index shipment_id and
  occurred_at for timeline ordering.
  - Phase 5A adds optional provenance columns `raw_status`? (short classification
    label) and `source_detail`? (source app/package key) via a versioned migration.
    Enum source values persist as strings, so new `TrackingStatusSource` values
    (COURIER_APP_NOTIFICATION, MESSAGE_NOTIFICATION) need no schema change.
  - Phase 5A adds a `detection_records` table: id, detected_at, source,
    source_detail?, outcome, candidate_shipment_id?, proposed_status?,
    confidence?, reason? — structured fields only, no raw notification bodies.
- Timeline queries order by occurred_at; dedupe detections so the same detected
  transition cannot create duplicate events (composite key: shipment + source +
  normalized status + time window + source event identifier/hash — not text
  equality alone).
- Courier/tracking columns that existed on `orders` in early schemas migrate into a
  default shipment row per affected order via an explicit versioned Migration —
  no destructive reset, no data loss.

## DAO rules
DAOs expose persistence primitives and observable queries. They do not contain UI formatting or Groq/OCR logic.

Useful query families:
- observeOrder(id)
- observeOrders(query/filter/sort)
- observeActiveOrders()
- observeArchivedOrders()
- observeUpcomingDeliveries()
- observeOutstandingCod()
- observeSpendingRange()
- observeShipments(orderId)
- observeShipment(shipmentId)
- observeTrackingTimeline(shipmentId)
- findShipmentByTrackingNumber(trackingNumber)
- transactionally load an order with related records (items, shipments, events)

## Transactions
Saving an order with items, restoring a backup, and other multi-table atomic operations must use Room transactions.

## Migrations
- Version database from the first release.
- Provide explicit migrations for schema changes.
- Do not use destructive migration for normal user upgrades.
- Migration tests must verify preserved user data.

## Mapping
Room entities stay in data layer. Map Entity <-> Domain explicitly. Do not leak Room annotations/entities into UI/domain.

## Deletion
Prefer archive for normal UX. Permanent deletion must define cascade behavior for items, shipments, tracking events, reminders, receipts metadata, warranties, returns, and refunds without leaving orphan rows. Deleting a shipment cascades its tracking events.
