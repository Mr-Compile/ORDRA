package com.ordra.data.local.mapper

import com.ordra.data.local.entity.ShipmentEntity
import com.ordra.data.local.entity.TrackingEventEntity
import com.ordra.domain.model.Courier
import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.TrackingEvent
import com.ordra.domain.model.TrackingStatusSource
import java.time.Instant

object ShipmentMapper {

    fun toDomain(entity: ShipmentEntity): Shipment = with(entity) {
        Shipment(
            id = id,
            orderId = orderId,
            courier = Courier.valueOf(courier),
            customCourierName = customCourierName,
            trackingNumber = trackingNumber,
            shipmentStatus = ShipmentStatus.valueOf(shipmentStatus),
            statusSource = TrackingStatusSource.valueOf(statusSource),
            statusConfidence = DetectionConfidence.valueOf(statusConfidence),
            lastStatusAt = lastStatusAt?.let(Instant::ofEpochMilli),
            lastDetectedAt = lastDetectedAt?.let(Instant::ofEpochMilli),
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt),
        )
    }

    fun toEntity(domain: Shipment): ShipmentEntity = with(domain) {
        ShipmentEntity(
            id = id,
            orderId = orderId,
            courier = courier.name,
            customCourierName = customCourierName,
            trackingNumber = trackingNumber,
            shipmentStatus = shipmentStatus.name,
            statusSource = statusSource.name,
            statusConfidence = statusConfidence.name,
            lastStatusAt = lastStatusAt?.toEpochMilli(),
            lastDetectedAt = lastDetectedAt?.toEpochMilli(),
            createdAt = createdAt.toEpochMilli(),
            updatedAt = updatedAt.toEpochMilli(),
        )
    }

    fun toDomain(entity: TrackingEventEntity): TrackingEvent = with(entity) {
        TrackingEvent(
            id = id,
            shipmentId = shipmentId,
            shipmentStatus = ShipmentStatus.valueOf(shipmentStatus),
            title = title,
            description = description,
            occurredAt = Instant.ofEpochMilli(occurredAt),
            detectedAt = Instant.ofEpochMilli(detectedAt),
            source = TrackingStatusSource.valueOf(source),
            confidence = DetectionConfidence.valueOf(confidence),
            rawReference = rawReference,
        )
    }

    fun toEntity(domain: TrackingEvent): TrackingEventEntity = with(domain) {
        TrackingEventEntity(
            id = id,
            shipmentId = shipmentId,
            shipmentStatus = shipmentStatus.name,
            title = title,
            description = description,
            occurredAt = occurredAt.toEpochMilli(),
            detectedAt = detectedAt.toEpochMilli(),
            source = source.name,
            confidence = confidence.name,
            rawReference = rawReference,
        )
    }
}
