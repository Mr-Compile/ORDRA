# ORDRA Branding in UI

Name: ORDRA
Tagline: All your orders. One place.
Meaning: inspired by Order + Organize.

Logo direction: simple O-based symbol that can suggest parcel/check/route through negative space. Must remain recognizable as an Android adaptive launcher icon. Do not use a generic detailed cardboard box illustration as the primary mark.

Splash: centered Ordra icon/wordmark, light/dark system-aware surface, subtle brand gradient allowed, no long animation.

Brand gradient (#5B5FEF -> #7C5CFC) and aurora accents are reserved for splash, onboarding,
empty-state illustration areas, hero surfaces and selected premium dashboard highlights —
the 10% layer of the visual formula. Routine order screens, dense text and tracking
information stay on clean Material surfaces for readability.

## Ordi — the ORDRA AI companion (implemented)
- Naming: ORDRA = product, ORDRA AI = intelligence layer, Ordi = visual companion. Copy
  uses `R.string` resources so the name can change in one place.
- Character: soft rounded parcel silhouette in the brand gradient, signal antenna, minimal
  expressive face — friendly, calm, professional; not a literal cardboard box, not a generic
  robot/mascot.
- Rendering: procedural Canvas art in `CompanionAvatar` today; the `painter` slot accepts
  final SVG/PNG/Lottie/Rive artwork later. Art direction for final assets: simple
  silhouette, readable at 40dp, light/dark safe, subtle gradients only.
- Personality ceiling: supporting character for a serious productivity app —
  functionality > readability > accessibility > brand personality > effects.
