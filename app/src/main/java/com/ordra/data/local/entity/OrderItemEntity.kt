package com.ordra.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
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
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val name: String,
    val variant: String?,
    val quantity: Int,
    val unitPriceMinor: Long?,
    val lineTotalMinor: Long?,
    val imageUri: String?,
    val notes: String?,
)
