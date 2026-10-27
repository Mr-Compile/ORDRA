package com.ordra.data.repository

import com.ordra.data.local.dao.ShipmentDao
import com.ordra.data.local.dao.TrackingEventDao
import com.ordra.data.local.mapper.ShipmentMapper
import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.TrackingEvent
import com.ordra.domain.model.TrackingStatusSource
import com.ordra.domain.repository.ShipmentRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ShipmentRepositoryImpl @Inject constructor(
    private val shipmentDao: ShipmentDao,
    private val trackingEventDao: TrackingEventDao,
) : ShipmentRepository {

    override fun observePrimaryShipment(orderId: String): Flow<Shipment?> =
        shipmentDao.observePrimaryShipment(orderId).map { it?.let(ShipmentMapper::toDomain) }

    override fun observeEvents(shipmentId: String): Flow<List<TrackingEvent>> =
        trackingEventDao.observeEvents(shipmentId).map { list -> list.map(ShipmentMapper::toDomain) }

    override suspend fun getPrimaryShipment(orderId: String): Shipment? =
        shipmentDao.getPrimaryShipment(orderId)?.let(ShipmentMapper::toDomain)

    override suspend fun saveShipment(shipment: Shipment) =
        shipmentDao.upsertShipment(ShipmentMapper.toEntity(shipment))

    override suspend fun updateShipmentStatus(
        shipmentId: String,
        status: ShipmentStatus,
        source: TrackingStatusSource,
        title: String?,
    ) {
        val now = Instant.now().toEpochMilli()
        shipmentDao.updateStatus(
            shipmentId = shipmentId,
            status = status.name,
            source = source.name,
            confidence = DetectionConfidence.HIGH.name,
            at = now,
            detectedAt = if (source == TrackingStatusSource.MANUAL) null else now,
        )
        // Events are immutable; skip when this transition is already recorded.
        if (trackingEventDao.countEventsWithStatus(shipmentId, status.name) == 0) {
            trackingEventDao.insert(
                TrackingEventEntityBuilder.event(
                    shipmentId = shipmentId,
                    status = status,
                    title = title,
                    source = source,
                    at = now,
                ),
            )
        }
    }
}

private object TrackingEventEntityBuilder {
    fun event(
        shipmentId: String,
        status: ShipmentStatus,
        title: String?,
        source: TrackingStatusSource,
        at: Long,
    ) = TrackingEvent(
        id = UUID.randomUUID().toString(),
        shipmentId = shipmentId,
        shipmentStatus = status,
        title = title ?: status.name.lowercase().replace('_', ' ')
            .replaceFirstChar { it.uppercase() },
        occurredAt = Instant.ofEpochMilli(at),
        detectedAt = Instant.ofEpochMilli(at),
        source = source,
        confidence = DetectionConfidence.HIGH,
    ).let(ShipmentMapper::toEntity)
}
