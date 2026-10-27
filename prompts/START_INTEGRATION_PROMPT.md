# COPY/PASTE — ORDRA INTEGRATION START PROMPT

You are working on the existing ORDRA Android project.

First read the canonical project docs plus the Ordra Integration & Data Contract Pack under `docs/contracts/` (execution roadmap: `docs/plan/MEGAPLAN_INTEGRATION.md`). Do not begin by rewriting the project.

Task:
1. Audit the existing codebase against the integration contracts.
2. Produce a concise conflict report covering model names, Room schema, repositories, use cases, ViewModels/UiState, navigation, reminders, OCR/Groq, analytics, backup, and errors.
3. Preserve valid existing code.
4. Repair only the conflicts required for Integration Phase I0.
5. Do not implement later features merely because their contracts exist.
6. Build and run applicable tests.
7. Report actual PASS/FAIL results.
8. Stop after I0 and request/await the next implementation phase.

Hard rules:
- Android only.
- Kotlin + Jetpack Compose.
- Room is source of truth.
- Core app offline.
- No custom backend/accounts/cloud DB/cloud sync.
- Groq is optional and the only planned online service.
- No courier APIs, third-party tracking APIs, or scraped tracking pages — tracking is local Smart Parcel Status (docs/contracts/TRACKING_CONTRACT.md).
- Shipment is first-class: Order 1→N Shipment 1→N TrackingEvent; detected statuses carry source + confidence.
- No real API key in source, docs, logs, tests, or Git.
- UI cannot call DAO/network/WorkManager/NotificationListenerService directly.
- Money uses integer minor units, never Float/Double.
- AI/OCR/notification parsing creates drafts/suggestions, not silent authoritative mutations.
- One canonical `docs/plan/` and `docs/qa/` structure.
- Do not claim build/tests passed unless actually executed.

At completion provide:
- Files inspected
- Conflicts found
- Files changed
- Build result
- Test result
- Remaining blockers
- I0 READY: YES/NO
Then STOP.
