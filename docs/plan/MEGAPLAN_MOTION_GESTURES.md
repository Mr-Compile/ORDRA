# Ordra Motion & Gestures Megaplan

Exhaustive execution plan for the gesture + animation track (UI-10 polish items:
"microinteractions per `docs/uiux/MOTION_AND_FEEDBACK.md`, reduced-motion fallbacks,
skeleton loading"). One feature = one commit, pushed to `origin main`.

## Audit baseline (2026-10-28)

Animations present before this track: `core/ui/ai/Companion.kt` infinite loops only
(breathe/pulse/sweep, correctly gated by system animator scale). Everything else
swapped instantly — no nav transitions, no step transitions, no list item motion,
no status-chip color transition, no skeletons, no snackbars, no delivered moment,
no motion tokens in code.

Gestures present before this track: `HorizontalPager` in Onboarding and the
editor's `BackHandler` isDirty guard only. No swipe actions, no long-press menus,
no haptics, no predictive-back flag.

Out of scope (owner decisions, 2026-10-28): pull-to-refresh (no real refresh
operation — local-first live Flow), detection-card swipes, canonical bottom nav /
Add-Scan sheet (separate nav-shell change).

## Rules

- Token durations only (`core/ui/theme/Motion.kt`): micro 100–150ms, standard
  200–300ms, emphasis <=400ms, standard decelerate; no decorative animation.
- Reduced motion → fade/instant; state still conveyed by color/icon/label.
- Destructive gestures confirm or undo — swipe actions ship with undo snackbar.
- `mockup/` syncs in the same track (drift = defect; batch-sync commit precedent).
- Stage only this track's files per commit — never sweep in-flight working-tree
  changes into feature commits.

## Status key

DONE / IN PROGRESS / PENDING / BLOCKED

| WS | Scope | Status | Notes |
|---|---|---|---|
| M0 | Motion foundation | IN PROGRESS | `OrdraMotion` tokens, shared `rememberReducedMotion`, this doc |
| M1 | Screen/step transitions + predictive back | PENDING | NavHost slide/fade, AnimatedContent on Smart Import + Editor steps, `enableOnBackInvokedCallback` |
| M2 | List & status micro-motion | PENDING | `Modifier.animateItem()` on all keyed lists; StatusChip `animateColorAsState` |
| M3 | Swipe order cards | PENDING | `SwipeToDismissBox`: left=archive, right=mark delivered (restore on archived list); undo snackbar; VM actions; `SwipeActionTest` |
| M4 | Long-press quick actions | PENDING | `combinedClickable` + `OrderActionsSheet` ModalBottomSheet: Open / Mark delivered / Archive-Restore |
| M5 | Calendar month swipe | PENDING | `HorizontalPager` centered paging; `monthForPage` + `CalendarPageTest` |
| M6 | Delivered confirmation moment | PENDING | Mint check scale+fade overlay <=400ms + haptic; fired from detail dialog, swipe, and sheet |
| M7 | Loading skeletons | PENDING | Static `SkeletonCard`s on Home/Orders/Detail/Timeline (no shimmer) |
| M8 | Mockup sync + docs + QA evidence | PENDING | CSS motion tokens, drag/long-press affordances, delivered overlay, `MOTION_AND_FEEDBACK.md` additions, `docs/qa/MOTION_GESTURE_EVIDENCE.md` |

## Acceptance

All workstreams DONE; `assembleDebug` + `testDebugUnitTest` green; motion tokens
are the only duration source; reduced-motion verified per code path; mockup and
`docs/uiux/MOTION_AND_FEEDBACK.md` reflect shipped behavior; QA evidence filed.
