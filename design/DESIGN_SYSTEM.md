# Ordra Design System

> Note: superseded by `docs/uiux/DESIGN_SYSTEM.md` (UI/UX Pack v1), which is canonical.
> This file is kept for reference from the Frontend Pack.

## Colors
Light: background #F8F9FD, surface #FFFFFF, surfaceVariant #F0F1F8, primary #5B5FEF, secondary #7C5CFC, textPrimary #171821, textSecondary #666979, outline #DADCE8.
Dark: background #101116, surface #18191F, surfaceVariant #212229, primary #8588FF, secondary #A48BFF, textPrimary #F5F5FA, textSecondary #B2B3C1, outline #343640.
Semantic: success #35D0A0, warning #FFB547, error #F05252.

## Status mapping
Ordered=Indigo; Processing=Violet; Packed=Blue; Shipped=Cyan; OutForDelivery=Amber; Delivered=Mint; Cancelled=Red; Returned=Orange; Refunded=Teal. Use status color on chip/icon/accent, not whole card.

## Shape & spacing
Use Material 3 shapes with consistent small/medium/large tokens. Base spacing grid 4dp; common gaps 8/12/16/24/32dp. Touch targets >=48dp.

## Components
OrdraTopBar, OrdraBottomBar, OrderCard, StatusChip, PlatformBadge, MoneyText, CodBadge, EtaRow, EmptyState, ErrorState, LoadingState, StatCard, SectionHeader, SearchBar, FilterChipRow, OrderItemRow, PriceBreakdown, Timeline, AttachmentTile, ConfidenceField, PermissionExplainer, ConfirmationSheet.

## Motion
Subtle only: screen transitions, chip/state changes, scan progress. Respect reduced motion. Splash must not delay app startup.

## Iconography
Material Symbols where suitable; custom Ordra logo only for branding. Do not use emoji as production UI icons.
