# ORDRA Design System

Canonical tokens for the ORDRA hybrid design system: 70% Minimal/Material, 20% Bento, 10%
glass/gradient/aurora. All values become reusable tokens — never hard-code colors, dp or
durations inside screens.

## Brand colors
- Ordra Indigo: #5B5FEF
- Ordra Violet: #7C5CFC
- Deep Indigo: #3F43C7
- Mint Accent: #35D0A0
- Warm Accent: #FFB547
- Error: #F05252
- Brand gradient: #5B5FEF -> #7C5CFC (branding/high emphasis only)
- Aurora accents (optional): soft radial blends of Violet + Mint at low alpha for
  splash/onboarding/hero/empty-state illustration areas only.

## Light theme
- background #F8F9FD
- surface #FFFFFF
- surfaceVariant #F0F1F8
- primary #5B5FEF
- secondary #7C5CFC
- onBackground #171821
- onSurfaceVariant #666979
- outline #DADCE8

## Dark theme
- background #101116
- surface #18191F
- surfaceVariant #212229
- primary #8588FF
- secondary #A48BFF
- onBackground #F5F5FA
- onSurfaceVariant #B2B3C1
- outline #343640
- Dark theme stays neutral and readable — no cyberpunk neon, no saturated fills.

> Token decision (2026-10-27): the design-direction spec suggested #F7F8FA/#111827 and
> #0D1117/#161B22/#F3F4F6 as *starting* tokens. Ordra keeps the palette above because it is
> already implemented in `app/.../core/ui/theme/Color.kt`, `branding/theme_tokens.json` and
> `mockup/styles.css`, and meets the same intent (soft neutral, near-white surfaces).
> Recorded in `docs/plan/MEGAPLAN_UIUX.md` → Conflicts.

## Unified ORDRA status system
Two status vocabularies, both canonical from `docs/contracts/DOMAIN_MODELS.md`. UI maps every
status to a `StatusChip` with **icon + text + semantic color** — never color alone, never a
full-card fill.

OrderStatus (purchase lifecycle): DRAFT, ORDERED, PROCESSING, PACKED, SHIPPED, DELIVERED,
CANCELLED, RETURNED, REFUNDED.
ShipmentStatus (parcel movement): UNKNOWN, PREPARING, PICKED_UP, IN_TRANSIT, ARRIVED_AT_HUB,
OUT_FOR_DELIVERY, DELIVERED, DELIVERY_FAILED, RETURNING, RETURNED.

