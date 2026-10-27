# Phase 9 — Security, Privacy, Performance & Release Hardening

## Implement
- Dependency/security audit and release configuration review.
- Verify no secrets/tokens/debug endpoints/logged PII in release build.
- Optional biometric/app-lock design if implemented.
- Database migration tests across released schema versions.
- Performance profiling for startup, large lists, image/OCR flows.
- Accessibility audit.
- Privacy policy/data-safety inventory based on actual behavior (incl. notification-listener data handling: parsed fields only, no raw notification storage).
- Tracking audit per `docs/qa/TRACKING_QA.md`: no courier API/scraping dependency, confidence gates enforced, manual fallback verified.
- Crash handling/telemetry decision with privacy review.
- Signed release/AAB pipeline and versioning policy.
- Groq key architecture decision resolved for the release channel: personal-only build keeps local-config keys; any public distribution requires the API-key reassessment defined in PHASE_04 and PRIVACY_SECURITY before release.
- Crash/telemetry default is none; adding any online telemetry requires owner approval and a privacy review.

## Exit criteria
[ ] Release build passes lint/tests.
[ ] No critical/high unresolved security issue.
[ ] No destructive migration for normal upgrades.
[ ] Permissions are minimal and contextual.
[ ] Privacy documentation matches implementation.
