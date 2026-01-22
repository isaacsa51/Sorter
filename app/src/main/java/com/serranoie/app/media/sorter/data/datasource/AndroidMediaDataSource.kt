package com.serranoie.app.media.sorter.data.datasource

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.AppError
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.asError
import com.serranoie.app.media.sorter.domain.asSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class AndroidMediaDataSource @Inject constructor(
    private val context: Context
) : MediaDataSource {
    
    companion object {
        private const val TAG = "AndroidMediaDataSource"
    }
    
    override suspend fun fetchImages(): Result<List<MediaFile>> = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
            )
            
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            
            fetchMediaGeneric(
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection = projection,
                sortOrder = sortOrder,
                mediaType = "image"
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for images", e)
            AppError.PermissionError().asError()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching images", e)
            AppError.MediaLoadError(
                details = "Failed to load images: ${e.message}",
                cause = e
            ).asError()
        }
    }
    
    override suspend fun fetchVideos(): Result<List<MediaFile>> = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_TAKEN,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT
            )
            
            val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"
            
            fetchMediaGeneric(
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection = projection,
                sortOrder = sortOrder,
                mediaType = "video"
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for videos", e)
            AppError.PermissionError().asError()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos", e)
            AppError.MediaLoadError(
                details = "Failed to load videos: ${e.message}",
                cause = e
            ).asError()
        }
    }
    
    override suspend fun fetchMediaByUri(uri: Uri): Result<MediaFile> = withContext(Dispatchers.IO) {
        // TODO: Implement if needed for future features
        AppError.UnknownError(message = "Not implemented").asError()
    }
    
    override suspend fun deleteMedia(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val deletedRows = context.contentResolver.delete(uri, null, null)
            if (deletedRows > 0) {
                Log.d(TAG, "Successfully deleted media: $uri")
                true.asSuccess()
            } else {
                Log.w(TAG, "Failed to delete media: $uri")
                AppError.UnknownError(message = "Failed to delete file").asError()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to delete media", e)
            AppError.PermissionError().asError()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting media", e)
            AppError.UnknownError(message = "Error deleting file: ${e.message}", cause = e).asError()
        }
    }
    
    override suspend fun deleteMultipleMedia(uris: List<Uri>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var successCount = 0
            var failureCount = 0
            val pendingUris = mutableListOf<Uri>()
            
            uris.forEach { uri ->
                try {
                    val deletedRows = context.contentResolver.delete(uri, null, null)
                    if (deletedRows > 0) {
                        successCount++
                    } else {
                        failureCount++
                    }
                } catch (e: RecoverableSecurityException) {
                    // On Android 10+, we need user permission for files we didn't create
                    Log.w(TAG, "RecoverableSecurityException for $uri, needs user permission")
                    pendingUris.add(uri)
                    failureCount++
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException for $uri", e)
                    failureCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete $uri", e)
                    failureCount++
                }
            }
            
            Log.d(TAG, "Deleted $successCount of ${uris.size} media files ($failureCount failed, ${pendingUris.size} need permission)")
            
            // If all failures are RecoverableSecurityException, we can request permission
            if (successCount == 0 && pendingUris.size == uris.size) {
                Log.d(TAG, "All files need user permission, returning error to trigger permission request")
                AppError.PermissionError().asError()
            } else {
                successCount.asSuccess()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied to delete media", e)
            AppError.PermissionError().asError()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting multiple media", e)
            AppError.UnknownError(message = "Error deleting files: ${e.message}", cause = e).asError()
        }
    }
    
    override fun createDeleteRequest(uris: List<Uri>): android.app.PendingIntent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30)
            Log.d(TAG, "Creating delete request for ${uris.size} files (Android 11+)")
            MediaStore.createDeleteRequest(context.contentResolver, uris)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29)
            Log.d(TAG, "Creating delete request for ${uris.size} files (Android 10)")
            MediaStore.createDeleteRequest(context.contentResolver, uris)
        } else {
            // Android 9 and below - no special permission needed
            Log.d(TAG, "No delete request needed for Android < 10")
            null
        }
    }

    private fun fetchMediaGeneric(
        contentUri: Uri,
        projection: Array<String>,
        sortOrder: String,
        mediaType: String
    ): Result<List<MediaFile>> {
        val mediaFiles = mutableListOf<MediaFile>()
        
        try {
            val cursor: Cursor? = context.contentResolver.query(
                contentUri,
                projection,
                null,
                null,
                sortOrder
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(projection[0])
                val nameColumn = it.getColumnIndexOrThrow(projection[1])
                val sizeColumn = it.getColumnIndexOrThrow(projection[2])
                val dateTakenColumn = it.getColumnIndexOrThrow(projection[3])
                val dateAddedColumn = it.getColumnIndexOrThrow(projection[4])
                val bucketColumn = it.getColumnIndexOrThrow(projection[6])
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: continue
                    val size = it.getLong(sizeColumn)
                    val dateTaken = it.getLong(dateTakenColumn)
                    val dateAdded = it.getLong(dateAddedColumn)
                    val bucket = it.getString(bucketColumn) ?: "Unknown"
                    
                    val contentUriWithId = ContentUris.withAppendedId(contentUri, id)
                    val extension = name.substringAfterLast(".", if (mediaType == "image") "jpg" else "mp4")
                    
                    // Use dateTaken if available, otherwise use dateAdded
                    val timestamp = if (dateTaken > 0) dateTaken else dateAdded * 1000
                    val fileDate = Instant.ofEpochMilli(timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    
                    mediaFiles.add(
                        MediaFile(
                            uri = contentUriWithId,
                            mediaType = mediaType,
                            extension = extension,
                            fileName = name,
                            folderName = bucket,
                            fileSize = size,
                            fileDate = fileDate,
                            previewUri = contentUriWithId,
                            dateTaken = timestamp
                        )
                    )
                }
            }
            
            return if (mediaFiles.isEmpty()) {
                AppError.NoMediaFoundError().asError()
            } else {
                mediaFiles.asSuccess()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchMediaGeneric", e)
            return AppError.fromThrowable(e).asError()
        }
    }
}
