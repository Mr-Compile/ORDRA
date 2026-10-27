package com.ordra.data.repository

import com.ordra.data.local.dao.PostPurchaseDao
import com.ordra.data.local.dao.ReminderDao
import com.ordra.data.local.entity.ReceiptEntity
import com.ordra.data.local.entity.RefundEntity
import com.ordra.data.local.entity.ReminderEntity
import com.ordra.data.local.entity.ReturnEntity
import com.ordra.data.local.entity.WarrantyEntity
import com.ordra.domain.model.PurchaseReturn
import com.ordra.domain.model.Receipt
import com.ordra.domain.model.Refund
import com.ordra.domain.model.RefundStatus
import com.ordra.domain.model.ReminderType
import com.ordra.domain.model.ReturnStatus
import com.ordra.domain.model.Warranty
import com.ordra.domain.repository.PostPurchaseRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PostPurchaseRepositoryImpl @Inject constructor(
    private val dao: PostPurchaseDao,
    private val reminderDao: ReminderDao,
) : PostPurchaseRepository {

    override fun observeReturns(orderId: String): Flow<List<PurchaseReturn>> =
        dao.observeReturns(orderId).map { list ->
            list.map {
                PurchaseReturn(
                    id = it.id, orderId = it.orderId, reason = it.reason,
                    status = ReturnStatus.valueOf(it.status),
                    requestedAt = Instant.ofEpochMilli(it.requestedAt),
                    resolvedAt = it.resolvedAt?.let(Instant::ofEpochMilli),
                    notes = it.notes,
                )
            }
        }

    override fun observeRefunds(orderId: String): Flow<List<Refund>> =
        dao.observeRefunds(orderId).map { list ->
            list.map {
                Refund(
                    id = it.id, orderId = it.orderId, amountMinor = it.amountMinor,
                    status = RefundStatus.valueOf(it.status), reason = it.reason,
                    requestedAt = Instant.ofEpochMilli(it.requestedAt),
                    processedAt = it.processedAt?.let(Instant::ofEpochMilli),
                )
            }
        }

    override fun observeWarranties(orderId: String): Flow<List<Warranty>> =
        dao.observeWarranties(orderId).map { list ->
            list.map {
                Warranty(
                    id = it.id, orderId = it.orderId, provider = it.provider,
                    startDate = LocalDate.parse(it.startDate),
                    endDate = LocalDate.parse(it.endDate),
                    notes = it.notes,
                )
            }
        }

    override fun observeReceipts(orderId: String): Flow<List<Receipt>> =
        dao.observeReceipts(orderId).map { list ->
            list.map {
                Receipt(
                    id = it.id, orderId = it.orderId, imageUri = it.imageUri,
                    label = it.label, addedAt = Instant.ofEpochMilli(it.addedAt),
                )
            }
        }

    override suspend fun saveReturn(item: PurchaseReturn) = dao.upsertReturn(
        ReturnEntity(
            id = item.id, orderId = item.orderId, reason = item.reason,
            status = item.status.name,
            requestedAt = item.requestedAt.toEpochMilli(),
            resolvedAt = item.resolvedAt?.toEpochMilli(), notes = item.notes,
        ),
    )

    override suspend fun saveRefund(item: Refund) = dao.upsertRefund(
        RefundEntity(
            id = item.id, orderId = item.orderId, amountMinor = item.amountMinor,
            status = item.status.name, reason = item.reason,
            requestedAt = item.requestedAt.toEpochMilli(),
            processedAt = item.processedAt?.toEpochMilli(),
        ),
    )

    override suspend fun saveWarranty(item: Warranty) {
        dao.upsertWarranty(
            WarrantyEntity(
                id = item.id, orderId = item.orderId, provider = item.provider,
                startDate = item.startDate.toString(), endDate = item.endDate.toString(),
                notes = item.notes,
            ),
        )
        // Warranty-expiry reminder: CUSTOM type one day before end, 09:00.
        val at = item.endDate.minusDays(1)
            .atTime(LocalTime.of(9, 0)).atZone(ZoneId.systemDefault()).toInstant()
        if (at.isAfter(Instant.now())) {
            reminderDao.upsert(
                ReminderEntity(
                    id = "warranty_${item.id}",
                    orderId = item.orderId,
                    type = ReminderType.CUSTOM.name,
                    scheduledAt = at.toEpochMilli(),
                    enabled = true,
                    notificationId = null,
                    createdAt = Instant.now().toEpochMilli(),
                ),
            )
        }
    }

    override suspend fun saveReceipt(item: Receipt) = dao.upsertReceipt(
        ReceiptEntity(
            id = item.id, orderId = item.orderId, imageUri = item.imageUri,
            label = item.label, addedAt = item.addedAt.toEpochMilli(),
        ),
    )

    override suspend fun deleteReceipt(id: String) = dao.deleteReceipt(id)
}
