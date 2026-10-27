# Navigation & Routing

> Note: superseded by `docs/uiux/NAVIGATION_UX.md` (UI/UX Pack v1), which replaces the
> Scan/More tabs with Home | Orders | Calendar | Insights plus a central Add/Scan sheet.
> This file remains as the original route list; treat NAVIGATION_UX as canonical for the
> navigation model until owner confirms. Tracked in `docs/plan/MEGAPLAN_UIUX.md` → Conflicts.

Root: Splash -> Onboarding (first run only) -> MainShell.
MainShell tabs: Home | Orders | Scan | Calendar | More.

Routes:
home
orders
orders/{orderId}
orders/add
orders/{orderId}/edit
orders/filter
scan
scan/processing
scan/review
calendar
reminders
spending
history
returns
returns/{id}
warranty
warranty/{id}
backup
notification-assistance
settings
about

Rules: detail/edit routes return to the originating logical screen; Save must not create duplicate screens; tab reselection may scroll to top; deep links are optional, not required for V1; destructive confirmation uses dialog/sheet; unsaved form changes prompt before discard.
