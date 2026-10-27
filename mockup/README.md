# Ordra UI Mockup

Interactive HTML mockup of the Ordra app UI. Open `index.html` in a browser — no build step.

## Consistency rule

Per `docs/RULES.md` → **UI mockup consistency**: this mockup mirrors the live app UI (Kotlin + Jetpack Compose + Material 3). When UI code changes — screens, flows, components, theme tokens — update this folder in the same change so `index.html` stays consistent with the implemented UI.

- `styles.css` tokens come from `branding/theme_tokens.json`. Token changes land here first or alongside the Compose theme.
- `app.js` reference data mirrors the domain enums in `docs/BIBLE.md` and `docs/contracts/DOMAIN_MODELS.md` (platforms, statuses, COD/payment states).
- Drift between the mockup and the live UI is a defect.

## What's inside

| Screen | Covers |
| --- | --- |
| Home | Summary stats (active, arriving soon, COD to prepare), arriving soon, recent orders |
| Orders | Search, platform/status/COD/archived filters, sorting, order cards |
| Order details | Status timeline, items, price breakdown, payment/COD, courier & tracking, notes, archive/delete, mark delivered |
| New order | 5-step wizard: Platform → Details & items → Payment → Delivery → Review |
| Settings | Theme (System/Light/Dark), default currency/platform/payment, notification placeholders, About |

Sample data stands in for Room. Actions (add, deliver, archive, delete, search, theme) are functional in-page.

## Scope

Mockup follows Phase 1 scope (`docs/plan/PHASE_01B_MANUAL_ORDERS.md`): manual orders only. Smart import, reminders scheduling and notification assistance are out of scope until those phases land.
