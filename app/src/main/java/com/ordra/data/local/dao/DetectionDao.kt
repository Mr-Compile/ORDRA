package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ordra.data.local.entity.DetectionSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionDao {

    @Upsert
    suspend fun upsert(suggestion: DetectionSuggestionEntity)

    @Query("SELECT * FROM detection_suggestions WHERE state = 'PENDING' ORDER BY detectedAt DESC")
    fun observePending(): Flow<List<DetectionSuggestionEntity>>

    @Query("SELECT * FROM detection_suggestions WHERE id = :id")
    suspend fun get(id: String): DetectionSuggestionEntity?

    @Query("UPDATE detection_suggestions SET state = :state, appliedAt = :appliedAt WHERE id = :id")
    suspend fun updateState(id: String, state: String, appliedAt: Long?)

    /** Dedupe: an identical pending suggestion (same key + status + source) is dropped. */
    @Query(
        "SELECT COUNT(*) FROM detection_suggestions WHERE state = 'PENDING' " +
            "AND status = :status AND source = :source " +
            "AND (trackingNumber = :tracking OR orderId = :orderId OR summary = :summary)",
    )
    suspend fun countSimilar(
        tracking: String?,
        orderId: String?,
        status: String,
        source: String,
        summary: String?,
    ): Int
}
