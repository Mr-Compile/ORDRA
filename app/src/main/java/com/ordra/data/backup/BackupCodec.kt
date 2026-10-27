package com.ordra.data.backup

import com.ordra.data.local.entity.CategoryEntity
import com.ordra.data.local.entity.OrderCategoryEntity
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity
import com.ordra.data.local.entity.ReceiptEntity
import com.ordra.data.local.entity.RefundEntity
import com.ordra.data.local.entity.ReminderEntity
import com.ordra.data.local.entity.ReturnEntity
import com.ordra.data.local.entity.ShipmentEntity
import com.ordra.data.local.entity.TrackingEventEntity
import com.ordra.data.local.entity.WarrantyEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class BackupDocument(
    val schemaVersion: Int = BackupCodec.SCHEMA_VERSION,
    val exportedAt: Long,
    val appVersion: String = "",
    val orders: List<OrderEntity> = emptyList(),
    val items: List<OrderItemEntity> = emptyList(),
    val shipments: List<ShipmentEntity> = emptyList(),
    val trackingEvents: List<TrackingEventEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val orderCategories: List<OrderCategoryEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val returns: List<ReturnEntity> = emptyList(),
    val refunds: List<RefundEntity> = emptyList(),
    val warranties: List<WarrantyEntity> = emptyList(),
    val receipts: List<ReceiptEntity> = emptyList(),
)

/**
 * Versioned JSON backup codec. Pure — unit-testable.
 * Entities are serialized directly so FK relationships survive a round-trip.
 */
object BackupCodec {
    const val SCHEMA_VERSION = 1

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(doc: BackupDocument): ByteArray =
        json.encodeToString(BackupDocument.serializer(), doc).toByteArray()

    sealed interface DecodeResult {
        data class Ok(val doc: BackupDocument) : DecodeResult
        data class Error(val message: String) : DecodeResult
    }

    fun decode(bytes: ByteArray): DecodeResult = runCatching {
        val doc = json.decodeFromString(BackupDocument.serializer(), bytes.decodeToString())
        if (doc.schemaVersion > SCHEMA_VERSION) {
            DecodeResult.Error("Backup is from a newer Ordra version (${doc.schemaVersion}).")
        } else {
            DecodeResult.Ok(doc)
        }
    }.getOrElse { DecodeResult.Error("Not a valid Ordra backup file.") }
}
