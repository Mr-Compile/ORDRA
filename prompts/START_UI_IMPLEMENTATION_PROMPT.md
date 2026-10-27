# Copy-Paste Prompt — Start Ordra UI Implementation

You are implementing the UI/UX of ORDRA, an Android-only, local-first personal order and parcel manager.

Before coding, read the existing Ordra project Bible/rules/architecture/frontend documentation and this entire UI/UX pack. Existing architecture and data contracts take precedence over inventing replacements.

Implement only the next incomplete UI phase from `docs/plan/UI_IMPLEMENTATION_PHASES.md`.

Hard rules:
- Kotlin + Jetpack Compose + Material 3 only.
- Use the canonical Ordra design system and reusable components.
- Do not introduce Flutter, web UI, Firebase, accounts, cloud sync, or a custom backend.
- Do not redesign domain/database models merely to make UI easier; report contract conflicts instead.
- Do not add fake network dependencies.
- Core UI must work offline.
- Groq-specific UI must have offline/failure fallback.
- Do not silently accept uncertain AI/OCR/notification-derived changes.
- Never label tracking "live"/"real-time"; show detection source + freshness. Confidence gates decide auto-apply vs confirm vs suggest-only.
- Preserve predictable Android back behavior.
- Support light/dark themes and accessibility.
- Avoid placeholder screens presented as complete.

For the selected phase:
1. Inspect existing implementation.
2. State the exact screens/components/routes to create or modify.
3. Implement using existing ViewModels/use cases/contracts where available.
4. Add Compose previews where practical.
5. Add/update UI tests for critical behavior where the project test setup supports them.
6. Build/run available checks.
7. Audit against `docs/qa/UI_UX_QA.md` and `docs/qa/SCREEN_COVERAGE_MATRIX.md`.
8. Repair failures before declaring the phase complete.
9. Report files changed, tests/checks run, known issues, and PASS/FAIL.

Do not automatically proceed to the next UI phase after completion. STOP and report.
