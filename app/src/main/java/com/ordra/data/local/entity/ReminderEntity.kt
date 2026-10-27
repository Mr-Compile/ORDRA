package com.ordra.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
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
data class ReminderEntity(
    @PrimaryKey val id: String,
    val orderId: String?,
    val type: String,
    val scheduledAt: Long,
    val enabled: Boolean,
    val notificationId: Int?,
    val createdAt: Long,
)
