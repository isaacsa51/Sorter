package com.serranoie.app.media.sorter.update.model

data class UpdateInfo(
	val versionName: String,
	val versionCode: Int,
	val downloadUrl: String,
	val releaseNotes: String = "",
	val isCritical: Boolean = false,
	val minimumRequiredVersion: String? = null
)

data class UpdateCheckResult(
	val hasUpdate: Boolean, val updateInfo: UpdateInfo? = null, val error: String? = null
)

enum class UpdateSource {
	GITHUB, PLAY_STORE
}

data class Version(val name: String) : Comparable<Version> {
	private val parts = name.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

	override fun compareTo(other: Version): Int {
		for (i in 0 until maxOf(parts.size, other.parts.size)) {
			val thisPart = parts.getOrNull(i) ?: 0
			val otherPart = other.parts.getOrNull(i) ?: 0
			val comparison = thisPart.compareTo(otherPart)
			if (comparison != 0) return comparison
		}
		return 0
	}

	override fun toString(): String = name
}