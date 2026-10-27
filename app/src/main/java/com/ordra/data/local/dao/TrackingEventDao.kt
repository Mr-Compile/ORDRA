package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ordra.data.local.entity.TrackingEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackingEventDao {

    @Insert
    suspend fun insert(event: TrackingEventEntity)

    @Query("SELECT * FROM tracking_events WHERE shipmentId = :shipmentId ORDER BY occurredAt DESC")
    fun observeEvents(shipmentId: String): Flow<List<TrackingEventEntity>>

    @Query("SELECT COUNT(*) FROM tracking_events WHERE shipmentId = :shipmentId AND shipmentStatus = :status")
    suspend fun countEventsWithStatus(shipmentId: String, status: String): Int
}
