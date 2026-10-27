package com.ordra.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "returns",
    foreignKeys = [ForeignKey(OrderEntity::class, ["id"], ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("orderId")],
)
data class ReturnEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val reason: String?,
    val status: String,
    val requestedAt: Long,
    val resolvedAt: Long?,
    val notes: String?,
)

@Entity(
    tableName = "refunds",
    foreignKeys = [ForeignKey(OrderEntity::class, ["id"], ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("orderId")],
)
data class RefundEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val amountMinor: Long?,
    val status: String,
    val reason: String?,
    val requestedAt: Long,
    val processedAt: Long?,
)

@Entity(
    tableName = "warranties",
    foreignKeys = [ForeignKey(OrderEntity::class, ["id"], ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("orderId")],
)
data class WarrantyEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val provider: String?,
    val startDate: String,
    val endDate: String,
    val notes: String?,
)

@Entity(
    tableName = "receipts",
    foreignKeys = [ForeignKey(OrderEntity::class, ["id"], ["orderId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("orderId")],
)
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val imageUri: String,
    val label: String?,
    val addedAt: Long,
)
