# Privacy & Security Specification

Ordra may process screenshots containing names, addresses, phone numbers, tracking IDs and payment/order details. Marketplace notifications processed by the notification listener contain the same kinds of personal data. Minimize collection and keep local processing as the default.

Rules:
- No permanent production API secret inside APK. Secrets embedded in a distributed APK can never be considered secure — acceptable for personal use only; reassess before any public distribution.
- Groq credentials are never committed to Git, Kotlin source, XML, Markdown, tests, screenshots or logs. Personal/dev keys live only in local `secrets.properties` (git-ignored; template: `secrets.example.properties`).
- Never commit secrets or local config.
- Ask only for permissions needed at the moment they provide value.
- Notification access is optional and clearly explained before sending the user to Android settings.
- Rider/message notification detection gets its own explicit opt-in explainer before enabling: Ordra may inspect message notifications for delivery-related information; unrelated messages are dropped by the detection pipeline and never stored. Never enable it silently.
- Messaging apps are read through their Android notifications only. Ordra does not read the SMS inbox; direct inbox access would be a separate capability requiring its own privacy, permission, platform-policy and distribution review.
- Do not persist raw notification bodies or full raw OCR text. Tracking detections store parsed fields (status, courier, tracking number, timestamps, source, confidence) plus minimal provenance (`rawStatus`/`sourceDetail`/`rawReference` pointers).
- AI fallback for ambiguous notification text sends only the minimized candidate text needed for classification/extraction — never notification history, conversation history, contacts, phone numbers, addresses, or other notifications.
- Ordra never scrapes courier tracking pages. "Track Externally" opens the courier's official page in the user's browser; nothing from that page is read back by Ordra.
- Prefer Photo Picker over broad media/storage access.
- Do not upload screenshots by default.
- Groq is the only planned online service. Smart Import sends only the minimum text fields needed for interpretation; minimize/redact names, addresses, phone numbers and unrelated screenshot content before transmission, and document what is sent. Courier/third-party tracking APIs are never contacted.
- Do not log full screenshots, addresses, tokens, order IDs or other sensitive payloads in production logs.
- Permanent deletion must be explicit.
