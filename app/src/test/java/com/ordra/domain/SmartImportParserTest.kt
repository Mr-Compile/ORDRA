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
    fun `shopee order details page extracts accurately`() {
        // Transcribed from a real Shopee "Order Details" screenshot (ENGLAB ESP32).
        val text = """
            Order Details
            Estimated delivery date: 29 Oct - 1 Nov
            Guaranteed On-Time Delivery: Get a ₱40 voucher if no delivery was attempted by 03-11-2026. View More
            Shipping Information
            SPX Express: PH2613786845088
            Parcel has departed from sorting facility
            27-10-2026 07:42
            Delivery Information
            Michael Torillo (+63) 955 241 9031
            Garzon Store, Balayon Street, Purok 13a, Poblacion, Tupi, Mindanao, South Cotabato, 9505
            Preferred
            ENGLAB.ph
            ENGLAB★ ESP32 WiFi IoT Development Board
            ESP32 - 30P - TYPEC - CP2102 x1
            ₱329
            Merchandise Subtotal
            ₱329
            Shipping Fee
            ₱106
            Shipping Discount Subtotal
            -₱106
            Shopee Voucher Applied
            -₱70
            Order Total: ₱259
            Support Center
            Request for Refund
            Request Refund
            Track Order
        """.trimIndent()
        val r = SmartImportParser.parse(text)
        assertEquals(Platform.SHOPEE, r.detectedPlatform)
        assertEquals(Courier.SPX_EXPRESS, r.draft.courier.value)
        assertEquals("PH2613786845088", r.draft.trackingNumber.value)
        assertEquals(25900L, r.draft.totalMinor.value)
        assertEquals(10600L, r.draft.shippingMinor.value)
        assertEquals(17600L, r.draft.discountMinor.value)
        assertEquals(32900L, r.draft.merchandiseSubtotalMinor.value)
        assertEquals(ShipmentStatus.IN_TRANSIT, r.draft.shipmentStatus.value)
        assertEquals(java.time.LocalDate.of(2026, 10, 29), r.draft.etaStart.value)
        assertEquals(java.time.LocalDate.of(2026, 11, 1), r.draft.etaEnd.value)
        assertTrue(r.draft.title.value!!.contains("ESP32"))
        assertTrue(r.draft.sellerName.value!!.contains("ENGLAB"))
        assertTrue(r.draft.itemNames.isNotEmpty())
        assertEquals(1, r.draft.itemQuantity.value)
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
