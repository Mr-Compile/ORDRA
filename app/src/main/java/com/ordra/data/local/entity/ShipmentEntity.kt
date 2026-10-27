package com.ordra.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shipments",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("orderId")],
)
@Serializable
data class ShipmentEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val courier: String,
    val customCourierName: String?,
    val trackingNumber: String?,
    val shipmentStatus: String,
    val statusSource: String,
    val statusConfidence: String,
    val lastStatusAt: Long?,
    val lastDetectedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
