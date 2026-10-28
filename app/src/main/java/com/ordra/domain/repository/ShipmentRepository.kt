package com.ordra.domain.repository

import com.ordra.domain.model.DetectionConfidence
import com.ordra.domain.model.Shipment
import com.ordra.domain.model.ShipmentStatus
import com.ordra.domain.model.TrackingEvent
import com.ordra.domain.model.TrackingStatusSource
import kotlinx.coroutines.flow.Flow

interface ShipmentRepository {
    fun observePrimaryShipment(orderId: String): Flow<Shipment?>
    fun observeEvents(shipmentId: String): Flow<List<TrackingEvent>>
    fun observeAttentionShipmentCount(): Flow<Int>
    suspend fun getPrimaryShipment(orderId: String): Shipment?
    suspend fun saveShipment(shipment: Shipment)
    suspend fun updateShipmentStatus(
        shipmentId: String,
        status: ShipmentStatus,
        source: TrackingStatusSource = TrackingStatusSource.MANUAL,
        confidence: DetectionConfidence = DetectionConfidence.HIGH,
        title: String? = null,
    )
}
