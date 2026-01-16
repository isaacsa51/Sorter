package com.serranoie.app.media.sorter.data.settings

import com.serranoie.app.media.sorter.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    
    override val appSettings: Flow<AppSettings> = settingsDataStore.appSettingsFlow
    
    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsDataStore.setThemeMode(themeMode)
    }
    
    override suspend fun setUseDynamicColors(enabled: Boolean) {
        settingsDataStore.setUseDynamicColors(enabled)
    }
    
    override suspend fun setUseBlurredBackground(enabled: Boolean) {
        settingsDataStore.setUseBlurredBackground(enabled)
    }
    
    override suspend fun resetSettings() {
        settingsDataStore.clearSettings()
    }
}
