# Room Database Contract

## Core tables
At minimum:
- orders
- order_items
- categories
- order_categories
- tracking_events
- reminders
- receipts
- warranties
- returns
- refunds

Add tables only when a real feature requires them.

## Relationships
- Order 1 -> many OrderItem
- Order many <-> many Category through order_categories
- Order 1 -> many TrackingEvent
- Order 1 -> many Reminder
- Order 1 -> many Receipt
- Order 1 -> zero/many Warranty records as defined by implementation
- Order 1 -> zero/many ReturnRecord
- Order 1 -> zero/many RefundRecord

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
- transactionally load an order with related records

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
Prefer archive for normal UX. Permanent deletion must define cascade behavior for items, events, reminders, receipts metadata, warranties, returns, and refunds without leaving orphan rows.
