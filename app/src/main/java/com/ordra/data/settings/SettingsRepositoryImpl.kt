package com.ordra.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ThemeMode
import com.ordra.domain.model.UserSettings
import com.ordra.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY = stringPreferencesKey("currency_code")
        val DEFAULT_PLATFORM = stringPreferencesKey("default_platform")
        val DEFAULT_PAYMENT = stringPreferencesKey("default_payment_method")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    private inline fun <reified T : Enum<T>> Preferences.enumOrNull(key: Preferences.Key<String>): T? =
        this[key]?.let { runCatching { enumValueOf<T>(it) }.getOrNull() }

    override val settings: Flow<UserSettings> = dataStore.data.map { prefs ->
        UserSettings(
            themeMode = prefs.enumOrNull<ThemeMode>(Keys.THEME_MODE) ?: ThemeMode.SYSTEM,
            currencyCode = prefs[Keys.CURRENCY] ?: "PHP",
            defaultPlatform = prefs.enumOrNull<Platform>(Keys.DEFAULT_PLATFORM),
            defaultPaymentMethod = prefs.enumOrNull<PaymentMethod>(Keys.DEFAULT_PAYMENT),
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false,
        )
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    override suspend fun setCurrencyCode(currencyCode: String) {
        dataStore.edit { it[Keys.CURRENCY] = currencyCode }
    }

    override suspend fun setDefaultPlatform(platform: Platform?) {
        dataStore.edit {
            if (platform == null) it.remove(Keys.DEFAULT_PLATFORM)
            else it[Keys.DEFAULT_PLATFORM] = platform.name
        }
    }

    override suspend fun setDefaultPaymentMethod(method: PaymentMethod?) {
        dataStore.edit {
            if (method == null) it.remove(Keys.DEFAULT_PAYMENT)
            else it[Keys.DEFAULT_PAYMENT] = method.name
        }
    }

    override suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }
}
