# Motion & Gesture QA Evidence — 2026-10-28

Code-level evidence for the gestures + animation workstreams in
`docs/plan/MEGAPLAN_MOTION_GESTURES.md`. Device walkthrough remains owner-side.

## PASS

- **Motion tokens** — single source in `core/ui/theme/Motion.kt`
  (micro 120 / standard 250 / emphasis 350, decelerate/accelerate easings);
  `OrdraThemeTokensTest` covers the object; grep shows no feature file
  hardcodes ms durations outside `Companion.kt`'s documented loops.
- **Reduced motion** — shared `rememberReducedMotion()` in
  `core/ui/ReducedMotion.kt` reads `ANIMATOR_DURATION_SCALE`; chip color
  animation snaps, delivered overlay uses static fade, Ordi loops unchanged.
- **Navigation/step transitions** — `OrdraNavHost` slide+fade with
  reduced-motion fade fallback; Smart Import and editor step swaps use
  `AnimatedContent`; `enableOnBackInvokedCallback` set in manifest.
- **List animation** — `animateItem()` on every keyed `items()` block
  (Home upcoming/recent/archived, Orders, COD groups, Detections, Timeline,
  Calendar day rows); `StatusChip` container/content colors animate via
  `animateColorAsState` with the standard token.
- **Swipe actions** — `SwipeableOrderCard` on all order-card lists;
  vetoed dismiss + Room-driven removal + snackbar Undo with real reverse
  ops (archive↔restore, delivered→previous status incl. deliveredAt clear).
  No destructive swipe action exists. `OrderSwipeTest` covers the action
  matrix incl. terminal-status veto and archived-direction rules.
- **Long-press quick actions** — `combinedClickable` exposes the long-press
  custom action to TalkBack; `OrderActionsSheet` offers Open plus the same
  context-valid actions via the same ViewModel path.
- **Calendar month swipe** — `HorizontalPager` with deterministic
  `monthForPage`/`pageForMonth` mapping; `CalendarPagingTest` covers
  adjacent pages, year boundary, far-page bounds and round-trip.
- **Delivered confirmation** — overlay fires only on the success event,
  350ms emphasis scale+fade + Confirm haptic; haptic is supplementary
  (chip/snackbar still communicate state); reduced motion = static fade.
- **Skeletons** — static `SkeletonCard`/`SkeletonList` on Home, Orders,
  Order Detail, Shipment Timeline loading branches; no shimmer.
- **Accessibility** — swipe never the only path (long-press sheet);
  56dp sheet action rows; month arrows have content descriptions; status
  never color-only (chip keeps its label while animating).

## Verified by build

- `:app:compileDebugKotlin`, `:app:testDebugUnitTest`, `:app:assembleDebug`
  green after each milestone commit.

## Open (device-required)

- TalkBack swipe-alternative walkthrough, reduced-motion visual pass,
  gesture-vs-scroll conflict check on long lists, haptic feel on hardware.
