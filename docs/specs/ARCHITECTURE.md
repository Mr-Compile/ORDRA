# Architecture Specification

UI (Compose) → ViewModel/state → Use cases → Repository contracts → data implementations → Room/DataStore/platform services → optional Groq network client (Smart Import only).

Room is the source of truth for user orders. Online intelligence enriches drafts but does not become the authoritative store. There is no backend, cloud database, cloud sync, or account system: the app works fully offline except for the optional Groq call.

Use feature-first packages while keeping boundaries clear. Do not split into many Gradle modules until build/codebase size justifies it. Android platform services must be wrapped behind interfaces so business logic remains testable.
