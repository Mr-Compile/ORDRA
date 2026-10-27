package com.ordra.app.di

import com.ordra.data.repository.CategoryRepositoryImpl
import com.ordra.data.repository.OrderRepositoryImpl
import com.ordra.core.notifications.ReminderSchedulerImpl
import com.ordra.core.ocr.OcrEngine
import com.ordra.data.ocr.MlKitOcrEngine
import com.ordra.data.repository.BackupRepositoryImpl
import com.ordra.data.repository.DetectionRepositoryImpl
import com.ordra.data.repository.PostPurchaseRepositoryImpl
import com.ordra.data.repository.ReminderRepositoryImpl
import com.ordra.data.repository.ShipmentRepositoryImpl
import com.ordra.data.settings.SettingsRepositoryImpl
import com.ordra.domain.repository.CategoryRepository
import com.ordra.domain.repository.OrderRepository
import com.ordra.domain.repository.SettingsRepository
import com.ordra.domain.repository.BackupRepository
import com.ordra.domain.repository.DetectionRepository
import com.ordra.domain.repository.PostPurchaseRepository
import com.ordra.domain.repository.ReminderRepository
import com.ordra.domain.repository.ReminderScheduler
import com.ordra.domain.repository.ShipmentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindShipmentRepository(impl: ShipmentRepositoryImpl): ShipmentRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: ReminderSchedulerImpl): ReminderScheduler

    @Binds
    @Singleton
    abstract fun bindOcrEngine(impl: MlKitOcrEngine): OcrEngine

    @Binds
    @Singleton
    abstract fun bindDetectionRepository(impl: DetectionRepositoryImpl): DetectionRepository

    @Binds
    @Singleton
    abstract fun bindPostPurchaseRepository(impl: PostPurchaseRepositoryImpl): PostPurchaseRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
