package com.serranoie.app.media.sorter.domain.repository

import android.net.Uri
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result
import java.time.LocalDate

interface MediaRepository {

    suspend fun fetchMediaFiles(): Result<List<MediaFile>>

    suspend fun getMediaByFolder(): Result<Map<String, List<MediaFile>>>

    suspend fun getMediaGroupedByDate(): Result<Map<LocalDate, List<MediaFile>>>
    
    suspend fun deleteMedia(uri: Uri): Result<Boolean>
    
    suspend fun deleteMultipleMedia(uris: List<Uri>): Result<Int>

    fun clearCache()
}
