package com.serranoie.app.media.sorter.data

import android.net.Uri
import android.util.Log
import com.serranoie.app.media.sorter.data.datasource.MediaDataSource
import com.serranoie.app.media.sorter.domain.AppError
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.asError
import com.serranoie.app.media.sorter.domain.asSuccess
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SorterMediaRepositoryImpl @Inject constructor(
	private val mediaDataSource: MediaDataSource
) : MediaRepository {

	companion object {
		private const val TAG = "SorterMediaRepository"
	}

	private var cachedMediaFiles: List<MediaFile>? = null
	private val cacheMutex = Mutex()

	override suspend fun fetchMediaFiles(): Result<List<MediaFile>> {
		return cacheMutex.withLock {
			cachedMediaFiles?.let {
				Log.d(TAG, "Returning ${it.size} cached media files")
				return@withLock it.asSuccess()
			}

			Log.d(TAG, "Fetching fresh media files")
			val result = fetchAndCombineMedia()

			if (result is Result.Success) {
				cachedMediaFiles = result.data
				Log.d(TAG, "Cached ${result.data.size} media files")
			}

			result
		}
	}

	override suspend fun getMediaByFolder(): Result<Map<String, List<MediaFile>>> {
		return when (val result = fetchMediaFiles()) {
			is Result.Success -> {
				val groupedByFolder = result.data.groupBy { it.folderName }
				Log.d(TAG, "Grouped media into ${groupedByFolder.size} folders")
				groupedByFolder.asSuccess()
			}

			is Result.Error -> result
			is Result.Loading -> Result.Loading
		}
	}

	override suspend fun getMediaGroupedByDate(): Result<Map<LocalDate, List<MediaFile>>> {
		return when (val result = fetchMediaFiles()) {
			is Result.Success -> {
				val groupedByDate = result.data.groupBy { it.fileDate }
				Log.d(TAG, "Grouped media into ${groupedByDate.size} dates")
				groupedByDate.asSuccess()
			}

			is Result.Error -> result
			is Result.Loading -> Result.Loading
		}
	}

	override suspend fun deleteMedia(uri: Uri): Result<Boolean> {
		return mediaDataSource.deleteMedia(uri)
	}

	override suspend fun deleteMultipleMedia(uris: List<Uri>): Result<Int> {
		return mediaDataSource.deleteMultipleMedia(uris)
	}

	override fun createDeletionRequest(
		uris: List<Uri>,
		useTrash: Boolean
	): android.app.PendingIntent? {
		return mediaDataSource.createDeletionRequest(uris, useTrash)
	}

	override fun clearCache() {
		cachedMediaFiles = null
		Log.d(TAG, "Cache cleared")
	}

	private suspend fun fetchAndCombineMedia(): Result<List<MediaFile>> {
		val imagesResult = mediaDataSource.fetchImages()
		val videosResult = mediaDataSource.fetchVideos()

		return when {
			imagesResult is Result.Success && videosResult is Result.Success -> {
				val combined = (imagesResult.data + videosResult.data)
					.sortedByDescending { it.dateTaken }

				if (combined.isEmpty()) {
					AppError.NoMediaFoundError().asError()
				} else {
					Log.d(
						TAG, "Successfully fetched ${combined.size} media files " +
								"(${imagesResult.data.size} images, ${videosResult.data.size} videos)"
					)
					combined.asSuccess()
				}
			}

			imagesResult is Result.Success -> {
				if (imagesResult.data.isEmpty()) {
					AppError.NoMediaFoundError().asError()
				} else {
					Log.d(TAG, "Fetched ${imagesResult.data.size} images (videos failed)")
					imagesResult.data.sortedByDescending { it.dateTaken }.asSuccess()
				}
			}

			videosResult is Result.Success -> {
				if (videosResult.data.isEmpty()) {
					AppError.NoMediaFoundError().asError()
				} else {
					Log.d(TAG, "Fetched ${videosResult.data.size} videos (images failed)")
					videosResult.data.sortedByDescending { it.dateTaken }.asSuccess()
				}
			}

			imagesResult is Result.Error -> {
				Log.e(TAG, "Failed to fetch media: ${imagesResult.error.message}")
				imagesResult
			}

			videosResult is Result.Error -> {
				Log.e(TAG, "Failed to fetch media: ${videosResult.error.message}")
				videosResult
			}

			else -> {
				AppError.UnknownError(message = "Unexpected state in fetchAndCombineMedia")
					.asError()
			}
		}
	}
}
