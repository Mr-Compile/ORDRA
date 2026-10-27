package com.ordra.domain.service

import com.ordra.domain.model.ShipmentStatus

/**
 * Raw status text -> canonical ShipmentStatus. The ONLY place raw wording maps
 * to statuses — ViewModels/Composables never string-match status text.
 * Unrecognized text yields UNKNOWN, never a guessed status.
 */
object StatusNormalizer {

    private val rules = listOf(
        ShipmentStatus.DELIVERED to listOf("delivered", "package delivered", "completed delivery"),
        ShipmentStatus.OUT_FOR_DELIVERY to listOf("out for delivery", "on the way to you", "rider is on the way"),
        ShipmentStatus.DELIVERY_FAILED to listOf("delivery failed", "unsuccessful delivery", "delivery attempt failed"),
        ShipmentStatus.ARRIVED_AT_HUB to listOf("arrived at", "arrived at hub", "arrived at sorting", "arrived at local"),
        ShipmentStatus.IN_TRANSIT to listOf("in transit", "departed", "on the way", "shipped", "left the", "handed over to courier"),
        ShipmentStatus.PICKED_UP to listOf("picked up", "pickup successful", "parcel picked up"),
        ShipmentStatus.PREPARING to listOf("to ship", "preparing", "packing", "awaiting pickup", "order placed"),
        ShipmentStatus.RETURNED to listOf("returned to seller", "return completed"),
        ShipmentStatus.RETURNING to listOf("returning to seller", "return to sender", "rts"),
    )

    fun normalize(text: String): ShipmentStatus {
        val lower = text.lowercase()
        return rules.firstOrNull { (_, keys) -> keys.any { lower.contains(it) } }?.first
            ?: ShipmentStatus.UNKNOWN
    }
}
