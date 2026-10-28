package com.ordra.feature.calendar

import java.time.YearMonth

const val CALENDAR_PAGER_COUNT = Int.MAX_VALUE
const val CALENDAR_CENTER_PAGE = CALENDAR_PAGER_COUNT / 2

/**
 * Maps a HorizontalPager page index to a month relative to [anchor].
 * The pager is centered on the anchor month so swiping works in both
 * directions; page N corresponds to anchor + (N - center) months.
 */
fun monthForPage(anchor: YearMonth, page: Int): YearMonth =
    anchor.plusMonths(page.toLong() - CALENDAR_CENTER_PAGE)

/**
 * Inverse of [monthForPage] — used to keep arrows and programmatic
 * navigation on the same page/month mapping.
 */
fun pageForMonth(anchor: YearMonth, month: YearMonth): Int {
    val delta = (month.year - anchor.year) * 12L + (month.monthValue - anchor.monthValue)
    return (CALENDAR_CENTER_PAGE + delta).toInt()
}
