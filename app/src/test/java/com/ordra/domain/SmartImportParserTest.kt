package com.ordra.domain

import com.ordra.domain.model.Courier
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.service.SmartImportParser
import com.ordra.domain.service.StatusNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartImportParserTest {

    @Test
    fun `detects shopee order with cod total and tracking`() {
        val text = """
            Shopee
            Soundcore R50i Wireless Earbuds
            Sold by Anker Official Store
            Total ₱649.00
            Cash on Delivery
            SPX Express
            SPXPH123456789
        """.trimIndent()
        val r = SmartImportParser.parse(text)
        assertEquals(Platform.SHOPEE, r.detectedPlatform)
        assertEquals(64900L, r.draft.totalMinor.value)
        assertEquals(PaymentMethod.COD, r.draft.paymentMethod.value)
        assertEquals(Courier.SPX_EXPRESS, r.draft.courier.value)
        assertEquals("SPXPH123456789", r.draft.trackingNumber.value)
        assertEquals("Soundcore R50i Wireless Earbuds", r.draft.title.value)
    }

    @Test
    fun `status normalizer maps common phrases`() {
        assertEquals(ShipmentStatus.DELIVERED, StatusNormalizer.normalize("Parcel delivered"))
        assertEquals(ShipmentStatus.OUT_FOR_DELIVERY, StatusNormalizer.normalize("Out for delivery"))
        assertEquals(ShipmentStatus.IN_TRANSIT, StatusNormalizer.normalize("In transit to hub"))
        assertEquals(ShipmentStatus.DELIVERY_FAILED, StatusNormalizer.normalize("Delivery attempt failed"))
        assertEquals(ShipmentStatus.UNKNOWN, StatusNormalizer.normalize("lorem ipsum"))
    }

    @Test
    fun `empty text warns and yields empty draft`() {
        val r = SmartImportParser.parse("")
        assertTrue("no_text" in r.warnings)
        assertEquals(0f, r.overallConfidence)
        assertEquals(null, r.draft.totalMinor.value)
    }

    @Test
    fun `lazada detection and shipping extraction`() {
        val text = "Lazada\nOrder #8899\nShipping Fee ₱45.00\nGrand Total ₱1,234.50\nIn transit"
        val r = SmartImportParser.parse(text)
        assertEquals(Platform.LAZADA, r.detectedPlatform)
        assertEquals(4500L, r.draft.shippingMinor.value)
        assertEquals(123450L, r.draft.totalMinor.value)
        assertEquals(ShipmentStatus.IN_TRANSIT, r.draft.shipmentStatus.value)
    }
}
