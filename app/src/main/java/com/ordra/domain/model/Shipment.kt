package com.ordra.domain.model

import java.time.Instant

/**
 * One physical parcel belonging to an order. Courier and tracking number live
 * here — never on Order — so one order may carry multiple parcels.
 */
data class Shipment(
    val id: String,
    val orderId: String,
    val courier: Courier = Courier.UNKNOWN,
    val customCourierName: String? = null,
    val trackingNumber: String? = null,
    val shipmentStatus: ShipmentStatus = ShipmentStatus.UNKNOWN,
    val statusSource: TrackingStatusSource = TrackingStatusSource.MANUAL,
    val statusConfidence: DetectionConfidence = DetectionConfidence.HIGH,
    val lastStatusAt: Instant? = null,
    val lastDetectedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
