package com.serranoie.app.media.sorter.update.service

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
	@SerializedName("tag_name") val tagName: String,
	@SerializedName("name") val name: String?,
	@SerializedName("html_url") val htmlUrl: String,
	@SerializedName("body") val body: String?,
	@SerializedName("prerelease") val prerelease: Boolean,
	@SerializedName("assets") val assets: List<GitHubAsset>
)

data class GitHubAsset(
	@SerializedName("name") val name: String,
	@SerializedName("browser_download_url") val browserDownloadUrl: String
)