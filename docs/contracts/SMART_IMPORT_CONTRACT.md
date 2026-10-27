# Smart Import Contract

## Goal
Convert an order screenshot into a reviewable OrderDraft without making AI the source of truth. Screenshot scanning also detects shipment data — courier, tracking number, shipment status text, tracking events and delivery estimates — which flow through the StatusNormalizer and shipment-matching pipeline in `docs/contracts/TRACKING_CONTRACT.md` before suggesting a shipment update.

## Pipeline
Image URI
-> local image access/validation
-> ML Kit OCR
-> normalized OCR text
-> platform detection
-> deterministic/local parsing (order fields + courier/tracking/status/events)
-> optional Groq interpretation
-> field-level merge
-> validation/confidence
-> Review UI
-> user corrections
-> ConfirmSmartImportUseCase
-> OrderRepository (+ shipment fields routed to ShipmentRepository)
-> Room

When a screenshot belongs to an already-saved shipment (e.g. a marketplace tracking
page), parsing may instead produce a `TrackingSuggestion` -> normalized ShipmentStatus
-> match -> review -> TrackingEvent, never a silent overwrite.

## Network policy
Groq is optional and the only planned online service. No Groq availability means local OCR/manual review remains usable. Courier/third-party tracking APIs are never used for shipment status.

## Merge precedence
User-confirmed values > deterministic high-confidence local values > Groq suggestions > weak OCR guesses.

Never replace a user-edited value with a later AI response.

## Groq response
Request structured JSON only. Parse into a dedicated DTO, validate every field, then map to domain draft. Unknown enum values must not crash parsing.

## Privacy
Prefer sending normalized extracted text rather than full screenshots when sufficient. Minimize personal/address/payment data sent to Groq. Never log the API key or unnecessary sensitive OCR content.

## ORDRA AI fallback for notification detection (Phase 5A)
The same Groq path may interpret ambiguous delivery-related notification text when
deterministic notification parsing cannot structure it (Phase 5A, see
`docs/contracts/TRACKING_CONTRACT.md` → Local-first parsing & AI fallback). Same
rules apply: optional and separately toggleable, minimized single-notification
payload only, structured JSON response validated per field, output treated as
candidate signals — never authoritative. It may classify delivery wording and
extract courier/marketplace/COD/order identifiers; it may not invent tracking
events, guess tracking numbers, mutate a shipment without validation, or resolve
ambiguous matches without sufficient evidence.

## Failure modes
Handle: no text found, unreadable screenshot, unsupported layout, no network, timeout, 4xx/5xx, rate limit, malformed JSON, partial extraction, duplicate candidate.

## Duplicate detection
Suggest possible duplicates using platform/order/tracking/date/amount/item signals. Never silently discard or merge an order.

## Review
Fields with uncertainty are visibly reviewable. Saving requires domain validation, not AI confidence alone.
