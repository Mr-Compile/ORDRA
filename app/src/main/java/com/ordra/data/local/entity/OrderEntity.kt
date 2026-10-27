package com.ordra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted order row. Enums stored as name strings; timestamps as epoch millis;
 * date-only delivery fields as epoch days; money as integer minor units.
 * [archivedAt] null = active row, non-null = archived (soft delete).
 * Courier/tracking live on the shipments table, not here.
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val platform: String,
    val customPlatformName: String?,
    val sellerName: String?,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val isCod: Boolean,
    val currencyCode: String,
    val itemSubtotalMinor: Long?,
    val shippingFeeMinor: Long?,
    val discountMinor: Long?,
    val totalAmountMinor: Long?,
    val orderedDate: Long?,
    val estimatedDeliveryStart: Long?,
    val estimatedDeliveryEnd: Long?,
    val deliveredAt: Long?,
    val notes: String?,
    val productImageUri: String?,
    val rating: Int?,
    val isFavorite: Boolean,
    val archivedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
)
