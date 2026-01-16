package com.serranoie.app.media.sorter.domain.settings

import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun setThemeMode(themeMode: ThemeMode) {
        repository.setThemeMode(themeMode)
    }
    
    suspend fun setUseDynamicColors(enabled: Boolean) {
        repository.setUseDynamicColors(enabled)
    }
    
    suspend fun setUseBlurredBackground(enabled: Boolean) {
        repository.setUseBlurredBackground(enabled)
    }
    
    suspend fun resetSettings() {
        repository.resetSettings()
    }
}
