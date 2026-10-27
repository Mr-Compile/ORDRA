# UI-10 Polish / Regression — Code-Level Audit — 2026-10-27

Device regression pass is owner-side; this documents the code-level checks.

## PASS
- **Dark/light** — `OrdraDarkColors` + `OrdraLightColors` M3 schemes;
  `ThemeMode` setting (SYSTEM/LIGHT/DARK) consumed by `OrdraApp`.
- **Accessibility** — 39 `contentDescription` usages across features; the 17
  `contentDescription = null` are decorative icons (correct M3 practice).
  Icon-only actions all expose descriptions.
- **State restoration** — ViewModels use `SavedStateHandle` (orderId) and
  `stateIn` flows; form state survives config change via VM retention.
- **Color-independent info** — Analytics breakdowns label values + bars
  (readable without color); StatusChips pair text with color.
- **Reduced motion** — Ordi banner respects reduced-motion (mockup parity);
  no long-running infinite animations elsewhere.

## Open (device-required)
- TalkBack walkthrough, font-scale stress, RTL mirror, performance profile.
  These need a physical/emulator pass — tracked under Phase 10 owner QA.
