package com.ordra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ordra.data.local.dao.OrderDao
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity

/**
 * Primary source of truth for Ordra.
 *
 * Migration policy: every schema change bumps [version] and ships an explicit
 * [androidx.room.migration.Migration] or AutoMigrationSpec. Destructive fallback
 * is never enabled — losing user order data is not an acceptable migration path.
 * Schema JSON is exported to app/schemas for review and migration testing.
 */
@Database(
    entities = [OrderEntity::class, OrderItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class OrdraDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
}
