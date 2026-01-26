package com.serranoie.app.media.sorter.data.datasource

import android.app.PendingIntent
import android.net.Uri
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result

interface MediaDataSource {

    suspend fun fetchImages(): Result<List<MediaFile>>

    suspend fun fetchVideos(): Result<List<MediaFile>>

    suspend fun fetchMediaByUri(uri: Uri): Result<MediaFile>
    
    suspend fun deleteMedia(uri: Uri): Result<Boolean>
    
    suspend fun deleteMultipleMedia(uris: List<Uri>): Result<Int>
    
    /**
     * Creates a delete request that requires user permission (Android 10+)
     * Returns a PendingIntent that should be launched by the Activity
     */
    fun createDeleteRequest(uris: List<Uri>): PendingIntent?
    
    /**
     * Creates a trash request that moves files to trash bin (Android 11+)
     * Returns a PendingIntent that should be launched by the Activity
     */
    fun createTrashRequest(uris: List<Uri>): PendingIntent?
    
    /**
     * Creates a deletion request based on user preference
     * @param uris List of URIs to delete/trash
     * @param useTrash If true, moves to trash bin; if false, permanently deletes
     * Returns a PendingIntent that should be launched by the Activity
     */
    fun createDeletionRequest(uris: List<Uri>, useTrash: Boolean): PendingIntent?
}
