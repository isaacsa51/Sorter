package com.serranoie.app.media.sorter.data

import android.content.ContentUris
import android.net.Uri
import android.util.Log
import com.serranoie.app.media.sorter.data.database.ViewedMedia
import com.serranoie.app.media.sorter.data.database.ViewedMediaDao
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
	private val mediaDataSource: MediaDataSource,
	private val viewedMediaDao: ViewedMediaDao
) : MediaRepository {

	companion object {
		private const val TAG = "SorterMediaRepository"
	}

	private var cachedMediaFiles: List<MediaFile>? = null
	private val cacheMutex = Mutex()
	
	// In-memory Set for high-performance filtering
	private val viewedMediaIds = mutableSetOf<Long>()
	private val viewedIdsMutex = Mutex()
	private var isViewedIdsInitialized = false

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
	
	override suspend fun getMediaGroupedByDateFiltered(): Result<Map<LocalDate, List<MediaFile>>> {
		ensureViewedIdsLoaded()
		
		return when (val result = fetchMediaFiles()) {
			is Result.Success -> {
				viewedIdsMutex.withLock {
					val filteredMedia = result.data.filter { mediaFile ->
						val mediaId = extractMediaId(mediaFile.uri)
						mediaId != null && !viewedMediaIds.contains(mediaId)
					}
					
					val groupedByDate = filteredMedia.groupBy { it.fileDate }
					Log.d(TAG, "Grouped ${filteredMedia.size} unviewed media into ${groupedByDate.size} dates " +
							"(filtered out ${result.data.size - filteredMedia.size} viewed items)")
					groupedByDate.asSuccess()
				}
			}
			is Result.Error -> result
			is Result.Loading -> Result.Loading
		}
	}
	
	override suspend fun markAsViewed(mediaId: Long) {
		viewedIdsMutex.withLock {
			viewedMediaIds.add(mediaId)
			
			viewedMediaDao.insertViewed(ViewedMedia(mediaId = mediaId))
			
			Log.d(TAG, "Marked media ID $mediaId as viewed (total viewed: ${viewedMediaIds.size})")
		}
	}
	
	override suspend fun isViewed(mediaId: Long): Boolean {
		ensureViewedIdsLoaded()
		return viewedIdsMutex.withLock {
			viewedMediaIds.contains(mediaId)
		}
	}
	
	override suspend fun clearViewedHistory() {
		viewedIdsMutex.withLock {
			viewedMediaIds.clear()
			viewedMediaDao.clearAllViewed()
			Log.d(TAG, "Cleared all viewed media history")
		}
	}
	
	override suspend fun getViewedCount(): Int {
		ensureViewedIdsLoaded()
		return viewedIdsMutex.withLock {
			viewedMediaIds.size
		}
	}
	
	/**
	 * Ensures the in-memory set of viewed IDs is synchronized with the database.
	 * Called once on first access.
	 */
	private suspend fun ensureViewedIdsLoaded() {
		if (!isViewedIdsInitialized) {
			viewedIdsMutex.withLock {
				if (!isViewedIdsInitialized) {
					val viewedIds = viewedMediaDao.getAllViewedIds()
					viewedMediaIds.clear()
					viewedMediaIds.addAll(viewedIds)
					isViewedIdsInitialized = true
					Log.d(TAG, "Loaded ${viewedIds.size} viewed media IDs from database into memory")
				}
			}
		}
	}
	
	/**
	 * Extracts the media ID from a content URI.
	 * Content URIs typically have the format: content://media/external/images/media/{id}
	 */
	private fun extractMediaId(uri: Uri): Long? {
		return try {
			ContentUris.parseId(uri)
		} catch (e: Exception) {
			Log.w(TAG, "Failed to extract media ID from URI: $uri", e)
			null
		}
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
