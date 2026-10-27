package com.ordra.core.notifications

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ordra.domain.model.Order
import com.ordra.domain.model.OrderStatus
import com.ordra.domain.repository.ReminderRepository
import com.ordra.domain.repository.ReminderScheduler
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val reminderRepository: ReminderRepository,
) : ReminderScheduler {

    private val terminal = setOf(
        OrderStatus.DELIVERED, OrderStatus.CANCELLED,
        OrderStatus.RETURNED, OrderStatus.REFUNDED,
    )

    override suspend fun syncOrderReminders(order: Order) {
        cancelOrderReminders(order.id)
        if (order.status in terminal || order.isArchived) return
        val reminders = reminderRepository.getForOrder(order.id)
        val now = Instant.now().toEpochMilli()
        reminders.forEach { reminder ->
            val delay = reminder.scheduledAt.toEpochMilli() - now
            if (delay > 0) {
                val work = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(ReminderWorker.KEY_REMINDER_ID to reminder.id))
                    .build()
                workManager.enqueueUniqueWork(
                    "reminder_${reminder.id}",
                    ExistingWorkPolicy.REPLACE,
                    work,
                )
            }
        }
    }

    override suspend fun cancelOrderReminders(orderId: String) {
        reminderRepository.getForOrder(orderId).forEach {
            workManager.cancelUniqueWork("reminder_${it.id}")
        }
    }

    override fun setDailySummaryEnabled(enabled: Boolean) {
        if (enabled) {
            val now = ZonedDateTime.now()
            var next = now.with(LocalTime.of(8, 0))
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = Duration.between(now, next).toMillis()
            val work = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "daily_summary",
                ExistingPeriodicWorkPolicy.UPDATE,
                work,
            )
        } else {
            workManager.cancelUniqueWork("daily_summary")
        }
    }
}
