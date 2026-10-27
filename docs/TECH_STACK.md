# Approved Tech Stack

## Android client
- Kotlin
- Jetpack Compose + Material 3
- MVVM + pragmatic Clean Architecture
- Navigation Compose
- Room / SQLite
- Hilt
- Kotlin Coroutines + Flow
- DataStore Preferences
- WorkManager
- Android Notification APIs
- NotificationListenerService (explicit optional permission; primary automatic parcel-status assistance source — supported marketplace, courier and messaging-app notifications via a source registry; messaging apps only through notifications, never SMS inbox reads)
- ML Kit Text Recognition for on-device OCR (smart import and shipment-status screenshot detection)
- Retrofit + OkHttp
- Kotlinx Serialization
- Coil
- JUnit + Android/Compose UI testing
- Gradle Kotlin DSL

## Optional online dependency
- Groq API for Smart Import interpretation enhancement. Direct app → Groq calls only; credential comes from local git-ignored config. Offline fallback to ML Kit OCR + manual review is mandatory.
- No courier APIs, third-party parcel-tracking APIs (AfterShip, 17TRACK, EasyPost, Shippo, Parcel Monitor, unofficial courier APIs), scraped tracking pages, or reverse-engineered marketplace APIs. Tracking stays local-first: marketplace notifications + OCR + shared content + manual updates. A `TrackingProvider` abstraction remains as an extension point for any future official integration (see `docs/contracts/TRACKING_CONTRACT.md`); it is not implemented in v1.

## Explicitly not selected
- Flutter
- React Native
- Firebase as a foundation dependency
- Any custom backend (Ktor/Node/Express/Laravel/Django/FastAPI/Supabase or similar)
- Any cloud database (Firestore, Realtime DB, MongoDB Atlas, remote SQL)
- Cloud sync or any account/auth system (login, registration, OAuth, remote profiles)

## Out of current scope / future consideration
- A Groq backend/proxy for secret protection — reassess only if Ordra is ever publicly distributed. Not a current requirement.
- Official courier TrackingProvider integrations — architecture supports them as extension points; not built in v1.
- Cloud sync/accounts — post-launch decision only, never assumed.