Color mapping:
- Ordered/Draft: Indigo · Processing: Violet · Packed: Blue (#3B82F6) · Shipped: Cyan (#06B6D4)
- Delivered: Mint/Green · Cancelled: Red · Returned: Orange (#F97316) · Refunded: Teal (#14B8A6)
- Shipment: Preparing/Picked Up: Violet · In Transit: Cyan · Arrived at Hub: Blue ·
  Out for Delivery: Amber · Delivered: Mint · Delivery Failed/Returning: Warm accent ·
  Returned: Orange · Unknown: neutral/outline

Spec-name mapping (design-direction wording -> canonical enums): "Order Placed" -> ORDERED;
"Delayed" is a *derived presentation state* (ETA window passed + not delivered, or
DELIVERY_FAILED context) — no new enum; "Delivery Attempt Failed" -> DELIVERY_FAILED;
"Returned" -> RETURNED; "Cancelled" -> CANCELLED (OrderStatus); "Tracking Issue"/"Unknown" ->
UNKNOWN or low-confidence surface. Do not add domain enums from UI specs.

Icon pairing (Material Symbols Rounded): Ordered `receipt_long` · Preparing `inventory_2` ·
Picked Up / Shipped `local_shipping` · In Transit `route` · Arrived at Hub `warehouse` ·
Out for Delivery `local_shipping` (+ fill emphasis) · Delivered `check_circle` ·
Delivery Failed / Delayed `error` / `schedule` · Returning / Returned `assignment_return` ·
Cancelled `cancel` · Refunded `currency_exchange` · Unknown / Tracking Issue `help`.

Status styling is identical across dashboard, order lists, parcel cards, search results,
tracking detail, notifications, activity and history.

## Marketplace vs courier
"Marketplace/source" = canonical `Platform` (SHOPEE, TIKTOK_SHOP, LAZADA, TEMU, AMAZON,
FACEBOOK, INSTAGRAM, OTHER). `Courier` = the parcel carrier (SPX_EXPRESS, JNT_EXPRESS,
NINJA_VAN, FLASH_EXPRESS, LBC, JRS_EXPRESS, GOGO_XPRESS, ENTREGO, TWO_GO, DHL, OTHER, UNKNOWN).

- `PlatformBadge`: small tonal badge with platform label (and optional glyph later). Suggested
  tonal tints, used sparingly and never dominant: Shopee orange, Lazada blue, TikTok dark,
  Temu orange, Amazon dark — all at chip/badge scale only. Do not bundle marketplace logos
  (`docs/specs/ASSET_MANIFEST.md`); text labels are sufficient for v1.
- `CourierBadge`: neutral outlined chip, courier name only; always visible on parcel detail
  and tracking screens.
- Never present a marketplace as a courier or a courier as a marketplace. Never treat
  marketplace names as statuses.

## Typography
Material 3 type scale, system sans-serif. Display -> rare hero numbers (COD totals);
Headline -> screen summaries; Title -> card titles/section headers; Body -> content;
Label -> chips, badges, timestamps, metadata. Respect system font scaling; nothing clips at
large font sizes.

## Spacing
4dp base grid. Tokens: 4 / 8 / 12 / 16 / 20 / 24 / 32. Screen horizontal padding: 16dp
compact, 24dp on medium+ window sizes. Card internal padding 16dp (compact) / 20dp (featured).

## Radius & shape
- small: 8dp (chips, small controls)
- medium: 12dp (inputs, buttons, small cards)
- large: 16dp (standard cards, sheets' content)
- xl: 20–24dp (featured/Bento cards, prominent surfaces)
- Bottom sheets/dialogs: Material 3 defaults with consistent large top corners.
Avoid excessive pill shapes outside chips/buttons.

## Elevation & surfaces
Tonal/surface separation first; subtle borders over heavy shadows. Reserved elevation only for
floating controls, sheets, dialogs and cards that need hierarchy. No nested cards inside
cards; no large decorative shadows.

## Glass surfaces
`GlassSurface` token = `surface` at ~80–88% alpha + `outline` at ~20% alpha, with blur where
cheap. Allowed locations only: floating bottom navigation, floating controls, modal overlays,
bottom sheets, quick-action surfaces, map-style controls, selected premium summary elements.
Rules: never behind dense text or tracking data; always keep contrast legal; provide a solid
tonal fallback when blur is unavailable or expensive; respect reduced-transparency/motion
settings by falling back to solid.

## Motion tokens
- micro: 100–150ms (press feedback, chip state)
- standard: 200–300ms (transitions, sheet open, status change)
- emphasis: <=400ms (progress advance, delivered confirmation)
- easing: Material standard decelerate; no bounce for routine tasks
- reduced-motion: replace movement with instant or fade; a state change must still be visible
  without animation (color/icon/label must carry it).

## Breakpoints (Android window size classes — Android only)
- compact (<600dp): bottom nav, single-column lists/cards, one-handed reach priority.
- medium (600–840dp): NavigationRail, up to 2-column Bento on Home/Insights.
- expanded (>840dp): NavigationRail/sidebar, multi-column Bento, optional split
  list+detail on Orders.
Do not stretch the phone layout; do not add desktop/web targets (`docs/RULES.md`).

## Loading, empty, error building blocks
- `SkeletonCard`/`LoadingSkeleton` shapes mirror the real layouts (dashboard, parcel card,
  orders list, tracking detail, timeline, activity). Static or subtle shimmer; shimmer off
  under reduced motion.
- `EmptyState`: icon/illustration + short title + one-line guidance + single primary action.
- `ErrorState`: friendly message mapped from the error taxonomy
  (`docs/contracts/ERROR_VALIDATION_CONTRACT.md`) + recovery action. Never raw exceptions.

## Iconography
Material Symbols Rounded everywhere; custom SVG only for the Ordra mark/brand illustration.
No emoji as production icons. Informative icons get content descriptions; decorative icons
stay silent.

## AI / companion tokens
Live in `core/ui/ai/Companion.kt` → `CompanionTokens` (keep values there, not in screens):
- Glass surface alpha: 0.80 light / 0.58 dark; border alpha 0.24; aurora alpha 0.14 light /
  0.20 dark (violet top-end + mint bottom-end radial glows inside `OrdraAiBanner`).
- Motion: breathe 2600ms, dot pulse 1500ms, working sweep 1800ms — all gated by the system
  animator-duration scale (reduced motion → static).
- Companion sizes come from `CompanionVariant`; the banner shrinks Ordi to 72dp below
  ~380dp width.
- AI surfaces stay in the ~10% accent layer: translucent surface + thin outline + restrained
  aurora + soft indigo shadow. Text stays fully opaque; status is always icon+text.
