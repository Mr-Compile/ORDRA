# Ordra Product Bible

## Product identity
Ordra is an **Android-only universal smart order and parcel manager**. It is not a courier company, marketplace, or full budgeting app.

## Core problem
Online shoppers may have orders spread across Shopee, TikTok Shop, Lazada, social sellers, websites, and local stores. Ordra provides one personal place to record what was ordered, how much it costs, whether it is COD or paid, when it may arrive, and what happened after delivery.

## Product promise
**Save it. Track it. Prepare for it.**

## Core principles
- Android first and Android only for the current product scope.
- Local-first: core order management must work without internet.
- Platform-independent: one universal Order model; marketplace is data, not architecture.
- User-confirmed intelligence: AI/OCR suggestions must be reviewable before saving or changing important data.
- Privacy by design: screenshots may contain names, addresses, phone numbers, order IDs and payment details.
- Friendly UX: never expose raw technical errors to normal users.
- Progressive automation: Manual Entry → Screenshot Import → Share Import → Notification Assistance.

## Target users
- People who frequently shop from multiple online marketplaces.
- COD users who need to know how much cash to prepare.
- Users who want a searchable purchase history without opening multiple shopping apps.
- Users who want delivery reminders and lightweight shopping-spend visibility.

## Core order capabilities
Create, edit, view, search, filter, archive, restore and delete orders. Store one or more order items, platform, seller, price components, payment state, COD amount, courier, tracking number, order status, dates, images and notes.

## Universal statuses
DRAFT, ORDERED, PROCESSING, PACKED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, RETURNED, REFUNDED.

## Supported platform values
SHOPEE, TIKTOK_SHOP, LAZADA, TEMU, AMAZON, FACEBOOK, INSTAGRAM, OTHER. `OTHER` must support a custom platform/store label.

## Smart Import vision
A user selects an order screenshot. On-device OCR extracts text. Local rules parse obvious fields. Optional online intelligence can interpret ambiguous text and return a structured OrderDraft. The user reviews/corrects the draft before saving.

## Groq rule
Groq is an optional intelligence layer, never the source of truth. Core app functions must not fail because Groq or internet access is unavailable. It is also the only planned online dependency: the personal version calls Groq directly from the app, with credentials held only in local git-ignored configuration. A production Groq secret must never be embedded in a distributed APK. (OUT OF CURRENT SCOPE / FUTURE CONSIDERATION: a controlled backend/proxy for secret protection, to be reassessed only if Ordra is ever publicly distributed.)

## Notification assistance vision
With explicit Android notification-access permission, later phases may parse supported shopping-app notifications and suggest a status update. Uncertain matches require user confirmation. Do not silently modify orders based on ambiguous notifications.

## Out of scope for early phases
- iOS
- Flutter
- Full financial/budgeting system
- Marketplace credential scraping
- Automated purchasing
- Cloud sync, cloud database, or any account system (login, registration, OAuth, remote profiles)
- Any custom backend (Ktor/Node/Express/Laravel/Django/FastAPI/Supabase/Firebase or similar)
- Production Groq proxy until needed

## Out of current scope / future consideration
These are not current requirements and must not be built without explicit owner approval: multi-device/cloud sync, a Groq proxy/backend for secret protection, and any public multi-user architecture.
