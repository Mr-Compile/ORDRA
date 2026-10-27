package com.ordra.domain.repository

import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ThemeMode
import com.ordra.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setCurrencyCode(currencyCode: String)
    suspend fun setDefaultPlatform(platform: Platform?)
    suspend fun setDefaultPaymentMethod(method: PaymentMethod?)
    suspend fun setOnlineAnalysisEnabled(enabled: Boolean)
    suspend fun setDailySummaryEnabled(enabled: Boolean)
    suspend fun setOnboardingComplete(complete: Boolean)
}
