package com.serranoie.app.media.sorter.presentation.update.service

import android.app.Activity
import javax.inject.Inject

class PlayStoreUpdateChecker @Inject constructor() {

	suspend fun checkForUpdates(
		activity: Activity, isCritical: Boolean = false
	): PlayStoreUpdateResult {
		return PlayStoreUpdateResult(
			hasUpdate = false, updateType = null, appUpdateInfo = null, error = null
		)
	}
}

data class PlayStoreUpdateResult(
	val hasUpdate: Boolean,
	val updateType: Int? = null,
	val appUpdateInfo: Any? = null,
	val error: String? = null
)