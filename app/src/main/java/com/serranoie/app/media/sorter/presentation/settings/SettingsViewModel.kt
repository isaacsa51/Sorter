package com.serranoie.app.media.sorter.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serranoie.app.media.sorter.data.settings.AppSettings
import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.domain.settings.GetAppSettingsUseCase
import com.serranoie.app.media.sorter.domain.settings.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getAppSettings: GetAppSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase
) : ViewModel() {
    

    val appSettings: StateFlow<AppSettings> = getAppSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings() // Default settings
        )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            updateSettings.setThemeMode(themeMode)
        }
    }
    

    fun toggleTheme() {
        viewModelScope.launch {
            val currentMode = appSettings.value.themeMode
            val newMode = when (currentMode) {
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.LIGHT
                ThemeMode.SYSTEM -> ThemeMode.DARK // If system, switch to dark
            }
            updateSettings.setThemeMode(newMode)
        }
    }

    fun setUseDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            updateSettings.setUseDynamicColors(enabled)
        }
    }

    fun toggleDynamicColors() {
        viewModelScope.launch {
            val current = appSettings.value.useDynamicColors
            updateSettings.setUseDynamicColors(!current)
        }
    }

    fun setUseBlurredBackground(enabled: Boolean) {
        viewModelScope.launch {
            updateSettings.setUseBlurredBackground(enabled)
        }
    }

    fun toggleBlurredBackground() {
        viewModelScope.launch {
            val current = appSettings.value.useBlurredBackground
            updateSettings.setUseBlurredBackground(!current)
        }
    }
    

    fun markTutorialCompleted() {
        viewModelScope.launch {
            updateSettings.setTutorialCompleted(true)
        }
    }
    
    fun resetTutorial() {
        viewModelScope.launch {
            updateSettings.resetTutorial()
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            updateSettings.resetSettings()
        }
    }
}
