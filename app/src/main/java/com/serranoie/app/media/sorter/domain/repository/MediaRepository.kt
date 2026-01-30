package com.serranoie.app.media.sorter.domain.repository

import android.app.PendingIntent
import android.net.Uri
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result
import java.time.LocalDate

interface MediaRepository {

    suspend fun fetchMediaFiles(): Result<List<MediaFile>>

    suspend fun getMediaByFolder(): Result<Map<String, List<MediaFile>>>

    suspend fun getMediaGroupedByDate(): Result<Map<LocalDate, List<MediaFile>>>

    suspend fun getMediaGroupedByDateFiltered(): Result<Map<LocalDate, List<MediaFile>>>
    
    suspend fun deleteMedia(uri: Uri): Result<Boolean>
    
    suspend fun deleteMultipleMedia(uris: List<Uri>): Result<Int>
    
    /**
     * Creates a deletion request based on user preference
     * @param uris List of URIs to delete/trash
     * @param useTrash If true, moves to trash bin; if false, permanently deletes
     * Returns a PendingIntent that should be launched by the Activity
     */
    fun createDeletionRequest(uris: List<Uri>, useTrash: Boolean): PendingIntent?

    fun clearCache()

    suspend fun markAsViewed(mediaId: Long)

    suspend fun isViewed(mediaId: Long): Boolean

    suspend fun clearViewedHistory()

    suspend fun getViewedCount(): Int
}
