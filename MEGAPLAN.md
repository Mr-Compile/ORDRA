# Ordra Megaplan

Merged execution roadmap across the Foundation Pack, Complete Project Pack (v2) and Frontend Pack.
One source of truth for phase order and status. Update status after each phase gate.

## Rules of execution
- Workflow per phase: Plan → Implement → Test → Audit → Repair → Verify → Commit.
- No phase proceeds on a failed gate. No future-phase features early.
- Architecture: Android-only, Kotlin + Compose M3, MVVM/Clean, Room source of truth,
  Hilt, DataStore, WorkManager, Nav Compose, Coil. Local-only: no backend/accounts/cloud.
- Only online dependency: direct Groq API (Phase 4+), key in git-ignored `secrets.properties`.
- `mockup/` mirrors the live UI; update it with any UI change (drift = defect).

## Status key
DONE / IN PROGRESS / PENDING / DEFERRED / BLOCKED

| Phase | Scope | Frontend map | Status | Notes |
|---|---|---|---|---|
| 0 | Product & branding foundation | F0 (partial) | DONE | Name, tagline, tokens, light/dark theme, adaptive icon, system splash, launcher branding |
| 1A | Android foundation + persistence | F0 | IN PROGRESS | Build config fixed for AGP 9 built-in Kotlin (2.2.10). Remaining: domain models, Room entities/DAO/db/mappers, repository + use cases, settings DS, nav shell, error model, unit tests |
| 1B | Manual order management | F1 | PENDING | Add/edit multi-section form, review-before-save, archive/restore/delete |
| 1C | Home & order details | F1 | PENDING | Home summary (active/arriving/COD), detail screen, status chips, mark-delivered |
| 1D | Search/filters/categories/COD | F2 | PENDING | Search items/seller/tracking/notes, filters+sort, categories, COD totals |
| 1E | Settings, onboarding, polish, core QA | F2 | PENDING | Theme/currency/defaults, first-run onboarding, About, states polish, Phase-1 QA gate |
| 2 | Reminders, calendar, notifications | F3 | PENDING | WorkManager scheduling, reminder types, daily summary, delivery calendar upgrades, channels |
| 3 | Screenshot Smart Import (local) | F4 | PENDING | Photo picker, ML Kit OCR, deterministic parser, OrderDraft, confidence, review |
| 4 | Groq-assisted extraction | F4 | PENDING | Direct app→Groq (Retrofit/OkHttp/kotlinx), strict schema, graceful offline fallback |
| 5 | Share-to-Ordra + notification assistance | F5 | PENDING | Share target, NotificationListenerService (optional), parsers, confirm suggestions |
| 6 | Spending analytics & history | F6 | PENDING | Monthly/weekly totals, platform/category breakdown, local-only |
| 7 | Post-purchase tools | F7 | PENDING | Returns/refunds, warranties, receipts/attachments |
| 8 | Backup, restore & export | F8 | PENDING | JSON/ZIP backup, CSV export, restore validation |
| 9 | Hardening & release prep | F9 | PENDING | Security/privacy/perf audit, a11y, migrations, release config; Groq key decision for channel |
| 10 | Beta & launch | F9 | PENDING | Needs owner: signing, store listing, beta rollout |

## Current run scope
Implement through Phase 1E (offline core) — the reliable offline manual-order app.
Deferred screens until their phase: Scan import (tab pre-wired), reminders config,
spending, returns/warranty, backup, notification assistance.

## Deferred decisions (owner)
- Public distribution → Groq key architecture reassessment (Phase 9).
- minSdk 26 confirmed; revisit to 24 only if install-base demands it.
