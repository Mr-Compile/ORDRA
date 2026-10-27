package com.ordra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracking_events",
    foreignKeys = [
        ForeignKey(
            entity = ShipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["shipmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("shipmentId")],
)
data class TrackingEventEntity(
    @PrimaryKey val id: String,
    val shipmentId: String,
    val shipmentStatus: String,
    val title: String,
    val description: String?,
    val occurredAt: Long,
    val detectedAt: Long,
    val source: String,
    val confidence: String,
    val rawReference: String?,
)
