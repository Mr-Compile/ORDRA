# Ordra Product Bible

## Product identity
Ordra is an **Android-only universal smart order and parcel manager**. It is not a courier company, marketplace, or full budgeting app.

## Core problem
Online shoppers may have orders spread across Shopee, TikTok Shop, Lazada, social sellers, websites, and local stores. Ordra provides one personal place to record what was ordered, how much it costs, whether it is COD or paid, when it may arrive, and what happened after delivery.

## Product promise
**Save it. Track it. Prepare for it.**

## Core principles
- Android first and Android only for the current product scope.
- Local-first: core order, shipment, reminder, analytics, backup, OCR and manual tracking features must work without internet.
- Platform-independent: one universal Order model; marketplace is data, not architecture.
- No-API tracking: Ordra does not use courier APIs, third-party parcel-tracking services, scraped courier websites, or reverse-engineered marketplace APIs. A tracking number is an identifier and matching signal — it is not a live status feed.
- User-confirmed intelligence: AI/OCR/notification detections are suggestions with confidence, reviewable before saving or changing important data.
- Privacy by design: screenshots and notifications may contain names, addresses, phone numbers, order IDs and payment details. Detected content is minimized and never persisted raw.
- Friendly UX: never expose raw technical errors to normal users.
- Progressive automation: Manual Entry → Screenshot Import → Share Import → Notification Assistance.

## Target users
- People who frequently shop from multiple online marketplaces.
- COD users who need to know how much cash to prepare.
- Users who want a searchable purchase history without opening multiple shopping apps.
- Users who want delivery reminders and lightweight shopping-spend visibility.

## Core order capabilities
Create, edit, view, search, filter, archive, restore and delete orders. Store one or more order items, platform, seller, price components, payment state, COD amount, order status, dates, images and notes. Each order may carry one or more shipments with courier, tracking number, shipment status and a tracking-event timeline.

## Universal statuses
OrderStatus (overall purchase lifecycle): DRAFT, ORDERED, PROCESSING, PACKED, SHIPPED, DELIVERED, CANCELLED, RETURNED, REFUNDED.
ShipmentStatus (physical parcel movement): UNKNOWN, PREPARING, PICKED_UP, IN_TRANSIT, ARRIVED_AT_HUB, OUT_FOR_DELIVERY, DELIVERED, DELIVERY_FAILED, RETURNING, RETURNED.
See `docs/contracts/DOMAIN_MODELS.md` and `docs/contracts/TRACKING_CONTRACT.md`.

## Supported platform values
SHOPEE, TIKTOK_SHOP, LAZADA, TEMU, AMAZON, FACEBOOK, INSTAGRAM, OTHER. `OTHER` must support a custom platform/store label.

## Smart Import vision
A user selects an order screenshot. On-device OCR extracts text. Local rules parse obvious fields. Optional online intelligence can interpret ambiguous text and return a structured OrderDraft. The user reviews/corrects the draft before saving.

## Smart Parcel Status vision
Ordra never queries courier APIs or third-party trackers and never scrapes tracking pages. A tracking number alone contains no live status. Parcel movement awareness instead comes from what is already on the user's device: supported marketplace, courier-app and messaging-app notifications (rider/delivery messages), on-device OCR of screenshots/imports, shared content, and manual updates. Deterministic local parsing runs first; ORDRA AI is an optional minimized fallback for ambiguous text. Detections are normalized to a canonical ShipmentStatus, carry a source and confidence, and land on a per-shipment timeline. High-confidence detections may auto-apply only when the user enabled it; uncertain or ambiguous detections ask first; low-confidence detections stay suggestions. Ordra never claims live or real-time courier tracking — these are detected parcel updates. See `docs/contracts/TRACKING_CONTRACT.md`.

## Groq rule
Groq is an optional intelligence layer, never the source of truth. Core app functions must not fail because Groq or internet access is unavailable. It is the only planned online service called directly by Ordra v1, used only for optional Smart Import enhancement; courier tracking APIs and third-party tracking services are not used. The personal version calls Groq directly from the app, with credentials held only in local git-ignored configuration. A production Groq secret must never be embedded in a distributed APK. (OUT OF CURRENT SCOPE / FUTURE CONSIDERATION: a controlled backend/proxy for secret protection, to be reassessed only if Ordra is ever publicly distributed.)

## Notification assistance vision
With explicit Android notification-access permission, a NotificationListenerService is Ordra's primary automatic status-assistance mechanism: it parses supported marketplace, courier-app and messaging-app notifications (including rider/delivery messages surfaced as notifications), matches them to local shipments through signal extraction and confidence scoring, and produces source/confidence-attributed status suggestions or events. Messaging apps are read only through their notifications — never the SMS inbox. Uncertain or ambiguous matches require user confirmation. Notification access is optional — Ordra works fully without it, and manual status updates always exist.

## Out of scope for early phases
- iOS
- Flutter
- Full financial/budgeting system
- Courier APIs and third-party parcel-tracking APIs (AfterShip, 17TRACK, EasyPost, Shippo, Parcel Monitor, unofficial courier APIs)
- Scraped courier tracking websites, browser automation against courier pages, reverse-engineered marketplace APIs
- Marketplace credential scraping
- Automated purchasing
- Cloud sync, cloud database, or any account system (login, registration, OAuth, remote profiles)
- Any custom backend (Ktor/Node/Express/Laravel/Django/FastAPI/Supabase/Firebase or similar)
- Production Groq proxy until needed

## Out of current scope / future consideration
These are not current requirements and must not be built without explicit owner approval: multi-device/cloud sync, a Groq proxy/backend for secret protection, any public multi-user architecture, and official courier TrackingProvider implementations (extension point only — see `docs/contracts/TRACKING_CONTRACT.md`).
