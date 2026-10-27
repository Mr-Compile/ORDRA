package com.ordra.domain.model

import java.time.Instant

enum class SuggestionState { PENDING, CONFIRMED, DISMISSED }

/** Extracted signals from a notification/shared text before matching. */
data class NotificationSignals(
    val isDeliveryRelevant: Boolean,
    val trackingNumber: String? = null,
    val courier: Courier? = null,
    val platform: Platform? = null,
    val statusText: String? = null,
    val codAmountMinor: Long? = null,
    val productHint: String? = null,
    val riderIndicator: Boolean = false,
)

data class DetectionSuggestion(
    val id: String,
    val shipmentId: String? = null,
    val orderId: String? = null,
    val status: ShipmentStatus,
    val courier: Courier? = null,
    val trackingNumber: String? = null,
    val source: TrackingStatusSource,
    val confidence: DetectionConfidence,
    val state: SuggestionState = SuggestionState.PENDING,
    val needsParcelPick: Boolean = false,
    val notificationPackage: String? = null,
    val summary: String? = null,
    val detectedAt: Instant,
    val appliedAt: Instant? = null,
)
