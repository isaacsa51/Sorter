package com.serranoie.app.media.sorter.update

import android.app.Activity
import android.content.Context
import com.serranoie.app.media.sorter.data.UpdatePreferences
import com.serranoie.app.media.sorter.update.model.UpdateCheckResult
import com.serranoie.app.media.sorter.update.model.UpdateInfo
import com.serranoie.app.media.sorter.update.model.UpdateSource
import com.serranoie.app.media.sorter.update.service.GitHubUpdateChecker
import com.serranoie.app.media.sorter.update.service.PlayStoreUpdateChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.util.prefs.Preferences
import javax.inject.Inject
import javax.inject.Named
import android.content.pm.PackageManager.NameNotFoundException
import com.serranoie.app.media.sorter.update.model.Version

class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updatePreferences: UpdatePreferences,
    private val githubUpdateChecker: GitHubUpdateChecker,
    private val playStoreUpdateChecker: PlayStoreUpdateChecker,
    @Named("GitHubRepoOwner") private val githubRepoOwner: String,
    @Named("GitHubRepoName") private val githubRepoName: String
) {
    private val packageInfo get() = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: NameNotFoundException) {
        null
    }

    val currentVersionName: String
        get() = packageInfo?.versionName ?: "1.0"

    val currentVersionCode: Int
        get() {
            @Suppress("DEPRECATION")
            return packageInfo?.versionCode ?: 1
        }

    suspend fun checkForUpdates(forceCheck: Boolean = false): UpdateCheckResponse {
        val lastCheckedVersion = updatePreferences.lastCheckedVersion.firstOrNull()
        val lastCheckTimeValue = try {
            updatePreferences.lastCheckTime.firstOrNull()?.toLong()
        } catch (e: Exception) {
            null
        }

        val eightHoursAgo = System.currentTimeMillis() - (8 * 60 * 60 * 1000)

        if (!forceCheck && lastCheckedVersion == currentVersionName && lastCheckTimeValue != null && lastCheckTimeValue > eightHoursAgo) {
            return UpdateCheckResponse(
                hasUpdate = false,
                source = UpdateSource.GITHUB,
                skipReason = "Recently checked"
            )
        }

        val isFromPlayStore = isInstalledFromPlayStore()
        val source = if (isFromPlayStore) UpdateSource.PLAY_STORE else UpdateSource.GITHUB

        return when (source) {
            UpdateSource.PLAY_STORE -> checkPlayStoreUpdates(forceCheck)
            UpdateSource.GITHUB -> checkGitHubUpdates(forceCheck)
        }
    }

    private suspend fun checkPlayStoreUpdates(forceCheck: Boolean): UpdateCheckResponse {
        return UpdateCheckResponse(
            hasUpdate = false,
            source = UpdateSource.PLAY_STORE,
            message = "Play Store updates are handled by the store"
        )
    }

    private suspend fun checkGitHubUpdates(forceCheck: Boolean): UpdateCheckResponse {
        val result = githubUpdateChecker.checkForUpdates(currentVersionName, currentVersionCode)

        if (result.hasUpdate && result.updateInfo != null) {
            updatePreferences.saveLastCheckedVersion(currentVersionName)
            return UpdateCheckResponse(
                hasUpdate = true,
                source = UpdateSource.GITHUB,
                updateInfo = result.updateInfo,
                message = "Update available from GitHub releases"
            )
        }

        updatePreferences.saveLastCheckedVersion(currentVersionName)
        return UpdateCheckResponse(
            hasUpdate = false,
            source = UpdateSource.GITHUB
        )
    }

    fun shouldForceUpdate(updateInfo: UpdateInfo?): Boolean {
        if (updateInfo == null) return false

        if (updateInfo.isCritical) return true

        val minimumRequiredVersion = updateInfo.minimumRequiredVersion
        if (minimumRequiredVersion != null) {
            val currentVersion = Version(currentVersionName)
            val requiredVersion = Version(minimumRequiredVersion)
            return currentVersion < requiredVersion
        }

        return false
    }

    suspend fun markUpdateDismissed(version: String) {
        updatePreferences.saveDismissedUpdateVersion(version)
    }

    suspend fun clearDismissedUpdate() {
        updatePreferences.clearDismissedUpdateVersion()
    }

    suspend fun getDismissedUpdateVersion(): String? {
        return updatePreferences.dismissedUpdateVersion.firstOrNull()
    }

    private fun isInstalledFromPlayStore(): Boolean {
        val installerPackageName = context.packageManager.getInstallerPackageName(context.packageName)
        return installerPackageName == "com.android.vending"
    }
}

data class UpdateCheckResponse(
    val hasUpdate: Boolean,
    val source: UpdateSource,
    val updateInfo: UpdateInfo? = null,
    val message: String? = null,
    val skipReason: String? = null
)