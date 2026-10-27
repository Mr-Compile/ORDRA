package com.ordra.data.local.entity

import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.PrimaryKey

@Entity(tableName = "detection_suggestions")
data class DetectionSuggestionEntity(
    @PrimaryKey val id: String,
    val shipmentId: String?,
    val orderId: String?,
    val status: String,
    val courier: String?,
    val trackingNumber: String?,
    val source: String,
    val confidence: String,
    val state: String,
    val needsParcelPick: Boolean,
    val notificationPackage: String?,
    val summary: String?,
    val detectedAt: Long,
    val appliedAt: Long?,
)
