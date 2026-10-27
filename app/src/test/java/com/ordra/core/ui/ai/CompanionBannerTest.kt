package com.ordra.core.ui.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CompanionBannerTest {

    @Test
    fun `attention outranks everything else`() {
        val banner = resolveCompanionBanner(
            activeCount = 5,
            arrivingTodayCount = 2,
            attentionCount = 1,
            recentlyDeliveredCount = 1,
        )
        assertEquals(CompanionBannerKind.ATTENTION, banner.kind)
        assertEquals(1, banner.count)
        // Arrivals still surface as the secondary line.
        assertEquals(CompanionBannerKind.ARRIVING_TODAY, banner.secondaryKind)
        assertEquals(2, banner.secondaryCount)
    }

    @Test
    fun `attention without arrivals has no secondary line`() {
        val banner = resolveCompanionBanner(
            activeCount = 3,
            arrivingTodayCount = 0,
            attentionCount = 2,
            recentlyDeliveredCount = 0,
        )
        assertEquals(CompanionBannerKind.ATTENTION, banner.kind)
        assertNull(banner.secondaryKind)
    }

    @Test
    fun `arriving today beats delivered and active`() {
        val banner = resolveCompanionBanner(
            activeCount = 4,
            arrivingTodayCount = 1,
            attentionCount = 0,
            recentlyDeliveredCount = 2,
        )
        assertEquals(CompanionBannerKind.ARRIVING_TODAY, banner.kind)
        assertEquals(1, banner.count)
    }

    @Test
    fun `recent delivery shows when nothing needs attention or arrives`() {
        val banner = resolveCompanionBanner(
            activeCount = 3,
            arrivingTodayCount = 0,
            attentionCount = 0,
            recentlyDeliveredCount = 1,
        )
        assertEquals(CompanionBannerKind.DELIVERED, banner.kind)
    }

    @Test
    fun `active orders fall back to active message`() {
        val banner = resolveCompanionBanner(
            activeCount = 4,
            arrivingTodayCount = 0,
            attentionCount = 0,
            recentlyDeliveredCount = 0,
        )
        assertEquals(CompanionBannerKind.ACTIVE, banner.kind)
        assertEquals(4, banner.count)
    }

    @Test
    fun `no orders means empty state`() {
        val banner = resolveCompanionBanner(
            activeCount = 0,
            arrivingTodayCount = 0,
            attentionCount = 0,
            recentlyDeliveredCount = 0,
        )
        assertEquals(CompanionBannerKind.EMPTY, banner.kind)
    }
}
