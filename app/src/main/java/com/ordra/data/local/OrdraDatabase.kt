package com.ordra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ordra.data.local.dao.CategoryDao
import com.ordra.data.local.dao.OrderDao
import com.ordra.data.local.dao.ShipmentDao
import com.ordra.data.local.dao.TrackingEventDao
import com.ordra.data.local.entity.CategoryEntity
import com.ordra.data.local.entity.OrderCategoryEntity
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity
import com.ordra.data.local.entity.ShipmentEntity
import com.ordra.data.local.entity.TrackingEventEntity

/**
 * Primary source of truth for Ordra.
 *
 * Migration policy: every schema change bumps [version] and ships an explicit
 * [androidx.room.migration.Migration] or AutoMigrationSpec. Destructive fallback
 * is never enabled — losing user order data is not an acceptable migration path.
 * Schema JSON is exported to app/schemas for review and migration testing.
 */
@Database(
    entities = [
        OrderEntity::class,
        OrderItemEntity::class,
        ShipmentEntity::class,
        TrackingEventEntity::class,
        CategoryEntity::class,
        OrderCategoryEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class OrdraDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun shipmentDao(): ShipmentDao
    abstract fun trackingEventDao(): TrackingEventDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        /**
         * v1 -> v2: courier/trackingNumber move off `orders` onto the new
         * `shipments` table (No-API Smart Parcel Status architecture).
         * Orders is rebuilt via copy because DROP COLUMN requires SQLite 3.35+.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `orders_new` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `title` TEXT NOT NULL,
                        `platform` TEXT NOT NULL,
                        `customPlatformName` TEXT,
                        `sellerName` TEXT,
                        `status` TEXT NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `paymentStatus` TEXT NOT NULL,
                        `isCod` INTEGER NOT NULL,
                        `currencyCode` TEXT NOT NULL,
                        `itemSubtotalMinor` INTEGER,
                        `shippingFeeMinor` INTEGER,
                        `discountMinor` INTEGER,
                        `totalAmountMinor` INTEGER,
                        `orderedDate` INTEGER,
                        `estimatedDeliveryStart` INTEGER,
                        `estimatedDeliveryEnd` INTEGER,
                        `deliveredAt` INTEGER,
                        `notes` TEXT,
                        `productImageUri` TEXT,
                        `archivedAt` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    INSERT INTO `orders_new` (
                        id, title, platform, customPlatformName, sellerName, status,
                        paymentMethod, paymentStatus, isCod, currencyCode,
                        itemSubtotalMinor, shippingFeeMinor, discountMinor, totalAmountMinor,
                        orderedDate, estimatedDeliveryStart, estimatedDeliveryEnd, deliveredAt,
                        notes, productImageUri, archivedAt, createdAt, updatedAt
                    )
                    SELECT
                        id, title, platform, customPlatformName, sellerName, status,
                        paymentMethod, paymentStatus, isCod, currencyCode,
                        itemSubtotalMinor, shippingFeeMinor, discountMinor, totalAmountMinor,
                        orderedDate, estimatedDeliveryStart, estimatedDeliveryEnd, deliveredAt,
                        notes, productImageUri, archivedAt, createdAt, updatedAt
                    FROM `orders`
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE `orders`")
                db.execSQL("ALTER TABLE `orders_new` RENAME TO `orders`")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `shipments` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `orderId` TEXT NOT NULL,
                        `courier` TEXT NOT NULL,
                        `customCourierName` TEXT,
                        `trackingNumber` TEXT,
                        `shipmentStatus` TEXT NOT NULL,
                        `statusSource` TEXT NOT NULL,
                        `statusConfidence` TEXT NOT NULL,
                        `lastStatusAt` INTEGER,
                        `lastDetectedAt` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shipments_orderId` ON `shipments` (`orderId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `tracking_events` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `shipmentId` TEXT NOT NULL,
                        `shipmentStatus` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT,
                        `occurredAt` INTEGER NOT NULL,
                        `detectedAt` INTEGER NOT NULL,
                        `source` TEXT NOT NULL,
                        `confidence` TEXT NOT NULL,
                        `rawReference` TEXT,
                        FOREIGN KEY(`shipmentId`) REFERENCES `shipments`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tracking_events_shipmentId` ON `tracking_events` (`shipmentId`)")
            }
        }

        /** v2 -> v3: categories + order↔category junction for Phase 1D. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `order_categories` (
                        `orderId` TEXT NOT NULL,
                        `categoryId` TEXT NOT NULL,
                        PRIMARY KEY(`orderId`, `categoryId`),
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_categories_categoryId` ON `order_categories` (`categoryId`)")
            }
        }
    }
}
