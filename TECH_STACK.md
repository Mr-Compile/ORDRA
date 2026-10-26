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
- NotificationListenerService (later phase, explicit permission)
- ML Kit Text Recognition for on-device OCR (smart import phase)
- Retrofit + OkHttp
- Kotlinx Serialization
- Coil
- JUnit + Android/Compose UI testing
- Gradle Kotlin DSL

## Optional online dependency
- Groq API for Smart Import extraction/normalization. Direct app → Groq calls only; credential comes from local git-ignored config. Offline fallback to ML Kit OCR + manual review is mandatory.

## Explicitly not selected
- Flutter
- React Native
- Firebase as a foundation dependency
- Any custom backend (Ktor/Node/Express/Laravel/Django/FastAPI/Supabase or similar)
- Any cloud database (Firestore, Realtime DB, MongoDB Atlas, remote SQL)
- Cloud sync or any account/auth system (login, registration, OAuth, remote profiles)

## Out of current scope / future consideration
- A Groq backend/proxy for secret protection — reassess only if Ordra is ever publicly distributed. Not a current requirement.
- Cloud sync/accounts — post-launch decision only, never assumed.
