package com.serranoie.app.media.sorter.domain.media

import android.util.Log
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.asSuccess
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import javax.inject.Inject

class SorterMediaUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    companion object {
        private const val TAG = "SorterMediaUseCase"
    }

    suspend operator fun invoke(): Result<List<MediaFile>> {
        Log.d(TAG, "Fetching media files in chronological order")

        return when (val result = repository.fetchMediaFiles()) {
            is Result.Success -> {
                val validFiles = result.data.filter { file ->
                    file.fileSize > 0 && file.fileName.isNotBlank()
                }

                Log.d(TAG, "Filtered ${result.data.size} files to ${validFiles.size} valid files")

                val sorted = validFiles.sortedByDescending { it.dateTaken }
                sorted.asSuccess()
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    suspend fun getMediaByFolder(): Result<Map<String, List<MediaFile>>> {
        return repository.getMediaByFolder()
    }
}