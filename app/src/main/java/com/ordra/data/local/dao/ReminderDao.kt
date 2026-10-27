package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.ordra.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Upsert
    suspend fun upsert(reminder: ReminderEntity)

    @Upsert
    suspend fun upsertAll(reminders: List<ReminderEntity>)

    @Query("SELECT * FROM reminders WHERE orderId = :orderId ORDER BY scheduledAt ASC")
    fun observeForOrder(orderId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminder(reminderId: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE orderId = :orderId")
    suspend fun getForOrder(orderId: String): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE orderId = :orderId")
    suspend fun deleteForOrder(orderId: String)

    @Transaction
    suspend fun replaceForOrder(orderId: String, reminders: List<ReminderEntity>) {
        deleteForOrder(orderId)
        upsertAll(reminders)
    }

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun delete(reminderId: String)
}
