# ORDRA UI/UX Bible

## Product experience
Ordra should feel modern, clean, fast, trustworthy, friendly and organized: a polished consumer parcel app, not a generic enterprise admin dashboard. It is an information-heavy utility, so clarity beats decoration and important parcel state must be visible within seconds of opening the app.

## Official visual formula
Ordra uses one hybrid design system:

- **70% Minimalism + Material-inspired UI.** Clean surfaces, strong hierarchy, generous but efficient whitespace, rounded components, subtle borders, restrained shadows. This controls the whole interface.
- **20% Bento grid.** Dashboards and summaries only: Home, delivery statistics, quick actions, alerts, recent activity. Never forced onto forms, lists, tracking details or settings.
- **10% Glassmorphism, gradients and aurora.** Branding accents and floating chrome only: splash, onboarding, empty-state illustration areas, floating navigation/controls, modal overlays, selected premium summary elements. Never behind dense text or tracking data.

Do not introduce other major UI styles. The app must feel cohesive, not like several design trends combined at random.

## Principles
1. One-glance status: users should immediately see what is arriving, what is COD, and what needs attention. The most important current shipment gets the largest visual emphasis on Home.
2. Local-first confidence: offline is a normal state, not an error state.
3. Progressive disclosure: show essential order information first; advanced fields remain available without overwhelming the user. Important parcel information stays discoverable — minimal never means empty.
4. Review before automation: OCR/Groq/notification-assisted changes must be reviewable when uncertain.
5. Honest tracking: never label status "live" or "real-time" courier tracking — use Smart Parcel Status wording and show detection source + freshness (e.g. "Detected 8:42 AM · Shopee notification").
6. Consistency: one status vocabulary, one component set, one spacing system, one navigation model, one icon family (Material Symbols Rounded).
7. Accessible by default: readable contrast, scalable text, meaningful semantics, 48dp minimum touch targets, status never encoded by color alone.
8. No marketplace imitation: marketplaces (Shopee, TikTok Shop, Lazada…) are identified by small badges/text, and couriers (SPX, J&T, Ninja Van…) are shown as data. Ordra keeps its own brand; marketplace vs courier is a structural distinction, never a merged label.
9. Scannable density: card lists and timelines optimize for fast scanning — status chip, latest event and ETA before secondary detail.

## UX priority rules
When goals conflict, resolve in this order:
- Usability beats visual beauty.
- Readability beats branding.
- Performance beats animation (low-end Android devices must stay usable).
- Ordra's design system beats marketplace branding.

## Primary navigation
Bottom destinations: Home, Orders, Calendar, Insights, with a visually emphasized central Add/Scan action. Settings/Profile is accessed from the top app bar. On medium/expanded window sizes the bottom bar becomes a NavigationRail/sidebar. See `docs/uiux/NAVIGATION_UX.md` for the canonical model and the spec-vs-canonical decision record.

## Key user journeys
- Add order manually in minimal steps.
- Scan screenshot -> extract -> review -> save.
- See today's/upcoming deliveries and COD to prepare.
- Search/filter all orders.
- Answer "where is my parcel right now?" from a parcel's tracking detail.
- Open an order and manage status, receipt, warranty, return/refund.
- View local spending insights.
- Configure reminders, theme, backup and privacy.
