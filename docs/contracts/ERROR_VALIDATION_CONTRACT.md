# Error & Validation Contract

## Error categories
- ValidationError
- NotFound
- DatabaseFailure
- FileAccessFailure
- PermissionRequired
- OcrFailure
- NetworkUnavailable
- GroqFailure
- RateLimited
- InvalidAiResponse
- BackupInvalid
- UnknownFailure

Map implementation exceptions to domain/app errors before UI.

## Validation
Centralize important rules:
- required order/product name where applicable
- platform required
- total amount valid
- quantity >= 1
- delivery end not before start
- deliveredAt consistent with status where enforced
- currency code valid
- refund amount valid relative to order/refund policy
- warranty end not before start
- shipment status transition valid (reject or require confirmation for invalid ones, e.g. DELIVERED -> IN_TRANSIT)
- detected shipment update carries a known TrackingStatusSource and confidence

## User messages
Messages must be actionable and human-readable. Raw stack traces/class names never appear in production UI.

## Logging
Logs may contain technical context but must avoid secrets and unnecessary personal/order data.
