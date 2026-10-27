package com.ordra.app.di

import android.content.Context
import androidx.room.Room
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.dao.CategoryDao
import com.ordra.data.local.dao.DetectionDao
import com.ordra.data.local.dao.OrderDao
import com.ordra.data.local.dao.ReminderDao
import com.ordra.data.local.dao.ShipmentDao
import com.ordra.data.local.dao.TrackingEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OrdraDatabase =
        Room.databaseBuilder(context, OrdraDatabase::class.java, "ordra.db")
            .addMigrations(
                OrdraDatabase.MIGRATION_1_2,
                OrdraDatabase.MIGRATION_2_3,
                OrdraDatabase.MIGRATION_3_4,
                OrdraDatabase.MIGRATION_4_5,
            )
            .build()

    @Provides
    fun provideOrderDao(database: OrdraDatabase): OrderDao = database.orderDao()

    @Provides
    fun provideShipmentDao(database: OrdraDatabase): ShipmentDao = database.shipmentDao()

    @Provides
    fun provideTrackingEventDao(database: OrdraDatabase): TrackingEventDao =
        database.trackingEventDao()

    @Provides
    fun provideCategoryDao(database: OrdraDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideReminderDao(database: OrdraDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideDetectionDao(database: OrdraDatabase): DetectionDao = database.detectionDao()
}
