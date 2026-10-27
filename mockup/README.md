# Ordra UI Mockup

Interactive HTML mockup of the Ordra app UI. Open `index.html` in a browser — no build step.

## Consistency rule

Per `docs/RULES.md` → **UI mockup consistency**: this mockup mirrors the live app UI (Kotlin + Jetpack Compose + Material 3). When UI code changes — screens, flows, components, theme tokens — update this folder in the same change so `index.html` stays consistent with the implemented UI.

- `styles.css` tokens come from `branding/theme_tokens.json`. Token changes land here first or alongside the Compose theme.
- `app.js` reference data mirrors the domain enums in `docs/BIBLE.md` and `docs/contracts/DOMAIN_MODELS.md` (platforms, order/shipment statuses, COD/payment states).
- Drift between the mockup and the live UI is a defect.

## What's inside

| Screen | Covers |
| --- | --- |
| Home | Summary stats (active, arriving soon, COD to prepare), Ordi AI banner, arriving soon, recent orders |
| Orders | Search, platform/status/COD/archived filters, sorting, order cards |
| Order details | Tracking timeline, items, price breakdown, payment/COD, notes, archive/delete, mark delivered, post-purchase (rating, favorite, returns/refunds/warranties/receipts) |
| New order | 5-step wizard: Platform → Details & items → Payment → Delivery (incl. reminder chips) → Review |
| Delivery calendar | Month grid with parcel/COD dots, per-day list |
| Detected updates | Suggestion review — apply / ignore / pick-parcel, confidence chips |
| Smart import | Screenshot pick → extracted-field review with confidence → save |
| Analytics | Spend metrics, platform breakdown, monthly history |
| Settings | Theme, defaults, reminders, smart-detection toggles, AI toggle, backup/restore/CSV, About |

Sample data stands in for Room. Actions (add, deliver, archive, delete, search, theme, apply detections, import, backup/export) are functional in-page — backup export produces a real `.json` download and restore parses it back.

## Scope

Mockup mirrors the app through Phase 9 (`docs/plan/IMPLEMENTATION_QUEUE.md`): manual orders, shipments, reminders, smart import (simulated OCR), detection review, post-purchase tools, analytics, backup/export. Groq calls and the real notification listener are device-side only — the mockup shows the review surfaces, not live AI.
