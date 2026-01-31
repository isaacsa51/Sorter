package com.serranoie.app.media.sorter.update.service

import com.serranoie.app.media.sorter.update.model.UpdateCheckResult
import com.serranoie.app.media.sorter.update.model.UpdateInfo
import com.serranoie.app.media.sorter.update.model.Version
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Named

interface GitHubApiService {
	@GET("releases/latest")
	suspend fun getLatestRelease(): GitHubRelease
}

class GitHubUpdateChecker @Inject constructor(
	@Named("GitHubRepoOwner") private val owner: String,
	@Named("GitHubRepoName") private val repoName: String
) {

	private val retrofit: Retrofit by lazy {
		Retrofit.Builder().baseUrl("https://api.github.com/repos/$owner/$repoName/")
			.addConverterFactory(GsonConverterFactory.create()).build()
	}

	private val apiService: GitHubApiService by lazy {
		retrofit.create(GitHubApiService::class.java)
	}

	suspend fun checkForUpdates(currentName: String, currentCode: Int): UpdateCheckResult {
		return try {
			val release = apiService.getLatestRelease()

			val latestVersion = Version(release.tagName)
			val currentVersion = Version(currentName)

			val hasUpdate = latestVersion > currentVersion

			if (hasUpdate) {
				val asset = release.assets.firstOrNull {
					it.name.endsWith(".apk") && !it.name.contains("-unsigned")
				} ?: release.assets.firstOrNull { it.name.endsWith(".apk") }

				val severity = UpdateSeverity.fromReleaseBody(release.body)
				val updateInfo = UpdateInfo(
					versionName = release.tagName,
					versionCode = parseVersionToCode(release.tagName),
					downloadUrl = asset?.browserDownloadUrl ?: release.htmlUrl,
					releaseNotes = release.body ?: "",
					isCritical = severity.isCritical,
					minimumRequiredVersion = severity.minimumRequiredVersion
				)

				UpdateCheckResult(
					hasUpdate = true, updateInfo = updateInfo
				)
			} else {
				UpdateCheckResult(hasUpdate = false)
			}
		} catch (e: Exception) {
			UpdateCheckResult(
				hasUpdate = false, error = "Failed to check for updates: ${e.message}"
			)
		}
	}

	private fun parseVersionToCode(version: String): Int {
		val cleaned = version.removePrefix("v")
		val parts = cleaned.split(".")
		return if (parts.size >= 3) {
			val major = parts[0].toIntOrNull() ?: 0
			val minor = parts[1].toIntOrNull() ?: 0
			val patch = parts[2].toIntOrNull() ?: 0
			major * 10000 + minor * 100 + patch
		} else 0
	}
}

data class UpdateSeverity(
	val isCritical: Boolean, val minimumRequiredVersion: String? = null
) {
	companion object {
		fun fromReleaseBody(body: String?): UpdateSeverity {
			val lines = body?.lines() ?: emptyList()
			val isCritical = lines.any { it.contains("🔴 CRITICAL", ignoreCase = true) }
			val minimumRequiredVersion =
				lines.firstOrNull { it.contains("minimum_version:", ignoreCase = true) }
					?.substringAfter("minimum_version:")?.trim()

			return UpdateSeverity(isCritical, minimumRequiredVersion)
		}
	}
}