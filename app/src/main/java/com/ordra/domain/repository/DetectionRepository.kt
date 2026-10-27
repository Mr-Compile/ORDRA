package com.ordra.domain.repository

import com.ordra.domain.model.DetectionSuggestion
import com.ordra.domain.model.NotificationSignals
import com.ordra.domain.model.TrackingStatusSource
import kotlinx.coroutines.flow.Flow

interface DetectionRepository {
    fun observePending(): Flow<List<DetectionSuggestion>>

    /**
     * Parse signals -> match against shipments -> persist a PENDING suggestion,
     * or apply immediately when confidence is HIGH and autoApply is enabled.
     * Returns the created suggestion or null when irrelevant/duplicate.
     */
    suspend fun ingest(
        signals: NotificationSignals,
        source: TrackingStatusSource,
        notificationPackage: String? = null,
        summary: String? = null,
        autoApplyHigh: Boolean = false,
    ): DetectionSuggestion?

    /** User-confirmed apply: writes status on the shipment + deduped TrackingEvent. */
    suspend fun confirm(suggestionId: String, orderIdOverride: String? = null)

    suspend fun dismiss(suggestionId: String)
}
