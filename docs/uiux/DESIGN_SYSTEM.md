# ORDRA Design System

## Brand colors
- Ordra Indigo: #5B5FEF
- Ordra Violet: #7C5CFC
- Deep Indigo: #3F43C7
- Mint Accent: #35D0A0
- Warm Accent: #FFB547
- Error: #F05252
- Brand gradient: #5B5FEF -> #7C5CFC (branding/high emphasis only)

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

## Status semantics
Order status:
- Ordered: Indigo
- Processing: Violet
- Packed: Blue
- Shipped: Cyan
- Delivered: Mint/Green
- Cancelled: Error/Red
- Returned: Orange
- Refunded: Teal

Shipment status (parcel movement): Preparing/Picked Up: Violet family; In Transit: Cyan;
Arrived at Hub: Blue; Out for Delivery: Amber; Delivered: Mint/Green;
Delivery Failed/Returning: warm accent; Returned: Orange; Unknown: neutral/outline.

Use status color primarily on chip/icon/accent, never as a full-card background by default.

## Typography
Use Material 3 typography with Android system-compatible sans-serif. Recommended hierarchy: Display for rare hero numbers, Headline for screen summaries, Title for cards/sections, Body for content, Label for chips/metadata. Respect system font scaling.

## Spacing
4dp base grid. Common spacing: 4, 8, 12, 16, 20, 24, 32. Screen horizontal padding: 16dp compact, 24dp on wider layouts.

## Shape
- Small controls/chips: 8-12dp
- Inputs/buttons: 12-16dp
- Cards: 16-20dp
- Modal/bottom sheets: Material 3 defaults with consistent large top corners
Avoid excessive pill shapes.

## Elevation
Prefer tonal/surface separation. Use subtle elevation only for floating controls, sheets, dialogs, and cards requiring hierarchy.

## Iconography
Prefer Material Symbols Rounded/standard Android icons. Custom SVG only for Ordra logo, brand-specific illustration, or concepts Material does not cover. Icons require content descriptions when informative.
