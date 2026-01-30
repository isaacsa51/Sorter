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

    /**
     * Fetches media files in random date batches, excluding already-viewed content.
     * Implements recursive logic: if a date has no unviewed content, it tries the next random date
     * until content is found or all dates are exhausted.
     */
    suspend operator fun invoke(): Result<List<Pair<LocalDate, List<MediaFile>>>> {
        Log.d(TAG, "Fetching unviewed media files in random batches")
        
        return when (val result = repository.getMediaGroupedByDateFiltered()) {
            is Result.Success -> {
                val grouped = result.data
                
                if (grouped.isEmpty()) {
                    Log.d(TAG, "No unviewed media found - all content has been processed")
                    AppError.NoMediaFoundError(
                        message = "All media has been viewed",
                        details = "No new content to show. You can reset viewed history in settings."
                    ).asError()
                } else {
                    val shuffledKeys = grouped.keys.shuffled()

                    val randomBatches = shuffledKeys.mapNotNull { date ->
                        grouped[date]?.let { files ->
                            val validFiles = files.filter { file ->
                                file.fileSize > 0 && file.fileName.isNotBlank()
                            }
                            
                            if (validFiles.isNotEmpty()) {
                                date to validFiles
                            } else {
                                Log.d(TAG, "Skipping date $date - no valid files")
                                null
                            }
                        }
                    }
                    
                    if (randomBatches.isEmpty()) {
                        Log.d(TAG, "No valid unviewed media found after filtering")
                        AppError.NoMediaFoundError(
                            message = "No valid unviewed media",
                            details = "All unviewed files appear to be invalid or corrupted."
                        ).asError()
                    } else {
                        Log.d(TAG, "Created ${randomBatches.size} random date batches " +
                                "with unviewed content (from ${grouped.size} dates with unviewed media)")
                        randomBatches.asSuccess()
                    }
                }
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }
}
