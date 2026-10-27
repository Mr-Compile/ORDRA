# Architecture Specification

UI (Compose) → ViewModel/state → Use cases → Repository contracts → data implementations → Room/DataStore/platform services → optional Groq network client (Smart Import only).

Room is the source of truth for user orders, shipments and tracking events. Online intelligence enriches drafts but does not become the authoritative store. There is no backend, cloud database, cloud sync, or account system: the app works fully offline except for the optional Groq call.

## Tracking architecture
Shipment is a first-class aggregate: `Order 1→N Shipment 1→N TrackingEvent`. Parcel status never comes from courier APIs or scraped pages — it is detected locally from marketplace notifications (NotificationListenerService), on-device OCR, shared content, and manual updates, then normalized by a dedicated `StatusNormalizer` and gated by confidence before it touches persisted data. A `TrackingProvider` interface is kept as an extension point so a future official courier integration could plug in without redesigning Order/Shipment/TrackingEvent, repositories, or UI. No provider is implemented in v1. Full contract: `docs/contracts/TRACKING_CONTRACT.md`.

Use feature-first packages while keeping boundaries clear. Do not split into many Gradle modules until build/codebase size justifies it. Android platform services must be wrapped behind interfaces so business logic remains testable.
