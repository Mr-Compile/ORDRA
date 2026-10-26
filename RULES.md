# Ordra Rules

## Non-negotiable product rules
1. Android only. Do not introduce Flutter, React Native, iOS, Electron, or desktop targets unless the owner explicitly changes scope.
2. Kotlin + Jetpack Compose is the approved client stack.
3. Room is the local source of truth for order data.
4. The app must remain useful offline. Core order management, reminders, analytics, backup/restore, and basic screenshot OCR must work without internet.
5. Never model the database around one marketplace. Use a universal Order/OrderItem model.
6. AI/OCR output is a draft/suggestion until validated and confirmed.
7. Never hardcode production API keys, secrets, tokens, passwords, or credentials.
8. Never commit `.env`, local secret files, signing keys, or credentials. Groq credentials live only in local git-ignored config (see `secrets.example.properties`).
9. Never display stack traces/database exceptions/raw API failures to users.
10. Do not invent marketplace APIs, permissions, endpoints, schemas, or platform behavior. Verify before implementation.
11. No backend. Do not add Ktor/Node/Express/Laravel/Django/FastAPI/Supabase/Firebase/REST/GraphQL servers. The app talks to Groq directly, and only for optional Smart Import.
12. No account system. No login, registration, passwords, OAuth, social sign-in, remote profiles, user roles, or admin accounts. Ordra has one local owner: the person using the device.
13. No cloud database or cloud sync. No Firestore/Realtime DB/Supabase/MongoDB Atlas/remote SQL. Backup is local export/import (JSON/ZIP/CSV) only.
14. Groq is the only planned online dependency and only for Smart Import interpretation. It must degrade gracefully when unavailable; Smart Import falls back to OCR + manual review.

## Engineering rules
- Prefer simple, testable code over premature abstraction.
- Start as a single Android app module; modularize only when justified.
- UI must not access Room DAO directly.
- Flow: Compose UI → ViewModel → Use Case → Repository → Data Source.
- Domain models must not depend on Compose UI classes.
- Database migrations must be versioned. Do not rely on destructive migration for user data.
- Monetary values must not use floating-point types for persisted calculations. Use integer minor units or a deliberate decimal strategy.
- Dates/times must have one documented representation and timezone policy.
- Every asynchronous operation must expose loading/success/error state where user-facing.
- Back navigation must behave predictably and must not duplicate Home screens.
- Accessibility: content descriptions where needed, scalable text, adequate touch targets, no meaning conveyed only by color.
- UI mockup consistency: the `mockup/` folder mirrors the live app UI. When screens, flows, components, or theme tokens change, update the mockup in the same change. Drift is a defect.

## AI coding rules
- Plan before code.
- Audit existing code before creating replacements.
- Do not hallucinate files, APIs, dependencies, or completed work.
- Reuse existing patterns if they comply with this Bible and Rules.
- Do not rewrite working architecture without a documented reason.
- Do not mark a task DONE merely because files exist; acceptance criteria and tests must pass.
- Make small feature-focused commits/milestones.
- If blocked, state the exact blocker and evidence instead of faking completion.
- After each phase: build, test, audit, update QA status, then proceed.
