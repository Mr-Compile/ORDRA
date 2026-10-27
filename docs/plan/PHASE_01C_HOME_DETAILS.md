# Phase 1C — Home & Order Details

## Goal
Give users a clear daily view of their purchases.

## Implement
- Home summary: active orders, arriving soon, outstanding COD.
- Upcoming and recent order sections.
- Reusable OrderCard and status chips.
- Order Detail as the canonical view for items, totals, payment, ETA, notes, order status and a Shipment section (courier, tracking number, shipment status with detected-at + source, actions: View Timeline / Update Status / Track Externally).
- Shipment Timeline screen showing ordered TrackingEvents with source attribution.
- Quick status actions such as Mark Delivered with confirmation where destructive/irreversible.
- Empty, loading and recoverable-error states.

## Exit criteria
[ ] Home reacts to Room Flow updates.
[ ] Totals match persisted orders.
[ ] Order detail handles missing optional data gracefully.
[ ] Back stack does not duplicate Home.
