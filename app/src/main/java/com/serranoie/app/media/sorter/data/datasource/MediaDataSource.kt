package com.serranoie.app.media.sorter.data.datasource

import android.net.Uri
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result

interface MediaDataSource {

    suspend fun fetchImages(): Result<List<MediaFile>>

    suspend fun fetchVideos(): Result<List<MediaFile>>

    suspend fun fetchMediaByUri(uri: Uri): Result<MediaFile>
}
