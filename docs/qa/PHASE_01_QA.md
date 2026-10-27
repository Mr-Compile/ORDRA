# Phase 1 QA Checklist

Mark each item PASS / FAIL / BLOCKED with evidence.

## Build
- [ ] Debug build succeeds.
- [ ] Unit tests pass.
- [ ] App launches from clean install.

## Data
- [ ] Create an order with minimum fields.
- [ ] Create an order with multiple items.
- [ ] Edit order and verify persistence.
- [ ] Archive and restore order.
- [ ] Create a shipment on an order and update its status manually (source = MANUAL event).
- [ ] Multiple shipments per order persist correctly.
- [ ] Delete flow requires appropriate confirmation (cascades items, shipments, events).
- [ ] Restart app and verify data remains.
- [ ] Money totals are correct and do not use unsafe floating-point persistence.

## UX
- [ ] Home summary reflects database changes.
- [ ] Search finds item/seller/tracking/platform/note as designed.
- [ ] Filters/sorting behave correctly.
- [ ] Back returns to expected screen without duplicate Home instances.
- [ ] Light, dark and system themes work.
- [ ] Empty states are understandable.
- [ ] User-facing errors are friendly.

## Offline
- [ ] Disable network and repeat core create/view/edit/search flows.

## Architecture audit
- [ ] Compose does not directly access DAO.
- [ ] Repository boundary is respected.
- [ ] No hardcoded secrets.
- [ ] No Groq/OCR/platform notification features were prematurely coupled into core order storage.
