package com.ordra.domain.service

import com.ordra.domain.model.ShipmentStatus

/**
 * Canonical forward progression. Stale detections never move a parcel backward;
 * a regression is rejected (the event may still be recorded as history).
 */
object StatusProgression {

    private val order = listOf(
        ShipmentStatus.PREPARING,
        ShipmentStatus.PICKED_UP,
        ShipmentStatus.IN_TRANSIT,
        ShipmentStatus.ARRIVED_AT_HUB,
        ShipmentStatus.OUT_FOR_DELIVERY,
        ShipmentStatus.DELIVERED,
    )

    private val exceptions = setOf(
        ShipmentStatus.DELIVERY_FAILED,
        ShipmentStatus.RETURNING,
        ShipmentStatus.RETURNED,
    )

    fun rank(status: ShipmentStatus): Int = order.indexOf(status)

    /** DELIVERED/terminal never regresses; exceptions allowed from any active state. */
    fun canTransition(from: ShipmentStatus, to: ShipmentStatus): Boolean = when {
        to == ShipmentStatus.UNKNOWN -> false
        from == ShipmentStatus.DELIVERED -> false
        from == ShipmentStatus.RETURNED -> false
        to in exceptions -> from !in setOf(
            ShipmentStatus.DELIVERED, ShipmentStatus.RETURNED,
        )
        from == ShipmentStatus.UNKNOWN -> true
        to !in order -> true
        from !in order -> true
        else -> rank(to) >= rank(from)
    }
}
