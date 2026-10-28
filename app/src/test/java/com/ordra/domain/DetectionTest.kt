package com.ordra.domain

import com.ordra.domain.model.Courier
import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.NotificationSignals
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.UserSettings
import com.ordra.domain.service.NotificationParser
import com.ordra.domain.service.ShipmentMatcher
import com.ordra.domain.service.StatusProgression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectionTest {

    @Test
    fun `auto-apply high-confidence updates defaults to enabled`() {
        assertTrue(UserSettings().autoApplyHighConfidence)
    }

    @Test
    fun `irrelevant notification is rejected`() {
        val s = NotificationParser.parse("Your OTP is 123456")
        assertFalse(s.isDeliveryRelevant)
    }

    @Test
    fun `delivery notification extracts signals`() {
        val s = NotificationParser.parse(
            "Shopee: Your parcel SPXPH123456 is out for delivery today!",
        )
        assertTrue(s.isDeliveryRelevant)
        assertEquals("SPXPH123456", s.trackingNumber)
        assertEquals(Platform.SHOPEE, s.platform)
        assertEquals(Courier.SPX_EXPRESS, s.courier)
        assertEquals(ShipmentStatus.OUT_FOR_DELIVERY.name, s.statusText)
    }

    @Test
    fun `filipino wording is relevant and normalized`() {
        val s = NotificationParser.parse("Nasa daan na ang iyong padala, rider is on the way")
        assertTrue(s.isDeliveryRelevant)
        assertTrue(s.riderIndicator)
    }

    private fun candidate(id: String, tracking: String? = null, courier: Courier = Courier.SPX_EXPRESS) =
        ShipmentMatcher.ShipmentCandidate(
            shipmentId = "s_$id", orderId = id, trackingNumber = tracking,
            courier = courier, totalAmountMinor = 64900, platform = Platform.SHOPEE,
        )

    @Test
    fun `exact tracking match yields HIGH`() {
        val outcome = ShipmentMatcher.match(
            NotificationSignals(
                isDeliveryRelevant = true, trackingNumber = "SPXPH999",
                courier = Courier.SPX_EXPRESS,
            ),
            listOf(candidate("a", "SPXPH999"), candidate("b", "OTHER1")),
        )
        assertEquals(DetectionConfidence.HIGH, outcome?.confidence)
        assertEquals("s_a", outcome?.shipmentId)
    }

    @Test
    fun `multiple tracking hits require parcel pick`() {
        val outcome = ShipmentMatcher.match(
            NotificationSignals(isDeliveryRelevant = true, trackingNumber = "SAME123"),
            listOf(candidate("a", "same123"), candidate("b", "SAME123")),
        )
        assertTrue(outcome?.needsParcelPick == true)
        assertNull(outcome?.shipmentId)
    }

    @Test
    fun `status progression rejects regression`() {
        assertFalse(
            StatusProgression.canTransition(
                ShipmentStatus.DELIVERED, ShipmentStatus.IN_TRANSIT,
            ),
        )
        assertFalse(
            StatusProgression.canTransition(
                ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.PREPARING,
            ),
        )
        assertTrue(
            StatusProgression.canTransition(
                ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERY_FAILED,
            ),
        )
        assertTrue(
            StatusProgression.canTransition(
                ShipmentStatus.PREPARING, ShipmentStatus.OUT_FOR_DELIVERY,
            ),
        )
    }
}
