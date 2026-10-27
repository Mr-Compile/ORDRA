# Phase 1B — Manual Order Management

## Goal
Make Ordra useful offline without AI.

## Implement
- Add Order flow with platform, seller, one or multiple items, quantity/variant, item price, shipping, discounts, total, payment/COD, order date, ETA range, notes and image URI.
- Shipment management per order: one or more shipments with courier (enum + custom name), tracking number, manual shipment status, and TrackingEvent timeline. Manual updates create events with `source = MANUAL`.
- Require only the minimum practical fields: order/product label, platform and total. Other fields remain optional unless logically required.
- Review-before-save screen.
- Edit, archive, restore and deliberate permanent-delete flow.
- Money calculations use decimal-safe storage; never Float/Double for persisted currency math.
- Friendly validation and unsaved-change protection.
- OTHER/custom platform support.

## Exit criteria
[ ] Create/edit/archive/restore survive app restart.
[ ] Multi-item totals are correct.
[ ] Orders can carry multiple shipments; each shipment has its own status + timeline.
[ ] Manual shipment status updates create TrackingEvents and survive restart.
[ ] Invalid/blank inputs cannot corrupt persistence.
[ ] Back navigation preserves or warns about drafts.
[ ] No technical exception text reaches users.
