# Data Model Specification

Core aggregates: Order and OrderItem. Supporting models may include Category, Reminder, TrackingEvent, Attachment, ReturnRecord and WarrantyRecord as their phases arrive.

## Order principles
- Stable UUID/local ID.
- Platform is an enum/provider key plus optional custom display name.
- Money uses integer minor units or a decimal-safe representation with currency code.
- Store timestamps consistently and format in UI; delivery dates may be date-only.
- Soft archive is distinct from permanent delete.
- Status history may be introduced when platform assistance/post-purchase requires it.
- Imported/OCR/AI data first lives in OrderDraft; only validated user-confirmed data becomes Order.

## Relationships
Order 1→N OrderItem. Order N↔N Category only if multi-category is retained; otherwise simplify to one category. Order 1→N Reminder/Attachment/TrackingEvent as features arrive.
