package com.ordra.app.di

import android.content.Context
import androidx.room.Room
import com.ordra.data.local.OrdraDatabase
import com.ordra.data.local.dao.OrderDao
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
            .build()

    @Provides
    fun provideOrderDao(database: OrdraDatabase): OrderDao = database.orderDao()
}
