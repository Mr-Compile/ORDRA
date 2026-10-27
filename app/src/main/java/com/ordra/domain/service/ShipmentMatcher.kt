package com.ordra.domain.service

import com.ordra.domain.model.Courier
import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.NotificationSignals
import com.ordra.domain.model.Platform

data class MatchOutcome(
    val shipmentId: String?,
    val orderId: String?,
    val confidence: DetectionConfidence,
    val needsParcelPick: Boolean,
    val matchedSignals: List<String>,
)

/**
 * Matches notification signals to known shipments/orders.
 * Exact tracking -> HIGH. Courier + amount/platform context -> MEDIUM.
 * Multiple candidates -> AMBIGUOUS (needsParcelPick, never arbitrary pick).
 */
object ShipmentMatcher {

    data class ShipmentCandidate(
        val shipmentId: String,
        val orderId: String,
        val trackingNumber: String?,
        val courier: Courier,
        val totalAmountMinor: Long?,
        val platform: Platform,
    )

    fun match(
        signals: NotificationSignals,
        candidates: List<ShipmentCandidate>,
    ): MatchOutcome? {
        if (!signals.isDeliveryRelevant) return null
        val tracking = signals.trackingNumber

        if (tracking != null) {
            val hits = candidates.filter {
                it.trackingNumber.equals(tracking, ignoreCase = true)
            }
            if (hits.size == 1) {
                return MatchOutcome(
                    hits[0].shipmentId, hits[0].orderId,
                    DetectionConfidence.HIGH, false,
                    listOf("tracking:$tracking"),
                )
            }
            if (hits.size > 1) {
                return MatchOutcome(
                    null, null, DetectionConfidence.MEDIUM, true,
                    listOf("tracking_ambiguous:$tracking"),
                )
            }
        }

        // Courier + (amount or platform) -> medium
        if (signals.courier != null) {
            val courierHits = candidates.filter { it.courier == signals.courier }
            val refined = courierHits.filter { c ->
                (signals.codAmountMinor != null &&
                    c.totalAmountMinor == signals.codAmountMinor) ||
                    (signals.platform != null && c.platform == signals.platform)
            }
            return when {
                refined.size == 1 -> MatchOutcome(
                    refined[0].shipmentId, refined[0].orderId,
                    DetectionConfidence.MEDIUM, false,
                    listOf("courier+context"),
                )
                courierHits.size == 1 -> MatchOutcome(
                    courierHits[0].shipmentId, courierHits[0].orderId,
                    DetectionConfidence.LOW, false,
                    listOf("courier_only"),
                )
                refined.isEmpty() && courierHits.isEmpty() -> null
                else -> MatchOutcome(
                    null, null, DetectionConfidence.LOW, true,
                    listOf("ambiguous"),
                )
            }
        }
        return null
    }
}
