package com.serranoie.app.media.sorter.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsDataStore(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val USE_DYNAMIC_COLORS_KEY = booleanPreferencesKey("use_dynamic_colors")
        private val USE_BLURRED_BACKGROUND_KEY = booleanPreferencesKey("use_blurred_background")
        
        private const val DEFAULT_THEME_MODE = "SYSTEM"
        private const val DEFAULT_USE_DYNAMIC_COLORS = true
        private const val DEFAULT_USE_BLURRED_BACKGROUND = true
    }
    
    val appSettingsFlow: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            themeMode = ThemeMode.valueOf(
                preferences[THEME_MODE_KEY] ?: DEFAULT_THEME_MODE
            ),
            useDynamicColors = preferences[USE_DYNAMIC_COLORS_KEY] ?: DEFAULT_USE_DYNAMIC_COLORS,
            useBlurredBackground = preferences[USE_BLURRED_BACKGROUND_KEY] ?: DEFAULT_USE_BLURRED_BACKGROUND
        )
    }
    
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun setUseDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS_KEY] = enabled
        }
    }
    
    suspend fun setUseBlurredBackground(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_BLURRED_BACKGROUND_KEY] = enabled
        }
    }

    suspend fun clearSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
