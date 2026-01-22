package com.serranoie.app.media.sorter.domain

import android.net.Uri
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import javax.inject.Inject

class DeleteMediaUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Boolean> {
        return repository.deleteMedia(uri)
    }
    
    suspend fun deleteMultiple(uris: List<Uri>): Result<Int> {
        return repository.deleteMultipleMedia(uris)
    }
}
