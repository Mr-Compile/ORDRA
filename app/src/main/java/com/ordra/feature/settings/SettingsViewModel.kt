package com.ordra.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ordra.domain.model.PaymentMethod
import com.ordra.domain.model.Platform
import com.ordra.domain.model.ThemeMode
import com.ordra.domain.model.UserSettings
import com.ordra.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<UserSettings?> = repository.settings
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        repository.setThemeMode(mode)
    }

    fun setCurrency(code: String) = viewModelScope.launch {
        if (code.length == 3) repository.setCurrencyCode(code.uppercase())
    }

    fun setDefaultPlatform(platform: Platform?) = viewModelScope.launch {
        repository.setDefaultPlatform(platform)
    }

    fun setDefaultPaymentMethod(method: PaymentMethod?) = viewModelScope.launch {
        repository.setDefaultPaymentMethod(method)
    }
}
