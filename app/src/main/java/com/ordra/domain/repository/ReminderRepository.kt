package com.ordra.domain.repository

import com.ordra.domain.model.Reminder
import com.ordra.domain.model.ReminderType
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeForOrder(orderId: String): Flow<List<Reminder>>
    suspend fun getForOrder(orderId: String): List<Reminder>
    suspend fun getReminder(reminderId: String): Reminder?
    /** Rebuilds an order's reminders from selected types + delivery date (09:00 local). */
    suspend fun setOrderReminders(
        orderId: String,
        types: Set<ReminderType>,
        deliveryDate: LocalDate?,
    )
}
