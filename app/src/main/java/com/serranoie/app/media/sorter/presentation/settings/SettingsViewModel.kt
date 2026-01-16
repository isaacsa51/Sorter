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

/**
 * ViewModel for managing app settings
 * Observes settings changes and provides methods to update them
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    getAppSettings: GetAppSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase
) : ViewModel() {
    
    /**
     * StateFlow of current app settings
     * Automatically updates UI when settings change
     */
    val appSettings: StateFlow<AppSettings> = getAppSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings() // Default settings
        )
    
    /**
     * Updates the theme mode (Light, Dark, or System)
     */
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            updateSettings.setThemeMode(themeMode)
        }
    }
    
    /**
     * Toggles the theme between Light and Dark
     * (Does not toggle System mode)
     */
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
    
    /**
     * Updates the dynamic colors setting (Material You)
     */
    fun setUseDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            updateSettings.setUseDynamicColors(enabled)
        }
    }
    
    /**
     * Toggles dynamic colors on/off
     */
    fun toggleDynamicColors() {
        viewModelScope.launch {
            val current = appSettings.value.useDynamicColors
            updateSettings.setUseDynamicColors(!current)
        }
    }
    
    /**
     * Updates the blurred background setting for media screen
     */
    fun setUseBlurredBackground(enabled: Boolean) {
        viewModelScope.launch {
            updateSettings.setUseBlurredBackground(enabled)
        }
    }
    
    /**
     * Toggles blurred background on/off
     */
    fun toggleBlurredBackground() {
        viewModelScope.launch {
            val current = appSettings.value.useBlurredBackground
            updateSettings.setUseBlurredBackground(!current)
        }
    }
    
    /**
     * Resets all settings to their default values
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            updateSettings.resetSettings()
        }
    }
}
