# Phase 4 — Groq-Assisted Smart Extraction

## Goal
Use Groq only where it materially improves messy OCR/order understanding.

## Architecture
The app calls the Groq API directly; no backend exists in this project. Development and personal use read the key from local configuration excluded from Git (`secrets.properties`, template in `secrets.example.properties`). Secrets embedded in a distributed APK can never be considered secure — acceptable for personal use only. If Ordra is ever publicly distributed, the API-key architecture MUST be reassessed before release (OUT OF CURRENT SCOPE / FUTURE CONSIDERATION: a controlled proxy).

## Flow
OCR/local parser → if uncertain and user allows online analysis → redact/minimize sensitive data where possible → Groq → strict structured response → schema validation → OrderDraft → user review.

Offline path (mandatory): OCR/local parser → OrderDraft → user review/correction → save. On Groq failure (no internet, timeout, API error, rate limit, invalid response, invalid key config) show a friendly message such as "Smart analysis isn't available right now. You can still review the detected text and complete the order manually." Never expose raw networking exceptions.

## Implement
- Versioned extraction schema.
- Strict JSON validation and fallback on malformed AI output.
- Timeout/retry/rate-limit handling.
- Never let AI calculate trusted monetary totals without deterministic verification.
- Privacy disclosure for online analysis: document exactly what fields are sent and minimize/redact names, addresses, phone numbers and unrelated screenshot content before transmission.
- Feature works without Groq through manual/local path.

## Exit criteria
[ ] No Groq credential is committed to Git or embedded in source.
[ ] Invalid AI responses cannot overwrite valid user data.
[ ] User always confirms before persistence.
[ ] Offline failure is graceful.
