package com.ordra.feature.orderedit

import com.ordra.core.common.MoneyFormat
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderFormTest {

    @Test
    fun `money parsing is decimal safe`() {
        assertEquals(64900L, MoneyFormat.parseToMinor("649"))
        assertEquals(64900L, MoneyFormat.parseToMinor("649.00"))
        assertEquals(64950L, MoneyFormat.parseToMinor("649.5"))
        assertEquals(64955L, MoneyFormat.parseToMinor("649.549"))
        assertEquals(125001L, MoneyFormat.parseToMinor("1,250.01"))
        assertNull(MoneyFormat.parseToMinor("abc"))
        assertNull(MoneyFormat.parseToMinor(""))
    }

    @Test
    fun `minimum required fields are name platform and total`() {
        val form = OrderFormState(title = "", platform = null, total = "")
        val errors = form.fieldErrors()
        assertTrue(errors.containsKey("title"))
        assertTrue(errors.containsKey("platform"))
        assertTrue(errors.containsKey("total"))

        val valid = form.copy(
            platform = Platform.SHOPEE,
            total = "649",
            items = listOf(OrderItemForm(name = "Earbuds")),
        )
        assertTrue(valid.isValid())
    }

    @Test
    fun `custom platform requires name when OTHER`() {
        val form = OrderFormState(
            platform = Platform.OTHER,
            total = "100",
            title = "X",
        )
        assertTrue(form.fieldErrors().containsKey("customPlatformName"))
        assertFalse(form.copy(customPlatformName = "Local store").fieldErrors()
            .containsKey("customPlatformName"))
    }

    @Test
    fun `eta end before start fails validation`() {
        val form = OrderFormState(
            title = "x",
            platform = Platform.SHOPEE,
            total = "1",
            etaStart = LocalDate.of(2026, 11, 10),
            etaEnd = LocalDate.of(2026, 11, 1),
        )
        assertTrue(form.fieldErrors().containsKey("etaEnd"))
    }

    @Test
    fun `completely empty item rows are dropped on save`() {
        val form = OrderFormState(
            title = "Order",
            platform = Platform.SHOPEE,
            total = "10",
            items = listOf(
                OrderItemForm(name = "Real item", quantity = "2", unitPrice = "5"),
                OrderItemForm(),
            ),
        )
        val order = form.toOrder(existing = null, now = Instant.ofEpochMilli(1))
        assertEquals(1, order.items.size)
        assertEquals("Real item", order.items[0].name)
        assertEquals(1000L, order.items[0].lineTotalMinor)
    }

    @Test
    fun `cod flag derives from payment method`() {
        val form = OrderFormState(
            title = "x",
            platform = Platform.SHOPEE,
            total = "10",
            paymentMethod = PaymentMethod.COD,
        )
        assertTrue(form.toOrder(null, Instant.ofEpochMilli(1)).isCod)
        assertFalse(
            form.copy(paymentMethod = PaymentMethod.EWALLET)
                .toOrder(null, Instant.ofEpochMilli(1)).isCod,
        )
    }

    @Test
    fun `subtotal and suggested total compute correctly`() {
        val form = OrderFormState(
            items = listOf(
                OrderItemForm(name = "a", quantity = "2", unitPrice = "100"),
                OrderItemForm(name = "b", quantity = "1", unitPrice = "50"),
            ),
            shipping = "30",
            discount = "20",
        )
        assertEquals(25000L, form.itemsSubtotalMinor)
        assertEquals(26000L, form.suggestedTotalMinor)
    }

    @Test
    fun `blank title falls back to first item name`() {
        val order = OrderFormState(
            title = "  ",
            platform = Platform.LAZADA,
            total = "10",
            items = listOf(OrderItemForm(name = "Mouse")),
        ).toOrder(null, Instant.ofEpochMilli(1))
        assertEquals("Mouse", order.title)
    }

    @Test
    fun `new order items bind to the generated order id`() {
        val order = OrderFormState(
            title = "x",
            platform = Platform.SHOPEE,
            total = "10",
            items = listOf(
                OrderItemForm(name = "a"),
                OrderItemForm(name = "b", quantity = "2"),
            ),
        ).toOrder(existing = null, now = Instant.ofEpochMilli(1))
        assertTrue(order.items.isNotEmpty())
        order.items.forEach { assertEquals(order.id, it.orderId) }
    }
}
