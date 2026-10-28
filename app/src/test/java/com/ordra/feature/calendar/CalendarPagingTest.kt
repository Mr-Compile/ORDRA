package com.ordra.feature.calendar

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarPagingTest {

    private val anchor = YearMonth.of(2026, 3)

    @Test
    fun `center page maps to anchor month`() {
        assertEquals(anchor, monthForPage(anchor, CALENDAR_CENTER_PAGE))
    }

    @Test
    fun `adjacent pages map to adjacent months`() {
        assertEquals(
            YearMonth.of(2026, 4),
            monthForPage(anchor, CALENDAR_CENTER_PAGE + 1),
        )
        assertEquals(
            YearMonth.of(2026, 2),
            monthForPage(anchor, CALENDAR_CENTER_PAGE - 1),
        )
    }

    @Test
    fun `page boundary crosses year correctly`() {
        val dec = YearMonth.of(2025, 12)
        assertEquals(
            YearMonth.of(2026, 1),
            monthForPage(dec, CALENDAR_CENTER_PAGE + 1),
        )
        val jan = YearMonth.of(2026, 1)
        assertEquals(
            YearMonth.of(2025, 12),
            monthForPage(jan, CALENDAR_CENTER_PAGE - 1),
        )
    }

    @Test
    fun `far pages stay inside int bounds`() {
        val farFuture = monthForPage(anchor, CALENDAR_PAGER_COUNT - 1)
        assertEquals(anchor.plusMonths((CALENDAR_PAGER_COUNT - 1L - CALENDAR_CENTER_PAGE)), farFuture)
        val farPast = monthForPage(anchor, 0)
        assertEquals(anchor.plusMonths(-CALENDAR_CENTER_PAGE.toLong()), farPast)
    }

    @Test
    fun `pageForMonth inverts monthForPage`() {
        listOf(-13, -1, 0, 1, 25, 1200).forEach { delta ->
            val month = anchor.plusMonths(delta.toLong())
            val page = pageForMonth(anchor, month)
            assertEquals(month, monthForPage(anchor, page))
            assertEquals(CALENDAR_CENTER_PAGE + delta, page)
        }
    }
}
