package com.ordra.domain.service

import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.Platform
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Deterministic spending analytics computed from local orders.
 * Money rules (explicit):
 * - Spending counts orders that are NOT CANCELLED or REFUNDED.
 * - Delivered spend = DELIVERED orders; pending spend = active non-delivered.
 * - COD outstanding = isCod + not paid + not delivered/cancelled/refunded.
 */
object SpendingAnalytics {

    private val EXCLUDED = setOf(OrderStatus.CANCELLED, OrderStatus.REFUNDED)

    fun spendable(orders: List<Order>): List<Order> =
        orders.filter { it.status !in EXCLUDED && it.totalAmountMinor != null }

    fun totalSpend(orders: List<Order>): Long =
        spendable(orders).sumOf { it.totalAmountMinor ?: 0L }

    fun deliveredSpend(orders: List<Order>): Long =
        orders.filter { it.status == OrderStatus.DELIVERED }
            .sumOf { it.totalAmountMinor ?: 0L }

    fun pendingSpend(orders: List<Order>): Long =
        orders.filter { it.status !in EXCLUDED + OrderStatus.DELIVERED }
            .sumOf { it.totalAmountMinor ?: 0L }

    fun codOutstanding(orders: List<Order>): Long =
        orders.filter {
            it.isCod &&
                it.status !in EXCLUDED + OrderStatus.DELIVERED + OrderStatus.RETURNED
        }.sumOf { it.totalAmountMinor ?: 0L }

    fun platformTotals(orders: List<Order>): Map<Platform, Long> =
        spendable(orders).groupingBy { it.platform }
            .fold(0L) { acc, o -> acc + (o.totalAmountMinor ?: 0L) }

    private fun Order.orderedLocalDate(): LocalDate? =
        orderedDate?.atZone(ZoneId.systemDefault())?.toLocalDate()

    fun monthlyTotals(orders: List<Order>): List<Pair<YearMonth, Long>> =
        spendable(orders)
            .mapNotNull { o -> o.orderedLocalDate()?.let { YearMonth.from(it) to (o.totalAmountMinor ?: 0L) } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, v) -> v.sum() }
            .entries.sortedByDescending { it.key }
            .map { it.key to it.value }

    fun weeklyTotal(orders: List<Order>, weekStart: LocalDate = LocalDate.now()): Long {
        val start = weekStart.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
        return spendable(orders).filter {
            val d = it.orderedLocalDate()
            d != null && !d.isBefore(start) && !d.isAfter(start.plusDays(6))
        }.sumOf { it.totalAmountMinor ?: 0L }
    }

    fun averageOrderValue(orders: List<Order>): Long {
        val valid = spendable(orders)
        return if (valid.isEmpty()) 0L else totalSpend(valid) / valid.size
    }
}
