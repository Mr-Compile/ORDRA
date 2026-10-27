package com.ordra.data.repository

import com.ordra.BuildConfig
import com.ordra.core.common.AppError
import com.ordra.core.common.AppResult
import com.ordra.data.backup.BackupCodec
import com.ordra.data.backup.BackupDocument
import androidx.room.withTransaction
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.dao.BackupDao
import com.ordra.domain.repository.BackupRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    private val dao: BackupDao,
    private val db: OrdraDatabase,
) : BackupRepository {

    override suspend fun exportBackup(): ByteArray = BackupCodec.encode(
        BackupDocument(
            exportedAt = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            orders = dao.allOrders(),
            items = dao.allItems(),
            shipments = dao.allShipments(),
            trackingEvents = dao.allTrackingEvents(),
            categories = dao.allCategories(),
            orderCategories = dao.allOrderCategories(),
            reminders = dao.allReminders(),
            returns = dao.allReturns(),
            refunds = dao.allRefunds(),
            warranties = dao.allWarranties(),
            receipts = dao.allReceipts(),
        ),
    )

    override suspend fun importBackup(bytes: ByteArray): AppResult<Int> =
        when (val decoded = BackupCodec.decode(bytes)) {
            is BackupCodec.DecodeResult.Error ->
                AppResult.Failure(AppError.BackupInvalid(decoded.message))
            is BackupCodec.DecodeResult.Ok -> runCatching {
                val doc = decoded.doc
                db.withTransaction {
                    dao.upsertOrders(doc.orders)
                    dao.upsertItems(doc.items)
                    dao.upsertShipments(doc.shipments)
                    dao.upsertTrackingEvents(doc.trackingEvents)
                    dao.upsertCategories(doc.categories)
                    dao.upsertOrderCategories(doc.orderCategories)
                    dao.upsertReminders(doc.reminders)
                    dao.upsertReturns(doc.returns)
                    dao.upsertRefunds(doc.refunds)
                    dao.upsertWarranties(doc.warranties)
                    dao.upsertReceipts(doc.receipts)
                }
                AppResult.Success(doc.orders.size)
            }.getOrElse { AppResult.Failure(AppError.DatabaseFailure(it)) }
        }

    override suspend fun exportCsv(): String {
        fun esc(v: Any?): String = "\"${v.toString().replace("\"", "\"\"")}\""
        val header = listOf(
            "id", "title", "platform", "status", "payment_method",
            "is_cod", "currency", "total_minor", "ordered_date", "created_at",
        ).joinToString(",")
        val rows = dao.allOrders().map { o ->
            listOf(
                o.id, esc(o.title), o.platform, o.status, o.paymentMethod,
                if (o.isCod) 1 else 0, o.currencyCode, o.totalAmountMinor ?: "",
                o.orderedDate ?: "", o.createdAt,
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}
