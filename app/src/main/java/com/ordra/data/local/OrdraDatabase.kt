package com.ordra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ordra.data.local.dao.BackupDao
import com.ordra.data.local.dao.CategoryDao
import com.ordra.data.local.dao.DetectionDao
import com.ordra.data.local.dao.PostPurchaseDao
import com.ordra.data.local.dao.OrderDao
import com.ordra.data.local.dao.ReminderDao
import com.ordra.data.local.dao.ShipmentDao
import com.ordra.data.local.dao.TrackingEventDao
import com.ordra.data.local.entity.CategoryEntity
import com.ordra.data.local.entity.DetectionSuggestionEntity
import com.ordra.data.local.entity.OrderCategoryEntity
import com.ordra.data.local.entity.OrderEntity
import com.ordra.data.local.entity.OrderItemEntity
import com.ordra.data.local.entity.ReceiptEntity
import com.ordra.data.local.entity.RefundEntity
import com.ordra.data.local.entity.ReminderEntity
import com.ordra.data.local.entity.ReturnEntity
import com.ordra.data.local.entity.WarrantyEntity
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
        ReminderEntity::class,
        DetectionSuggestionEntity::class,
        ReturnEntity::class,
        RefundEntity::class,
        WarrantyEntity::class,
        ReceiptEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class OrdraDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun shipmentDao(): ShipmentDao
    abstract fun trackingEventDao(): TrackingEventDao
    abstract fun categoryDao(): CategoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun detectionDao(): DetectionDao
    abstract fun postPurchaseDao(): PostPurchaseDao
    abstract fun backupDao(): BackupDao

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

        /** v3 -> v4: reminders table for WorkManager-backed local reminders. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `orderId` TEXT,
                        `type` TEXT NOT NULL,
                        `scheduledAt` INTEGER NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        `notificationId` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_orderId` ON `reminders` (`orderId`)")
            }
        }

        /** v4 -> v5: detection suggestions for notification/share assistance. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `detection_suggestions` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `shipmentId` TEXT,
                        `orderId` TEXT,
                        `status` TEXT NOT NULL,
                        `courier` TEXT,
                        `trackingNumber` TEXT,
                        `source` TEXT NOT NULL,
                        `confidence` TEXT NOT NULL,
                        `state` TEXT NOT NULL,
                        `needsParcelPick` INTEGER NOT NULL,
                        `notificationPackage` TEXT,
                        `summary` TEXT,
                        `detectedAt` INTEGER NOT NULL,
                        `appliedAt` INTEGER
                    )
                    """.trimIndent(),
                )
            }
        }

        /** v5 -> v6: post-purchase tables + order rating/favorite columns. */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `rating` INTEGER")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `isFavorite` INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `returns` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `orderId` TEXT NOT NULL,
                        `reason` TEXT,
                        `status` TEXT NOT NULL,
                        `requestedAt` INTEGER NOT NULL,
                        `resolvedAt` INTEGER,
                        `notes` TEXT,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `refunds` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `orderId` TEXT NOT NULL,
                        `amountMinor` INTEGER,
                        `status` TEXT NOT NULL,
                        `reason` TEXT,
                        `requestedAt` INTEGER NOT NULL,
                        `processedAt` INTEGER,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `warranties` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `orderId` TEXT NOT NULL,
                        `provider` TEXT,
                        `startDate` TEXT NOT NULL,
                        `endDate` TEXT NOT NULL,
                        `notes` TEXT,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `receipts` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `orderId` TEXT NOT NULL,
                        `imageUri` TEXT NOT NULL,
                        `label` TEXT,
                        `addedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                listOf("returns", "refunds", "warranties", "receipts").forEach { t ->
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_${t}_orderId` ON `$t` (`orderId`)")
                }
            }
        }
    }
}
