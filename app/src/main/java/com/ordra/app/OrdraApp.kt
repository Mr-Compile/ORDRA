package com.ordra.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.ordra.app.navigation.OrdraNavHost
import com.ordra.core.ui.theme.OrdraTheme
import com.ordra.domain.model.ThemeMode
import com.ordra.domain.model.UserSettings
import com.ordra.domain.repository.SettingsRepository
import com.ordra.feature.onboarding.OnboardingScreen
import kotlinx.coroutines.launch

@Composable
fun OrdraApp(settingsRepository: SettingsRepository) {
    val settings by produceState<UserSettings?>(initialValue = null) {
        settingsRepository.settings.collect { value = it }
    }
    val scope = rememberCoroutineScope()
    val systemDark = isSystemInDarkTheme()

    val current = settings ?: return // brief load — splash stays visible
    val darkTheme = when (current.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    OrdraTheme(darkTheme = darkTheme) {
        if (!current.onboardingComplete) {
            OnboardingScreen(
                onFinish = { apiKey ->
                    scope.launch {
                        if (apiKey.isNotBlank()) settingsRepository.setGroqApiKey(apiKey)
                        settingsRepository.setOnboardingComplete(true)
                    }
                },
            )
        } else {
            OrdraNavHost(navController = rememberNavController())
        }
    }
}
