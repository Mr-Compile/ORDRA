# Smart Import Contract

## Goal
Convert an order screenshot into a reviewable OrderDraft without making AI the source of truth.

## Pipeline
Image URI
-> local image access/validation
-> ML Kit OCR
-> normalized OCR text
-> platform detection
-> deterministic/local parsing
-> optional Groq interpretation
-> field-level merge
-> validation/confidence
-> Review UI
-> user corrections
-> ConfirmSmartImportUseCase
-> OrderRepository
-> Room

## Network policy
Groq is optional and the only planned online dependency. No Groq availability means local OCR/manual review remains usable.

## Merge precedence
User-confirmed values > deterministic high-confidence local values > Groq suggestions > weak OCR guesses.

Never replace a user-edited value with a later AI response.

## Groq response
Request structured JSON only. Parse into a dedicated DTO, validate every field, then map to domain draft. Unknown enum values must not crash parsing.

## Privacy
Prefer sending normalized extracted text rather than full screenshots when sufficient. Minimize personal/address/payment data sent to Groq. Never log the API key or unnecessary sensitive OCR content.

## Failure modes
Handle: no text found, unreadable screenshot, unsupported layout, no network, timeout, 4xx/5xx, rate limit, malformed JSON, partial extraction, duplicate candidate.

## Duplicate detection
Suggest possible duplicates using platform/order/tracking/date/amount/item signals. Never silently discard or merge an order.

## Review
Fields with uncertainty are visibly reviewable. Saving requires domain validation, not AI confidence alone.
