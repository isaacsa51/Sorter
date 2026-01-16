package com.serranoie.app.media.sorter.domain.repository

import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result
import java.time.LocalDate

interface MediaRepository {

    suspend fun fetchMediaFiles(): Result<List<MediaFile>>

    suspend fun getMediaByFolder(): Result<Map<String, List<MediaFile>>>

    suspend fun getMediaGroupedByDate(): Result<Map<LocalDate, List<MediaFile>>>

    fun clearCache()
}
