package com.ordra.domain

import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import com.ordra.domain.service.OrdiResponder
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Test

class OrdiResponderTest {

    private val today = LocalDate.now()

    private fun order(
        title: String = "t",
        status: OrderStatus = OrderStatus.SHIPPED,
        total: Long? = 10000,
        isCod: Boolean = false,
        paid: PaymentStatus = PaymentStatus.PAID,
        eta: LocalDate? = null,
    ) = Order(
        id = java.util.UUID.randomUUID().toString(),
        title = title, platform = Platform.SHOPEE, status = status,
        paymentStatus = paid, isCod = isCod,
        totalAmountMinor = total,
        estimatedDeliveryStart = eta, estimatedDeliveryEnd = eta,
        createdAt = Instant.now(), updatedAt = Instant.now(),
    )

    private fun ctx(
        orders: List<Order> = emptyList(),
        pending: Int = 0,
    ) = OrdiResponder.Context(
        orders = orders,
        shipments = emptyMap(),
        pendingDetections = pending,
        currencyCode = "PHP",
        today = today,
        now = Instant.now(),
    )

    @Test
    fun `arriving lists orders by ETA`() {
        val c = ctx(
            listOf(
                order(title = "B soon", eta = today.plusDays(1)),
                order(title = "A today", eta = today),
            ),
        )
        val reply = OrdiResponder.reply("what's arriving?", c)
        assertTrue(reply.contains("A today"))
        assertTrue(reply.contains("B soon"))
        assertTrue(reply.contains("arriving today"))
    }

    @Test
    fun `arriving with no ETAs explains how to add one`() {
        val reply = OrdiResponder.reply("when is it coming", ctx(listOf(order())))
        assertTrue(reply.contains("No delivery dates"))
    }

    @Test
    fun `cod sums unpaid cod orders`() {
        val c = ctx(
            listOf(
                order(title = "Shoes", isCod = true, paid = PaymentStatus.UNPAID, total = 150000),
                order(title = "Bag", isCod = true, paid = PaymentStatus.UNPAID, total = 50000),
                order(title = "Paid cod", isCod = true, paid = PaymentStatus.PAID, total = 99900),
            ),
        )
        val reply = OrdiResponder.reply("cod to prepare", c)
        assertTrue(reply.contains("PHP 2,000"))
        assertTrue(reply.contains("2 parcels"))
        assertTrue(!reply.contains("Paid cod"))
    }

    @Test
    fun `late reports overdue order`() {
        val c = ctx(listOf(order(title = "Late one", eta = today.minusDays(2))))
        val reply = OrdiResponder.reply("anything late?", c)
        assertTrue(reply.contains("Late one"))
        assertTrue(reply.contains("not delivered"))
    }

    @Test
    fun `detections prompt opens review`() {
        val reply = OrdiResponder.reply("any updates?", ctx(pending = 3))
        assertTrue(reply.contains("3 detected updates"))
    }

    @Test
    fun `spend totals exclude cancelled`() {
        val c = ctx(
            listOf(
                order(total = 10000),
                order(status = OrderStatus.CANCELLED, total = 50000),
            ),
        )
        val reply = OrdiResponder.reply("what have I spent", c)
        assertTrue(reply.contains("PHP 100"))
    }

    @Test
    fun `fallback mentions pending detections when present`() {
        val reply = OrdiResponder.reply("blorp", ctx(pending = 1))
        assertTrue(reply.contains("detected update"))
    }

    @Test
    fun `blank prompt asks to retry`() {
        assertTrue(OrdiResponder.reply("  ", ctx()).contains("didn't catch"))
    }
}
