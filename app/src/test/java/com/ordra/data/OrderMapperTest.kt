package com.ordra.data

import com.ordra.data.local.entity.OrderWithItems
import com.ordra.data.local.mapper.OrderMapper
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderItem
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.PaymentStatus
import com.ordra.domain.model.Platform
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderMapperTest {

    private fun sampleOrder() = Order(
        id = "order-1",
        title = "Soundcore R50i",
        platform = Platform.SHOPEE,
        customPlatformName = null,
        sellerName = "Anker Official",
        items = listOf(
            OrderItem(
                id = "item-1",
                orderId = "order-1",
                name = "Soundcore R50i",
                variant = "Black",
                quantity = 1,
                unitPriceMinor = 64900,
                lineTotalMinor = 64900,
            ),
        ),
        status = OrderStatus.SHIPPED,
        paymentMethod = PaymentMethod.COD,
        paymentStatus = PaymentStatus.UNPAID,
        isCod = true,
        currencyCode = "PHP",
        itemSubtotalMinor = 64900,
        shippingFeeMinor = 3800,
        discountMinor = 3800,
        totalAmountMinor = 64900,
        courier = "SPX",
        trackingNumber = "SPXPH123456",
        orderedDate = Instant.ofEpochMilli(1_700_000_000_000),
        estimatedDeliveryStart = LocalDate.of(2026, 10, 30),
        estimatedDeliveryEnd = LocalDate.of(2026, 11, 2),
        notes = "Leave at gate",
        productImageUri = "file:///img/p1.jpg",
        createdAt = Instant.ofEpochMilli(1_700_000_000_000),
        updatedAt = Instant.ofEpochMilli(1_700_100_000_000),
    )

    @Test
    fun `order round trips through entity without loss`() {
        val order = sampleOrder()
        val restored = OrderMapper.toDomain(
            OrderWithItems(
                order = OrderMapper.toEntity(order),
                items = order.items.map(OrderMapper::toEntity),
            ),
        )
        assertEquals(order, restored)
    }

    @Test
    fun `null and empty fields round trip`() {
        val order = sampleOrder().copy(
            customPlatformName = null,
            sellerName = null,
            items = emptyList(),
            itemSubtotalMinor = null,
            shippingFeeMinor = null,
            discountMinor = null,
            totalAmountMinor = null,
            courier = null,
            trackingNumber = null,
            orderedDate = null,
            estimatedDeliveryStart = null,
            estimatedDeliveryEnd = null,
            deliveredAt = null,
            notes = null,
            productImageUri = null,
        )
        val restored = OrderMapper.toDomain(
            OrderWithItems(OrderMapper.toEntity(order), emptyList()),
        )
        assertEquals(order, restored)
    }

    @Test
    fun `archivedAt maps to isArchived flag`() {
        val archived = sampleOrder().copy(archivedAt = Instant.ofEpochMilli(5_000))
        val restored = OrderMapper.toDomain(
            OrderWithItems(OrderMapper.toEntity(archived), emptyList()),
        )
        assertTrue(restored.isArchived)
        assertFalse(sampleOrder().isArchived)
    }

    @Test
    fun `item round trips through entity`() {
        val item = sampleOrder().items.first()
        val restored = OrderMapper.toDomain(OrderMapper.toEntity(item))
        assertEquals(item, restored)
    }
}
