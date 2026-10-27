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
