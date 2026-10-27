package com.ordra.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ordra.core.common.MoneyFormat
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.mapper.OrderMapper
import com.ordra.domain.model.OrderStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import kotlinx.coroutines.flow.first

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: OrdraDatabase,
    private val notifier: OrdraNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val terminal = setOf(
            OrderStatus.DELIVERED, OrderStatus.CANCELLED,
            OrderStatus.RETURNED, OrderStatus.REFUNDED,
        )
        val today = LocalDate.now()
        val orders = db.orderDao().observeActiveOrders().first()
            .map(OrderMapper::toDomain)
            .filter { it.status !in terminal }
        val dueToday = orders.count {
            (it.estimatedDeliveryStart ?: it.estimatedDeliveryEnd) == today
        }
        val cod = orders.filter { it.isCod }
            .sumOf { it.totalAmountMinor ?: 0L }
        if (dueToday == 0 && cod == 0L) return Result.success()
        val body = buildString {
            if (dueToday > 0) append("$dueToday parcel(s) expected today. ")
            if (cod > 0) {
                append(
                    "Prepare ${MoneyFormat.formatMinor(cod, "PHP")} COD.",
                )
            }
        }
        notifier.post(
            notificationId = 1001,
            channel = NotificationChannels.DAILY_SUMMARY,
            title = "Ordra daily summary",
            body = body.trim(),
        )
        return Result.success()
    }
}
