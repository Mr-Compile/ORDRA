package com.ordra.domain.model

import java.time.Instant

enum class ReminderType { DELIVERY_DAY, DAY_BEFORE, TWO_DAYS_BEFORE, CUSTOM }

data class Reminder(
    val id: String,
    val orderId: String?,
    val type: ReminderType,
    val scheduledAt: Instant,
    val enabled: Boolean = true,
    val notificationId: Int? = null,
    val createdAt: Instant,
)
