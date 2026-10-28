# Parcel Image (order card thumbnail)

## Goal
Every parcel (order) can show a product image on its cards and detail header.
The image is optional: when none is set, a deterministic letter tile derived
from the parcel title renders instead.

## Data
- Storage already exists: `Order.productImageUri` (schema v6, `DOMAIN_MODELS.md`),
  persisted `content://` URI with `takePersistableUriPermission`.
- Picker already exists: Add/Edit Order → "Attachment" section (`ImagePickerField`).
- No migration, no schema change, no new persisted state.
- `OrderItem.imageUri` stays unused by this feature (no item picker exists).

## Component
New `ParcelImage` in `core/ui/components/`:

- Params: `imageUri: String?`, `name: String`, `modifier`, `size` (Dp).
- Base layer: letter tile — first letter of `name` (uppercased, Unicode-safe),
  on a background color selected deterministically from the name.
- Top layer: `coil3.compose.AsyncImage` (`ContentScale.Crop`, clipped to
  `MaterialTheme.shapes.small`-ish rounding) drawn over the tile when
  `imageUri != null`. Broken/revoked URIs and load failures fall back to the
  tile automatically because the tile is painted underneath.
- Decorative: `contentDescription = null` (the title is always adjacent text).

## Deterministic tile derivation
Pure functions, unit-testable, no Android deps:

- `initialFor(name: String): String?` — first letter of the trimmed name,
  uppercased; `null` when blank. When null, the tile shows a package icon
  (`Icons.Outlined.Inventory2`) instead of a letter.
- `tileHueFor(name: String): Int` — `name.hashCode()` mapped onto a fixed
  ~8-hue list aligned with the brand palette (indigo, violet, blue, cyan,
  mint, amber, orange, red).
- Tile colors are generated with `Color.hsl` per theme:
  - Light: background HSL(h, 65%, 88%), letter HSL(h, 60%, 38%).
  - Dark:  background HSL(h, 40%, 28%), letter HSL(h, 70%, 82%).

## Placement
- `OrderCard`: 44dp tile, left of the title column. Covers Home (recent +
  archived), Orders list, Delivery Calendar, COD Overview — all use `OrderCard`.
- `OrderDetailScreen` header card: same component at ~72dp beside the title.

## Mockup mirror (RULES §37)
- `mockup/app.js`: add `<span class="order-thumb">` to `orderCard()` and the
  featured ParcelCard; JS twin of the same hash→hue + initial logic; an `img`
  child when the mock order carries an image URL.
- `mockup/styles.css`: `.order-thumb` sizing/rounding + letter-tile layout.

## Tests
- Unit test the derivation: same name → same hue, in-bounds index, blank →
  fallback initial, distinct-ish names → exercise hue mapping.
- Existing Room/mapper tests untouched (no model change).

## Out of scope
- Camera capture, image editing/cropping, remote image URLs, item-level
  images, image storage in backup export (URIs are device-local already).
