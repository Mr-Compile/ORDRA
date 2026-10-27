# Copy/Paste — Full Codebase Audit Prompt

Audit Ordra against docs/BIBLE.md, docs/RULES.md, AGENTS.md, docs/TECH_STACK.md, docs/BRANDING.md, docs/specs/*, docs/contracts/* and docs/plan/*. Determine the latest genuinely completed phase from evidence, not filenames.

Check architecture, database/migrations, navigation, state handling, errors, accessibility, security/privacy, secrets, Android permissions, tests, dead/duplicate code, placeholders, TODOs, Groq/OCR implementation, notification scheduling/listening, shipment/tracking model integrity (Order → Shipment → TrackingEvent, source+confidence, no courier API/scraping) and release readiness as applicable.

Return: completed phase, PASS/FAIL per subsystem, evidence, defects by severity, exact repair plan, tests required, and whether work may proceed. Do not change scope or invent requirements.
