package com.ordra.domain

import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import com.ordra.domain.service.SpendingAnalytics
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class SpendingAnalyticsTest {

    private fun order(
        status: OrderStatus = OrderStatus.DELIVERED,
        total: Long = 10000,
        isCod: Boolean = false,
        platform: Platform = Platform.SHOPEE,
        ordered: LocalDate? = LocalDate.now(),
    ) = Order(
        id = java.util.UUID.randomUUID().toString(),
        title = "t", platform = platform, customPlatformName = null,
        sellerName = null, status = status,
        paymentMethod = if (isCod) PaymentMethod.COD else PaymentMethod.CARD,
        paymentStatus = PaymentStatus.PAID, isCod = isCod,
        currencyCode = "PHP", itemSubtotalMinor = null, shippingFeeMinor = null,
        discountMinor = null, totalAmountMinor = total,
        orderedDate = ordered?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant(),
        estimatedDeliveryStart = null,
        estimatedDeliveryEnd = null, deliveredAt = null,
        notes = null, productImageUri = null,
        archivedAt = null, createdAt = java.time.Instant.now(),
        updatedAt = java.time.Instant.now(),
    )

    @Test
    fun `cancelled and refunded are excluded from spend`() {
        val orders = listOf(
            order(status = OrderStatus.DELIVERED, total = 10000),
            order(status = OrderStatus.CANCELLED, total = 50000),
            order(status = OrderStatus.REFUNDED, total = 50000),
        )
        assertEquals(10000L, SpendingAnalytics.totalSpend(orders))
    }

    @Test
    fun `pending vs delivered split`() {
        val orders = listOf(
            order(status = OrderStatus.DELIVERED, total = 40000),
            order(status = OrderStatus.SHIPPED, total = 10000),
        )
        assertEquals(40000L, SpendingAnalytics.deliveredSpend(orders))
        assertEquals(10000L, SpendingAnalytics.pendingSpend(orders))
    }

    @Test
    fun `cod outstanding excludes delivered`() {
        val orders = listOf(
            order(status = OrderStatus.SHIPPED, isCod = true, total = 20000),
            order(status = OrderStatus.DELIVERED, isCod = true, total = 30000),
        )
        assertEquals(20000L, SpendingAnalytics.codOutstanding(orders))
    }

    @Test
    fun `monthly totals group by ordered month`() {
        val orders = listOf(
            order(total = 10000, ordered = LocalDate.of(2026, 10, 5)),
            order(total = 5000, ordered = LocalDate.of(2026, 10, 20)),
            order(total = 2000, ordered = LocalDate.of(2026, 9, 1)),
        )
        val monthly = SpendingAnalytics.monthlyTotals(orders)
        assertEquals(YearMonth.of(2026, 10), monthly[0].first)
        assertEquals(15000L, monthly[0].second)
        assertEquals(2000L, monthly[1].second)
    }
}
