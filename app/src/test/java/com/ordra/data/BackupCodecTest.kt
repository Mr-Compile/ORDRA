package com.ordra.data

import com.ordra.data.backup.BackupCodec
import com.ordra.data.backup.BackupDocument
import com.ordra.data.local.entity.OrderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {

    private fun order() = OrderEntity(
        id = "o1", title = "Test", platform = "SHOPEE", customPlatformName = null,
        sellerName = null, status = "ORDERED", paymentMethod = "COD",
        paymentStatus = "UNPAID", isCod = true, currencyCode = "PHP",
        itemSubtotalMinor = null, shippingFeeMinor = null, discountMinor = null,
        totalAmountMinor = 1000, orderedDate = null, estimatedDeliveryStart = null,
        estimatedDeliveryEnd = null, deliveredAt = null, notes = null,
        productImageUri = null, rating = 4, isFavorite = true,
        archivedAt = null, createdAt = 1L, updatedAt = 1L,
    )

    @Test
    fun `round trip preserves entities`() {
        val doc = BackupDocument(exportedAt = 1L, orders = listOf(order()))
        val bytes = BackupCodec.encode(doc)
        val decoded = BackupCodec.decode(bytes)
        assertTrue(decoded is BackupCodec.DecodeResult.Ok)
        assertEquals("o1", (decoded as BackupCodec.DecodeResult.Ok).doc.orders[0].id)
        assertEquals(4, decoded.doc.orders[0].rating)
    }

    @Test
    fun `invalid bytes fail safely`() {
        val r = BackupCodec.decode("not json".toByteArray())
        assertTrue(r is BackupCodec.DecodeResult.Error)
    }

    @Test
    fun `newer schema version is rejected`() {
        val doc = BackupDocument(schemaVersion = 999, exportedAt = 1L)
        val r = BackupCodec.decode(BackupCodec.encode(doc))
        assertTrue(r is BackupCodec.DecodeResult.Error)
    }
}
