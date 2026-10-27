# Data Model Specification

Core aggregates: Order, OrderItem and Shipment. Supporting models may include TrackingEvent, Category, Reminder, Attachment, ReturnRecord and WarrantyRecord as their phases arrive.

## Order principles
- Stable UUID/local ID.
- Platform is an enum/provider key plus optional custom display name.
- Money uses integer minor units or a decimal-safe representation with currency code.
- Store timestamps consistently and format in UI; delivery dates may be date-only.
- Soft archive is distinct from permanent delete.
- Shipment status history lives on the TrackingEvent timeline (Shipment 1→N TrackingEvent), attributed by source + confidence plus minimal provenance fields (`rawStatus`, `sourceDetail`) — never raw notification/OCR content. Detection outcomes are recorded in a lightweight `DetectionRecord` history (Phase 5A).
- Imported/OCR/AI data first lives in OrderDraft; only validated user-confirmed data becomes Order.

## Relationships
Order 1→N OrderItem. Order 1→N Shipment; Shipment 1→N TrackingEvent (events reference `shipmentId`, not `orderId`). Courier/tracking-number fields live on Shipment — an order can carry multiple parcels. Order N↔N Category only if multi-category is retained; otherwise simplify to one category. Order 1→N Reminder/Attachment as features arrive.
