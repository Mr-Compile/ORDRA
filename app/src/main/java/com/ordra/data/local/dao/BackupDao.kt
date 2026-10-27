package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ordra.data.local.entity.CategoryEntity
import com.ordra.data.local.entity.OrderCategoryEntity
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity
import com.ordra.data.local.entity.ReceiptEntity
import com.ordra.data.local.entity.RefundEntity
import com.ordra.data.local.entity.ReminderEntity
import com.ordra.data.local.entity.ReturnEntity
import com.ordra.data.local.entity.ShipmentEntity
import com.ordra.data.local.entity.TrackingEventEntity
import com.ordra.data.local.entity.WarrantyEntity

/** Whole-table access for backup/export. Used only by BackupRepository. */
@Dao
interface BackupDao {
    @Query("SELECT * FROM orders") suspend fun allOrders(): List<OrderEntity>
    @Query("SELECT * FROM order_items") suspend fun allItems(): List<OrderItemEntity>
    @Query("SELECT * FROM shipments") suspend fun allShipments(): List<ShipmentEntity>
    @Query("SELECT * FROM tracking_events") suspend fun allTrackingEvents(): List<TrackingEventEntity>
    @Query("SELECT * FROM categories") suspend fun allCategories(): List<CategoryEntity>
    @Query("SELECT * FROM order_categories") suspend fun allOrderCategories(): List<OrderCategoryEntity>
    @Query("SELECT * FROM reminders") suspend fun allReminders(): List<ReminderEntity>
    @Query("SELECT * FROM returns") suspend fun allReturns(): List<ReturnEntity>
    @Query("SELECT * FROM refunds") suspend fun allRefunds(): List<RefundEntity>
    @Query("SELECT * FROM warranties") suspend fun allWarranties(): List<WarrantyEntity>
    @Query("SELECT * FROM receipts") suspend fun allReceipts(): List<ReceiptEntity>

    @Upsert suspend fun upsertOrders(v: List<OrderEntity>)
    @Upsert suspend fun upsertItems(v: List<OrderItemEntity>)
    @Upsert suspend fun upsertShipments(v: List<ShipmentEntity>)
    @Upsert suspend fun upsertTrackingEvents(v: List<TrackingEventEntity>)
    @Upsert suspend fun upsertCategories(v: List<CategoryEntity>)
    @Upsert suspend fun upsertOrderCategories(v: List<OrderCategoryEntity>)
    @Upsert suspend fun upsertReminders(v: List<ReminderEntity>)
    @Upsert suspend fun upsertReturns(v: List<ReturnEntity>)
    @Upsert suspend fun upsertRefunds(v: List<RefundEntity>)
    @Upsert suspend fun upsertWarranties(v: List<WarrantyEntity>)
    @Upsert suspend fun upsertReceipts(v: List<ReceiptEntity>)
}
