package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ordra.data.local.entity.ShipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {

    @Upsert
    suspend fun upsertShipment(shipment: ShipmentEntity)

    @Query("SELECT * FROM shipments WHERE orderId = :orderId ORDER BY createdAt ASC")
    fun observeShipmentsForOrder(orderId: String): Flow<List<ShipmentEntity>>

    /** Primary shipment = earliest created (common UI uses exactly one). */
    @Query("SELECT * FROM shipments WHERE orderId = :orderId ORDER BY createdAt ASC LIMIT 1")
    fun observePrimaryShipment(orderId: String): Flow<ShipmentEntity?>

    @Query("SELECT * FROM shipments WHERE orderId = :orderId ORDER BY createdAt ASC LIMIT 1")
    suspend fun getPrimaryShipment(orderId: String): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE id = :shipmentId")
    suspend fun getShipment(shipmentId: String): ShipmentEntity?

    @Query(
        "UPDATE shipments SET shipmentStatus = :status, statusSource = :source, " +
            "statusConfidence = :confidence, lastStatusAt = :at, lastDetectedAt = :detectedAt, " +
            "updatedAt = :at WHERE id = :shipmentId",
    )
    suspend fun updateStatus(
        shipmentId: String,
        status: String,
        source: String,
        confidence: String,
        at: Long,
        detectedAt: Long?,
    )

    @Query("DELETE FROM shipments WHERE id = :shipmentId")
    suspend fun deleteShipment(shipmentId: String)
}
