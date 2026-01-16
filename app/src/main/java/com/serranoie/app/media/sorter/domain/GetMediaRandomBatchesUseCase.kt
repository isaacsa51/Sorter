package com.serranoie.app.media.sorter.domain

import android.util.Log
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import java.time.LocalDate
import javax.inject.Inject

class GetMediaRandomBatchesUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    companion object {
        private const val TAG = "GetMediaRandomBatchesUseCase"
    }

    suspend operator fun invoke(): Result<List<Pair<LocalDate, List<MediaFile>>>> {
        Log.d(TAG, "Fetching media files in random batches")
        
        return when (val result = repository.getMediaGroupedByDate()) {
            is Result.Success -> {
                val grouped = result.data
                
                // Shuffle dates for randomization
                val shuffledKeys = grouped.keys.shuffled()
                
                val randomBatches = shuffledKeys.mapNotNull { date ->
                    grouped[date]?.let { files ->
                        // Filter out invalid files
                        val validFiles = files.filter { file ->
                            file.fileSize > 0 && file.fileName.isNotBlank()
                        }
                        
                        if (validFiles.isNotEmpty()) {
                            date to validFiles
                        } else {
                            null
                        }
                    }
                }
                
                Log.d(TAG, "Created ${randomBatches.size} random date batches " +
                        "from ${grouped.size} total dates")
                
                randomBatches.asSuccess()
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }
}
