package com.ordra.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCode: String = "PHP",
    val defaultPlatform: Platform? = null,
    val defaultPaymentMethod: PaymentMethod? = null,
    val onboardingComplete: Boolean = false,
)
