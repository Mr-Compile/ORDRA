package com.ordra.core.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette — branding/theme_tokens.json and BRANDING.md
val OrdraIndigo = Color(0xFF5B5FEF)
val OrdraViolet = Color(0xFF7C5CFC)
val OrdraDeepIndigo = Color(0xFF3F43C7)
val OrdraMint = Color(0xFF35D0A0)
val OrdraAmber = Color(0xFFFFB547)
val OrdraError = Color(0xFFF05252)

// Light theme tokens
val LightBackground = Color(0xFFF8F9FD)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F1F8)
val LightPrimary = OrdraIndigo
val LightPrimaryStrong = Color(0xFF4549CF)
val LightSecondary = OrdraViolet
val LightTextPrimary = Color(0xFF171821)
val LightTextSecondary = Color(0xFF666979)
val LightOutline = Color(0xFFDADCE8)

// Dark theme tokens
val DarkBackground = Color(0xFF101116)
val DarkSurface = Color(0xFF18191F)
val DarkSurfaceVariant = Color(0xFF212229)
val DarkPrimary = Color(0xFF8588FF)
val DarkSecondary = Color(0xFFA48BFF)
val DarkTextPrimary = Color(0xFFF5F5FA)
val DarkTextSecondary = Color(0xFFB2B3C1)
val DarkOutline = Color(0xFF343640)

// Status accents — used for chips/indicators, never full-card floods
val StatusOrdered = OrdraIndigo
val StatusProcessing = OrdraViolet
val StatusPacked = Color(0xFF3B82F6)
val StatusShipped = Color(0xFF06B6D4)
val StatusOutForDelivery = OrdraAmber
val StatusDelivered = OrdraMint
val StatusCancelled = OrdraError
val StatusReturned = Color(0xFFF97316)
val StatusRefunded = Color(0xFF14B8A6)
