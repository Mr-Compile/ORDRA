# UI-0/F0 Token Audit — 2026-10-27

Checked `app/src/main/java/com/ordra/core/ui/theme/Color.kt` + `Theme.kt`
against `branding/theme_tokens.json` and `docs/uiux/DESIGN_SYSTEM.md`.

## Result: PASS

- All brand tokens present and byte-identical: primary #5B5FEF,
  secondary #7C5CFC, primaryDark #3F43C7, success #35D0A0,
  warning #FFB547, error #F05252.
- Light tokens: background #F8F9FD, surface #FFFFFF, surfaceVariant #F0F1F8.
- Dark scheme exists (`OrdraDarkColors`) wired via `Theme.kt` light/dark
  `ColorScheme`s; `ThemeMode` setting selects SYSTEM/LIGHT/DARK.
- Tokens are consumed through M3 `MaterialTheme.colorScheme` — no hardcoded
  hex inside feature screens (spot-check: detail, settings, analytics,
  detections screens all use colorScheme/typography tokens).

## Residual
- `theme_tokens.json` covers surface/state colors only; spacing/typography
  numeric tokens live in `Type.kt` + layout constants — acceptable per the
  design-system doc (M3 defaults + brand palette).
