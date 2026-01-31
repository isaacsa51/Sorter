package com.serranoie.app.media.sorter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.updateDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_store")

@Singleton
class UpdatePreferences @Inject constructor(
	@ApplicationContext private val context: Context
) {
	private object PreferencesKeys {
		val LAST_CHECKED_VERSION = stringPreferencesKey("last_checked_version")
		val LAST_CHECK_TIME = stringPreferencesKey("last_check_time")
		val DISMISSED_UPDATE_VERSION = stringPreferencesKey("dismissed_update_version")
	}

	val lastCheckedVersion: Flow<String?> =
		context.updateDataStore.data.map { it[PreferencesKeys.LAST_CHECKED_VERSION] }

	val lastCheckTime: Flow<Long?> =
		context.updateDataStore.data.map { it[PreferencesKeys.LAST_CHECK_TIME]?.toLong() }

	val dismissedUpdateVersion: Flow<String?> =
		context.updateDataStore.data.map { it[PreferencesKeys.DISMISSED_UPDATE_VERSION] }

	suspend fun saveLastCheckedVersion(version: String) {
		context.updateDataStore.edit { preferences ->
			preferences[PreferencesKeys.LAST_CHECKED_VERSION] = version
			preferences[PreferencesKeys.LAST_CHECK_TIME] = System.currentTimeMillis().toString()
		}
	}

	suspend fun saveDismissedUpdateVersion(version: String) {
		context.updateDataStore.edit { preferences ->
			preferences[PreferencesKeys.DISMISSED_UPDATE_VERSION] = version
		}
	}

	suspend fun clearDismissedUpdateVersion() {
		context.updateDataStore.edit { preferences ->
			preferences.remove(PreferencesKeys.DISMISSED_UPDATE_VERSION)
		}
	}
}