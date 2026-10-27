# Master QA Checklist

Run at every phase gate.

## Build & code
[ ] Debug build passes
[ ] Relevant unit/instrumentation/UI tests pass
[ ] No critical lint errors
[ ] No unresolved scope TODO/stub
[ ] No duplicate competing implementation

## Data
[ ] Persistence survives restart
[ ] Migration tests pass when schema changes
[ ] Money/date calculations verified
[ ] Destructive actions confirmed
[ ] Shipments/tracking events retain Order -> Shipment -> TrackingEvent integrity

## Tracking (when shipment features are in scope)
[ ] Detected statuses carry source + confidence; confidence gates enforced
[ ] Manual shipment status always works; tracking survives with notification access off
[ ] No courier API/third-party tracker/scraping dependency anywhere
[ ] Checklist: docs/qa/TRACKING_QA.md

## UX
[ ] Loading/empty/error states exist
[ ] Back navigation works
[ ] Light/dark themes work
[ ] Accessibility basics pass
[ ] User sees friendly errors

## Security/privacy
[ ] No committed secrets
[ ] No sensitive production logs
[ ] Permissions justified and optional where possible
[ ] Online processing matches privacy docs

## Phase verdict
PASS only when all critical acceptance criteria are met. Otherwise create a repair list and do not proceed.
