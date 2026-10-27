# ORDRA UI/UX QA Gate

For each implemented screen verify:
- Matches canonical feature/screen spec
- Light theme PASS
- Dark theme PASS
- Large font PASS
- 48dp targets PASS
- TalkBack semantics PASS where applicable
- Loading/content/empty/error states PASS
- Offline state PASS where applicable
- Permission-denied state PASS where applicable
- Back navigation PASS
- State survives expected lifecycle/config changes
- No raw exceptions/technical errors exposed
- No fake data left in production path
- No duplicated design components when canonical component exists
- Prices/dates/statuses formatted consistently
- Destructive actions protected
- Smart/AI results are editable/reviewable
- Core app remains usable without Groq/network

v1.1 design-direction checks:
- Visual formula respected: Bento only on dashboard/summary surfaces; glass only on floating
  chrome/overlays; gradients only on brand moments — never behind dense text or tracking data
- Every status renders as icon + text + semantic color (no color-only meaning)
- Marketplace and courier are visually and structurally distinct; neither is presented as a
  status
- Tracking detail answers "where is my parcel now?" in the first viewport
- Skeleton loaders match real layouts; no blank screens or fake progress
- Reduced-motion and readability fallbacks hold for glass/translucent surfaces
- Touch targets and focus states pass on every interactive control
- ORDRA AI companion/banner checks:
  - Banner message priority: attention > arriving today > delivered > active > empty
    (unit-tested in CompanionBannerTest)
  - Banner counts come only from real order/shipment data (no fabricated AI output)
  - Ordi decorative semantics: contentDescription set; state never the sole carrier of meaning
  - Reduced motion: Ordi static, banner content unchanged
  - Light + dark glass readable; missing companion asset degrades to icon/text surface
  - Ask ORDRA absent (no AI destination yet) — acceptable; must not fake a feature

Final UI acceptance additionally checks every screen in docs/uiux/SCREEN_SPECS.md is either
implemented or explicitly marked out-of-scope by owner approval. Coverage is tracked in
docs/qa/SCREEN_COVERAGE_MATRIX.md.
