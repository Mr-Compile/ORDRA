# ORDRA Accessibility

Accessibility is mandatory, not a polish item.

- Minimum interactive target: 48dp. Comfortable targets on compact widths; one-handed reach
  for primary actions.
- Support Android font scaling without clipped prices/statuses/actions.
- Never encode status or meaning by color alone; always pair with text/icon/semantics
  (every `StatusChip` = icon + label + color).
- Maintain WCAG-aware contrast using Material 3 roles; verify the actual rendered pairs in
  light and dark themes.
- TalkBack labels for informative icons, images, controls, status, price, and navigation.
  Decorative icons stay silent.
- Logical focus order follows visual hierarchy; every control reachable by keyboard/D-pad
  shows a visible focus state.
- Forms expose labels, errors, required state, and helper text semantically.
- Respect reduced-motion/system animator settings: no required animation carries meaning;
  every state change survives with animation off.
- `GlassSurface`/translucent chrome must stay readable over content; provide solid fallbacks
  and honor reduced-transparency cues where the platform exposes them.
- Charts/insights require textual summaries; never rely on chart visuals alone.
- Destructive actions confirm or undo; focus is managed after dialogs/sheets dismiss.
