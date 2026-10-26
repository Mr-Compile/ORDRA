# Phase 1B — Manual Order Management

## Goal
Make Ordra useful offline without AI.

## Implement
- Add Order flow with platform, seller, one or multiple items, quantity/variant, item price, shipping, discounts, total, payment/COD, courier, tracking number, order date, ETA range, notes and image URI.
- Require only the minimum practical fields: order/product label, platform and total. Other fields remain optional unless logically required.
- Review-before-save screen.
- Edit, archive, restore and deliberate permanent-delete flow.
- Money calculations use decimal-safe storage; never Float/Double for persisted currency math.
- Friendly validation and unsaved-change protection.
- OTHER/custom platform support.

## Exit criteria
[ ] Create/edit/archive/restore survive app restart.
[ ] Multi-item totals are correct.
[ ] Invalid/blank inputs cannot corrupt persistence.
[ ] Back navigation preserves or warns about drafts.
[ ] No technical exception text reaches users.
