package com.ordra.data.repository

import com.ordra.data.local.dao.ReminderDao
import com.ordra.data.local.entity.ReminderEntity
import com.ordra.domain.model.Reminder
import com.ordra.domain.model.ReminderType
import com.ordra.domain.repository.ReminderRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val REMINDER_HOUR = 9

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val dao: ReminderDao,
) : ReminderRepository {

    private fun toDomain(e: ReminderEntity) = Reminder(
        id = e.id,
        orderId = e.orderId,
        type = ReminderType.valueOf(e.type),
        scheduledAt = Instant.ofEpochMilli(e.scheduledAt),
        enabled = e.enabled,
        notificationId = e.notificationId,
        createdAt = Instant.ofEpochMilli(e.createdAt),
    )

    private fun toEntity(d: Reminder) = ReminderEntity(
        id = d.id,
        orderId = d.orderId,
        type = d.type.name,
        scheduledAt = d.scheduledAt.toEpochMilli(),
        enabled = d.enabled,
        notificationId = d.notificationId,
        createdAt = d.createdAt.toEpochMilli(),
    )

    override fun observeForOrder(orderId: String): Flow<List<Reminder>> =
        dao.observeForOrder(orderId).map { list -> list.map(::toDomain) }

    override suspend fun getForOrder(orderId: String): List<Reminder> =
        dao.getForOrder(orderId).map(::toDomain)

    override suspend fun getReminder(reminderId: String): Reminder? =
        dao.getReminder(reminderId)?.let(::toDomain)

    override suspend fun setOrderReminders(
        orderId: String,
        types: Set<ReminderType>,
        deliveryDate: LocalDate?,
    ) {
        if (types.isEmpty() || deliveryDate == null) {
            dao.deleteForOrder(orderId)
            return
        }
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val existing = dao.getForOrder(orderId)
            .map(::toDomain)
            .associateBy { it.type }
        val reminders = types.mapNotNull { type ->
            val day = when (type) {
                ReminderType.DELIVERY_DAY -> deliveryDate
                ReminderType.DAY_BEFORE -> deliveryDate.minusDays(1)
                ReminderType.TWO_DAYS_BEFORE -> deliveryDate.minusDays(2)
                ReminderType.CUSTOM -> return@mapNotNull existing[type]
            }
            val at = day.atTime(LocalTime.of(REMINDER_HOUR, 0))
                .atZone(zone).toInstant()
            if (at.isBefore(now)) return@mapNotNull null
            val prior = existing[type]
            Reminder(
                id = prior?.id ?: UUID.randomUUID().toString(),
                orderId = orderId,
                type = type,
                scheduledAt = at,
                enabled = true,
                notificationId = prior?.notificationId,
                createdAt = prior?.createdAt ?: now,
            )
        }
        dao.replaceForOrder(orderId, reminders.map(::toEntity))
    }
}
