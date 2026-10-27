# UI Implementation Phases

Aligned with the official v1.1 design direction: 70% Minimalism + Material, 20% Bento
(dashboards/summaries), 10% restrained glass/gradient/aurora. Full detail, screen audit and
migration checklist: `docs/plan/MEGAPLAN_UIUX.md`.

## UI-0 Design Foundation
Material 3 theme, color roles, typography, spacing, radius/shape (8/12/16/20–24), elevation,
motion-duration and `GlassSurface` tokens, status→icon+color system, icon rules, previews,
light/dark verification, window-size-class policy.

## UI-1 Navigation Shell
Splash/onboarding state, Home shell, bottom navigation with central emphasized Add/Scan
(restrained glass allowed), Add/Scan sheet, settings route, back-stack rules, adaptive
bottom-bar → NavigationRail behavior.

## UI-2 Core Order Components
`ParcelCard` primitive + variants, status/payment/platform/courier chips and badges,
`TrackingProgress`, summary/stat cards, button/input family, money/date formatting,
empty/error/offline/skeleton components.

## UI-3 Order Management Screens
Orders, Add/Edit, Review, Order Detail, Tracking Detail (status hero → ETA → progress →
timeline), archive/history interactions.

## UI-4 Home + Search + COD
Bento dashboard sections + featured parcel card, search/filter chips/sort, COD overview and
attention states.

## UI-5 Calendar + Reminders
Delivery calendar, day details, reminder configuration, notification permission UX.

## UI-6 Smart Import
Image selection, processing, OCR/offline state, Groq state, confidence review, correction and
save. Gradient/aurora accents allowed on entry/illustration areas.

## UI-7 Smart Parcel Status Assistance
Share intent UX, notification access explanation, shipment status suggestions with
source/confidence, review/confirm/ignore, shipment timeline, Track Externally, status
notification settings, detection/activity feed.

## UI-8 Insights + Advanced Order Lifecycle
Insights (Bento stat cards allowed), returns/refunds, warranty, receipts and history.

## UI-9 Settings + Backup + About
Appearance, defaults, privacy, backup/restore/export, Groq configuration, About.

## UI-10 Polish & Regression
Accessibility (incl. glass readability + reduced motion), large fonts, dark mode,
adaptive layouts across window size classes, rotations/config changes,
empty/error/offline/permission states, navigation regression, performance and screenshot QA,
visual-formula consistency check.

Each phase follows IMPLEMENT -> TEST -> UI AUDIT -> REPAIR -> VERIFY. Do not advance with
blockers.
