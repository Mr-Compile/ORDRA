# Frontend Rules

- Android only; Kotlin + Jetpack Compose + Material 3.
- Use Navigation Compose and one canonical route graph.
- UI talks to ViewModels/state; never directly to Room/DAO.
- Every screen defines Loading, Content, Empty and Error states where applicable.
- All destructive actions require appropriate confirmation or undo.
- Preserve state across rotation/process recreation where reasonable.
- Back behavior must be predictable; never duplicate Home on the stack.
- Use design tokens; do not scatter raw colors/dimensions across screens.
- Dark and light themes are mandatory.
- Avoid technical user-facing errors.
- No fake online dependency for offline features.
- Groq UI must show offline/failure fallback.
- Notification access is optional and must explain why it is requested; rider/message detection gets its own consent explainer before enabling.
- Do not silently update an order/shipment from uncertain detection matches; ambiguous matches always route to parcel-pick review.
- AI/OCR import must show a review screen before permanent save.
- No placeholder screens may be marked complete.
- Each screen must have preview/sample states and QA acceptance criteria.
