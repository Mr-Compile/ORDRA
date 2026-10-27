package com.ordra.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currencyCode: String = "PHP",
    val defaultPlatform: Platform? = null,
    val defaultPaymentMethod: PaymentMethod? = null,
    val dailySummaryEnabled: Boolean = false,
    val onlineAnalysisEnabled: Boolean = false,
    val detectionMarketplace: Boolean = false,
    val detectionCourier: Boolean = false,
    val detectionRider: Boolean = false,
    val autoApplyHighConfidence: Boolean = false,
    val aiAssistedDetection: Boolean = false,
    val onboardingComplete: Boolean = false,
)
