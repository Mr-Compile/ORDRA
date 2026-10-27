package com.ordra.data.repository

import com.ordra.data.local.dao.DetectionDao
import com.ordra.data.local.dao.OrderDao
import com.ordra.data.local.dao.ShipmentDao
import com.ordra.data.local.entity.DetectionSuggestionEntity
import com.ordra.data.local.mapper.OrderMapper
import com.ordra.data.local.mapper.ShipmentMapper
import com.ordra.domain.model.Courier
import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.DetectionSuggestion
import com.ordra.domain.model.NotificationSignals
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.SuggestionState
import com.ordra.domain.model.TrackingStatusSource
import com.ordra.domain.repository.DetectionRepository
import com.ordra.domain.repository.ShipmentRepository
import com.ordra.domain.service.ShipmentMatcher
import com.ordra.domain.service.StatusNormalizer
import com.ordra.domain.service.StatusProgression
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class DetectionRepositoryImpl @Inject constructor(
    private val detectionDao: DetectionDao,
    private val orderDao: OrderDao,
    private val shipmentDao: ShipmentDao,
    private val shipmentRepository: ShipmentRepository,
) : DetectionRepository {

    private fun toDomain(e: DetectionSuggestionEntity) = DetectionSuggestion(
        id = e.id,
        shipmentId = e.shipmentId,
        orderId = e.orderId,
        status = ShipmentStatus.valueOf(e.status),
        courier = e.courier?.let { runCatching { Courier.valueOf(it) }.getOrNull() },
        trackingNumber = e.trackingNumber,
        source = TrackingStatusSource.valueOf(e.source),
        confidence = DetectionConfidence.valueOf(e.confidence),
        state = SuggestionState.valueOf(e.state),
        needsParcelPick = e.needsParcelPick,
        notificationPackage = e.notificationPackage,
        summary = e.summary,
        detectedAt = Instant.ofEpochMilli(e.detectedAt),
        appliedAt = e.appliedAt?.let(Instant::ofEpochMilli),
    )

    private fun toEntity(d: DetectionSuggestion) = DetectionSuggestionEntity(
        id = d.id,
        shipmentId = d.shipmentId,
        orderId = d.orderId,
        status = d.status.name,
        courier = d.courier?.name,
        trackingNumber = d.trackingNumber,
        source = d.source.name,
        confidence = d.confidence.name,
        state = d.state.name,
        needsParcelPick = d.needsParcelPick,
        notificationPackage = d.notificationPackage,
        summary = d.summary,
        detectedAt = d.detectedAt.toEpochMilli(),
        appliedAt = d.appliedAt?.toEpochMilli(),
    )

    override fun observePending(): Flow<List<DetectionSuggestion>> =
        detectionDao.observePending().map { list -> list.map(::toDomain) }

    override suspend fun ingest(
        signals: NotificationSignals,
        source: TrackingStatusSource,
        notificationPackage: String?,
        summary: String?,
        autoApplyHigh: Boolean,
    ): DetectionSuggestion? {
        if (!signals.isDeliveryRelevant) return null
        val status = signals.statusText?.let {
            runCatching { ShipmentStatus.valueOf(it) }.getOrNull()
        } ?: StatusNormalizer.normalize(summary.orEmpty())
        if (status == ShipmentStatus.UNKNOWN) return null

        // Build candidates from orders that have shipments
        val orders = orderDao.observeActiveOrders().first().map(OrderMapper::toDomain)
        val shipments = orders.mapNotNull { order ->
            shipmentDao.getPrimaryShipment(order.id)?.let { s ->
                ShipmentMatcher.ShipmentCandidate(
                    shipmentId = s.id,
                    orderId = order.id,
                    trackingNumber = s.trackingNumber,
                    courier = runCatching { Courier.valueOf(s.courier) }
                        .getOrDefault(Courier.UNKNOWN),
                    totalAmountMinor = order.totalAmountMinor,
                    platform = order.platform,
                )
            }
        }
        val outcome = ShipmentMatcher.match(signals, shipments)
        if (outcome == null && signals.trackingNumber == null && signals.courier == null) {
            return null // nothing usable detected
        }

        val dupes = detectionDao.countSimilar(
            tracking = signals.trackingNumber,
            orderId = outcome?.orderId,
            status = status.name,
            source = source.name,
            summary = summary?.take(120),
        )
        if (dupes > 0) return null

        val suggestion = DetectionSuggestion(
            id = UUID.randomUUID().toString(),
            shipmentId = outcome?.shipmentId,
            orderId = outcome?.orderId,
            status = status,
            courier = signals.courier,
            trackingNumber = signals.trackingNumber,
            source = source,
            confidence = outcome?.confidence ?: DetectionConfidence.LOW,
            needsParcelPick = outcome?.needsParcelPick ?: false,
            notificationPackage = notificationPackage,
            summary = summary?.take(120),
            detectedAt = Instant.now(),
        )
        detectionDao.upsert(toEntity(suggestion))

        if (autoApplyHigh &&
            outcome?.confidence == DetectionConfidence.HIGH &&
            !outcome.needsParcelPick
        ) {
            apply(suggestion)
        }
        return suggestion
    }

    override suspend fun confirm(suggestionId: String, orderIdOverride: String?) {
        val entity = detectionDao.get(suggestionId) ?: return
        val suggestion = toDomain(entity).let {
            if (orderIdOverride != null) it.copy(orderId = orderIdOverride) else it
        }
        apply(suggestion)
    }

    override suspend fun dismiss(suggestionId: String) {
        detectionDao.updateState(suggestionId, SuggestionState.DISMISSED.name, null)
    }

    private suspend fun apply(suggestion: DetectionSuggestion) {
        val shipmentId = suggestion.shipmentId
            ?: suggestion.orderId?.let { shipmentDao.getPrimaryShipment(it)?.id }
            ?: return
        val current = shipmentDao.getShipment(shipmentId)?.let(ShipmentMapper::toDomain)
        val from = current?.shipmentStatus ?: ShipmentStatus.UNKNOWN
        if (!StatusProgression.canTransition(from, suggestion.status)) {
            // Regression: mark confirmed but do not move the parcel backward.
            detectionDao.updateState(
                suggestion.id, SuggestionState.CONFIRMED.name,
                Instant.now().toEpochMilli(),
            )
            return
        }
        shipmentRepository.updateShipmentStatus(
            shipmentId, suggestion.status, suggestion.source,
            title = suggestion.summary,
        )
        detectionDao.updateState(
            suggestion.id, SuggestionState.CONFIRMED.name,
            Instant.now().toEpochMilli(),
        )
    }
}
