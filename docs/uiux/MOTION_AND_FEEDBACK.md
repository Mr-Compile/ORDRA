# ORDRA Motion & Feedback

Motion is functional, quick and purposeful. Use Material motion patterns and the duration
tokens in `docs/uiux/DESIGN_SYSTEM.md` (micro 100–150ms, standard 200–300ms, emphasis
<=400ms). Decorative animation that slows routine tasks is a defect; performance wins over
animation.

Approved microinteractions:
- Navigation: standard Compose/Material transitions; tab changes settle instantly.
- Tracking progress advancing and status-chip transitions (subtle, <=300ms).
- Parcel added / order saved confirmation (snackbar + card appearance).
- Delivered confirmation moment (the one allowed emphasis animation).
- Pull-to-refresh only where a real refresh operation exists.
- Bottom-sheet open/close; snackbar/Toast feedback; skeleton-to-content fade.
- Button/press feedback via standard ripple; optional restrained haptics for important
  confirmations — never required for understanding.
- Smart Import: real stage transitions; no fake progress percentages.

Reduced-motion settings replace movement with instant/fade equivalents; every state change
must still be communicated by color/icon/label when animation does not run.

## Implemented motion system (M0–M7, see `docs/plan/MEGAPLAN_MOTION_GESTURES.md`)

- Tokens live in `app/src/main/java/com/ordra/core/ui/theme/Motion.kt`
  (`OrdraMotion.MICRO_MS = 120`, `STANDARD_MS = 250`, `EMPHASIS_MS = 350`) with
  `microTween`/`standardTween`/`emphasisTween` helpers — no screen hardcodes durations.
- Reduced motion is gated by the shared `rememberReducedMotion()` in
  `core/ui/ReducedMotion.kt` (`Settings.Global.ANIMATOR_DURATION_SCALE <= 0`); all
  interactive motion snaps or fades instead.
- Navigation uses slide+fade transitions on `OrdraNavHost`; Smart Import steps and the
  order editor FORM↔REVIEW swap animate via `AnimatedContent`; predictive back is enabled
  in the manifest.
- Keyed lazy lists animate placement with `animateItem()` (Home, Orders, COD, Detections,
  Timeline, Calendar); `StatusChip` colors animate with the standard token and snap under
  reduced motion.
- Loading states show static `SkeletonCard`/`SkeletonList` placeholders — no shimmer.

## Implemented gestures

- Order cards swipe: → marks delivered (active, non-terminal statuses), ← archives;
  archived cards restore via →. The dismiss is vetoed so Room flow + item animation owns
  removal, failures leave the row in place, and every action offers a snackbar Undo.
- Order cards long-press opens a quick-actions `ModalBottomSheet` (Open / Mark delivered /
  Archive or Restore) using the same validity rules and action path — this is also the
  screen-reader route since swipe isn't directly triggerable by TalkBack.
- Delivery calendar swipes horizontally between months (`HorizontalPager`, deterministic
  `monthForPage`/`pageForMonth` mapping anchored at the current month); arrows animate the
  same pager so input modes stay in sync.
- Delivered confirmation: transient check overlay (emphasis-timed scale+fade, Confirm
  haptic) that only fires on a successful status write; static fade under reduced motion.

## Companion (Ordi) motion rules (implemented)
- Idle/greeting/delivery: 2.2px breathing bob on a 2600ms alternate loop — the only ambient
  motion.
- THINKING: staggered thinking dots pulse (1500ms); WORKING: mint orbit sweep around the
  antenna (1800ms). SUCCESS/ATTENTION use a static badge, no loop.
- OFFLINE suppresses breathing (asleep); reduced motion (`ANIMATOR_DURATION_SCALE=0`)
  disables all loops — faces/badges still communicate the state.
- Never: bouncing, particle effects, constant glow, motion required to convey meaning.
