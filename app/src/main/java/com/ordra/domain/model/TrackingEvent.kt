package com.ordra.domain.model

import java.time.Instant

/**
 * Immutable timeline entry belonging to a Shipment.
 * [rawReference] is a minimal pointer only — never raw notification/OCR text.
 */
data class TrackingEvent(
    val id: String,
    val shipmentId: String,
    val shipmentStatus: ShipmentStatus,
    val title: String,
    val description: String? = null,
    val occurredAt: Instant,
    val detectedAt: Instant,
    val source: TrackingStatusSource,
    val confidence: DetectionConfidence,
    val rawReference: String? = null,
)
