# Privacy & Security Specification

Ordra may process screenshots containing names, addresses, phone numbers, tracking IDs and payment/order details. Minimize collection and keep local processing as the default.

Rules:
- No permanent production API secret inside APK. Secrets embedded in a distributed APK can never be considered secure — acceptable for personal use only; reassess before any public distribution.
- Groq credentials are never committed to Git, Kotlin source, XML, Markdown, tests, screenshots or logs. Personal/dev keys live only in local `secrets.properties` (git-ignored; template: `secrets.example.properties`).
- Never commit secrets or local config.
- Ask only for permissions needed at the moment they provide value.
- Notification access is optional and clearly explained.
- Prefer Photo Picker over broad media/storage access.
- Do not upload screenshots by default.
- Groq is the only planned online dependency. Smart Import sends only the minimum text fields needed for interpretation; minimize/redact names, addresses, phone numbers and unrelated screenshot content before transmission, and document what is sent.
- Do not log full screenshots, addresses, tokens, order IDs or other sensitive payloads in production logs.
- Permanent deletion must be explicit.
