package com.serranoie.app.media.sorter.domain.repository

import com.serranoie.app.media.sorter.data.settings.AppSettings
import com.serranoie.app.media.sorter.data.settings.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val appSettings: Flow<AppSettings>

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setUseDynamicColors(enabled: Boolean)
    
    suspend fun setUseBlurredBackground(enabled: Boolean)

    suspend fun resetSettings()
}
