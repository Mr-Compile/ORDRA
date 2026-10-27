package com.ordra.domain.repository

import com.ordra.domain.model.Order

/** Wraps WorkManager so reminders survive reboot and reschedule on changes. */
interface ReminderScheduler {
    /** Cancel + re-enqueue all reminder work for [orderId]. No-op for terminal orders. */
    suspend fun syncOrderReminders(order: Order)
    suspend fun cancelOrderReminders(orderId: String)
    fun setDailySummaryEnabled(enabled: Boolean)
}
