package com.serranoie.app.media.sorter.update.service

import android.content.Context
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.update.model.UpdateCheckResult
import com.serranoie.app.media.sorter.update.model.UpdateInfo
import com.serranoie.app.media.sorter.update.model.Version
import dagger.hilt.android.qualifiers.ApplicationContext
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
	@ApplicationContext private val context: Context,
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

			println("=== Update Check Debug ===")
			println("Release tag: ${release.tagName}")
			println("Release name: ${release.name}")
			println("Current version: $currentName")
			println("Prerelease: ${release.prerelease}")

			val latestVersion = Version(release.tagName)
			val currentVersion = Version(currentName)

			println("Latest version parsed: $latestVersion")
			println("Current version parsed: $currentVersion")
			println("Has update: $latestVersion > $currentVersion}")

			val hasUpdate = latestVersion > currentVersion
			println("Has update result: $hasUpdate")

			// Skip prerelease releases
			if (release.prerelease) {
				println("Skipping prerelease release")
				return UpdateCheckResult(hasUpdate = false, error = "Release is prerelease")
			}

			if (hasUpdate) {
				val asset = release.assets.firstOrNull {
					it.name.endsWith(".apk") && !it.name.contains("-unsigned")
				} ?: release.assets.firstOrNull { it.name.endsWith(".apk") }

				println("Found asset: ${asset?.name}")
				println("Download URL: ${asset?.browserDownloadUrl}")

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
			println("Update check error: ${e.message}")
			e.printStackTrace()
			
			val errorMessage = when {
				e is java.net.UnknownHostException || 
				e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
				e.message?.contains("No address associated with hostname", ignoreCase = true) == true -> {
					context.getString(R.string.update_check_no_internet)
				}
				e.message?.contains("timeout", ignoreCase = true) == true -> {
					context.getString(R.string.update_check_timeout)
				}
				e.message?.contains("404", ignoreCase = true) == true -> {
					context.getString(R.string.update_check_no_releases)
				}
				else -> context.getString(R.string.update_check_failed, e.message ?: "Unknown error")
			}
			
			UpdateCheckResult(
				hasUpdate = false, error = errorMessage
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