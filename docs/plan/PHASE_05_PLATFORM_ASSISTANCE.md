# Phase 5 — Android Platform Assistance

## Goal
Reduce manual work while avoiding unsupported scraping or assumptions about marketplace APIs.

## Implement
- Android Sharesheet receiver for images/text/URLs where useful.
- Import intent routes into an OrderDraft/review flow.
- Optional NotificationListenerService with explicit onboarding and settings shortcut.
- Provider adapters/parsers for supported marketplace notification formats.
- Match notifications to existing orders using tracking number first, then conservative metadata matching.
- Suggest status changes; require confirmation for uncertain matches.
- Per-platform enable/disable controls.

## Rules
Do not read private app storage, automate marketplace credentials, bypass platform protections, or claim official integration unless it actually exists.

## Exit criteria
[ ] Revoking notification access leaves Ordra usable.
[ ] Unknown notifications are ignored safely.
[ ] Parser changes are tested with sanitized fixtures.
[ ] No silent low-confidence status changes.
