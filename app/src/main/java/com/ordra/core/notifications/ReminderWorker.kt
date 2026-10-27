package com.ordra.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ordra.core.common.MoneyFormat
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.mapper.OrderMapper
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.model.ReminderType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val db: OrdraDatabase,
    private val notifier: OrdraNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reminderId = inputData.getString(KEY_REMINDER_ID) ?: return Result.failure()
        val reminder = db.reminderDao().getReminder(reminderId) ?: return Result.success()
        if (!reminder.enabled) return Result.success()

        val orderId = reminder.orderId ?: return Result.success()
        val order = db.orderDao().getOrder(orderId)?.let(OrderMapper::toDomain)
            ?: return Result.success()
        if (order.status in TERMINAL) return Result.success()

        val dayLabel = when (ReminderType.valueOf(reminder.type)) {
            ReminderType.DELIVERY_DAY -> "expected today"
            ReminderType.DAY_BEFORE -> "expected tomorrow"
            ReminderType.TWO_DAYS_BEFORE -> "expected in 2 days"
            ReminderType.CUSTOM -> "delivery reminder"
        }
        val body = buildString {
            append("${order.title} — $dayLabel")
            order.totalAmountMinor?.let {
                if (order.isCod) {
                    append(". Prepare ${MoneyFormat.formatMinor(it, order.currencyCode)} COD")
                }
            }
        }
        notifier.post(
            notificationId = reminder.notificationId ?: reminderId.hashCode(),
            channel = NotificationChannels.DELIVERY_REMINDERS,
            title = "Parcel $dayLabel",
            body = body,
        )
        return Result.success()
    }

    companion object {
        const val KEY_REMINDER_ID = "reminderId"
        private val TERMINAL = setOf(
            OrderStatus.DELIVERED, OrderStatus.CANCELLED,
            OrderStatus.RETURNED, OrderStatus.REFUNDED,
        )
    }
}
