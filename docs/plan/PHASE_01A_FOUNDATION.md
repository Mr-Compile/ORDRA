# Phase 1A — Android Foundation

## Goal
Create a compiling, tested native Android foundation with no smart-import or marketplace automation.

## Stack
Kotlin, Jetpack Compose, Material 3, MVVM/Clean Architecture, Hilt, Room, DataStore, Coroutines/Flow, Navigation Compose, Kotlinx Serialization, Coil, Gradle Kotlin DSL.

## Implement
- Application/Hilt setup and build configuration.
- Feature-first packages: core, data, domain, feature.
- Universal models: Order, OrderItem, Platform, OrderStatus, PaymentMethod, PaymentStatus, Money representation and delivery dates.
- Room entities, relations, DAOs, database, explicit migration strategy and mappers.
- Repository interfaces/implementations.
- Use cases for create/read/update/archive/restore/delete/status updates.
- DataStore settings foundation.
- App navigation shell and route contracts.
- Domain-friendly error/result model.
- Unit tests for mapping, repositories and persistence.

## Forbidden in 1A
Groq, OCR, screenshot parsing, NotificationListenerService, cloud sync, advanced analytics.

## Exit criteria
[ ] Debug build succeeds.
[ ] Database CRUD tests pass.
[ ] No UI talks directly to Room.
[ ] Hilt graph resolves.
[ ] Migration policy exists.
[ ] No release secret is embedded.
