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

Final UI acceptance additionally checks every screen in docs/uiux/SCREEN_SPECS.md is either implemented or explicitly marked out-of-scope by owner approval. Coverage is tracked in docs/qa/SCREEN_COVERAGE_MATRIX.md.
