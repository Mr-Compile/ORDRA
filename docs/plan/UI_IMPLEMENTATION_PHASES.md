# UI Implementation Phases

## UI-0 Design Foundation
Material 3 theme, color roles, typography, spacing, shapes, icon rules, previews, light/dark verification.

## UI-1 Navigation Shell
Splash/onboarding state, Home shell, bottom navigation, Add/Scan sheet, settings route, back-stack rules.

## UI-2 Core Order Components
OrderCard, status/payment/platform chips, summary cards, money/date formatting, empty/error/offline components.

## UI-3 Order Management Screens
Orders, Add/Edit, Review, Order Detail, archive/history interactions.

## UI-4 Home + Search + COD
Dashboard sections, search/filter/sort, COD overview and attention states.

## UI-5 Calendar + Reminders
Delivery calendar, day details, reminder configuration, notification permission UX.

## UI-6 Smart Import
Image selection, processing, OCR/offline state, Groq state, confidence review, correction and save.

## UI-7 Smart Parcel Status Assistance
Share intent UX, notification access explanation, shipment status suggestions with source/confidence, review/confirm/ignore, shipment timeline, Track Externally, status notification settings.

## UI-8 Insights + Advanced Order Lifecycle
Insights, returns/refunds, warranty, receipts and history.

## UI-9 Settings + Backup + About
Appearance, defaults, privacy, backup/restore/export, Groq configuration, About.

## UI-10 Polish & Regression
Accessibility, large fonts, dark mode, rotations/config changes, empty/error/offline/permission states, navigation regression, performance and screenshot QA.

Each phase follows IMPLEMENT -> TEST -> UI AUDIT -> REPAIR -> VERIFY. Do not advance with blockers.
