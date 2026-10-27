# ORDRA Integration Contract Bible

## One architecture
Ordra must not develop parallel implementations for the same capability.

### Dependency direction
`UI -> Domain -> Data`

Android/framework implementations may satisfy interfaces owned by the appropriate inner layer, but domain logic must not depend on Compose, Room entities, Retrofit response classes, or Android UI classes.

## Source of truth
Room is authoritative for persisted user order data. UI state is derived from repositories/use cases. Groq responses, OCR results, notification text, and temporary form state are never authoritative persisted records until validated and intentionally saved.

## Feature vertical slice
Every feature should be traceable:
`Screen -> ViewModel -> UiState/UiEvent -> UseCase -> Repository -> DAO/service -> result -> ViewModel -> UI`

## Naming
Use canonical names from DOMAIN_MODELS.md. Do not create aliases such as `totalPrice`, `grandTotal`, and `amountTotal` for the same concept.

## Money
Persist money as integer minor units (`Long`) plus ISO currency code. For PHP, ₱649.50 is `64950` with currency `PHP`. Never persist monetary values as Float/Double.

## IDs and dates
Use stable IDs. Persist instants/epoch values consistently. Local date-only concepts (such as an estimated delivery date) must remain date concepts rather than fake midnight instants. Conversion rules belong in mappers.

## Mutations
All persisted mutations pass through repositories/use cases. Screens must not update DAOs directly.

## Destructive actions
Delete, restore, archive, backup restore, and overwrite operations require explicit behavior and appropriate confirmation.

## Smart systems
OCR/Groq/notification parsing produce suggestions or drafts. They must not silently overwrite trusted user data when a match or extraction is uncertain.

## Offline
A network failure must never prevent reading or manually managing existing orders.
