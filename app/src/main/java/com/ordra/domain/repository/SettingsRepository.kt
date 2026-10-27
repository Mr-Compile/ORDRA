package com.ordra.domain.repository

import com.ordra.domain.model.ThemeMode
import com.ordra.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setCurrencyCode(currencyCode: String)
    suspend fun setOnboardingComplete(complete: Boolean)
}
