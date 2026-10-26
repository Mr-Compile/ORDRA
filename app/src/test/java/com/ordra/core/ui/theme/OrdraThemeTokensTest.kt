package com.ordra.core.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OrdraThemeTokensTest {

    @Test
    fun `brand palette matches theme_tokens spec`() {
        assertEquals(Color(0xFF5B5FEF), OrdraIndigo)
        assertEquals(Color(0xFF7C5CFC), OrdraViolet)
        assertEquals(Color(0xFF3F43C7), OrdraDeepIndigo)
        assertEquals(Color(0xFF35D0A0), OrdraMint)
        assertEquals(Color(0xFFFFB547), OrdraAmber)
        assertEquals(Color(0xFFF05252), OrdraError)
    }

    @Test
    fun `light scheme uses branding tokens`() {
        assertEquals(Color(0xFF5B5FEF), OrdraLightColors.primary)
        assertEquals(Color(0xFF7C5CFC), OrdraLightColors.secondary)
        assertEquals(Color(0xFFF8F9FD), OrdraLightColors.background)
        assertEquals(Color(0xFFFFFFFF), OrdraLightColors.surface)
        assertEquals(Color(0xFFF0F1F8), OrdraLightColors.surfaceVariant)
        assertEquals(Color(0xFF171821), OrdraLightColors.onSurface)
        assertEquals(Color(0xFF666979), OrdraLightColors.onSurfaceVariant)
        assertEquals(Color(0xFFDADCE8), OrdraLightColors.outline)
        assertEquals(Color(0xFFF05252), OrdraLightColors.error)
    }

    @Test
    fun `dark scheme uses branding tokens`() {
        assertEquals(Color(0xFF8588FF), OrdraDarkColors.primary)
        assertEquals(Color(0xFFA48BFF), OrdraDarkColors.secondary)
        assertEquals(Color(0xFF101116), OrdraDarkColors.background)
        assertEquals(Color(0xFF18191F), OrdraDarkColors.surface)
        assertEquals(Color(0xFF212229), OrdraDarkColors.surfaceVariant)
        assertEquals(Color(0xFFF5F5FA), OrdraDarkColors.onSurface)
        assertEquals(Color(0xFFB2B3C1), OrdraDarkColors.onSurfaceVariant)
        assertEquals(Color(0xFF343640), OrdraDarkColors.outline)
    }

    @Test
    fun `light and dark schemes differ`() {
        assertNotEquals(OrdraLightColors.background, OrdraDarkColors.background)
        assertNotEquals(OrdraLightColors.surface, OrdraDarkColors.surface)
        assertNotEquals(OrdraLightColors.primary, OrdraDarkColors.primary)
    }
}
