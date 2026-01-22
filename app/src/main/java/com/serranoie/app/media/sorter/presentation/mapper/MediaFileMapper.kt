package com.serranoie.app.media.sorter.presentation.mapper

import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import com.serranoie.app.media.sorter.util.MediaFileFormatter
import javax.inject.Inject

class MediaFileMapper @Inject constructor(
    private val formatter: MediaFileFormatter
) {
    
    fun toUiModel(mediaFile: MediaFile, id: String): MediaFileUi {
        val fileSizeStr = formatter.formatFileSize(mediaFile.fileSize)
        val dateStr = formatter.formatRelativeDate(mediaFile.fileDate)
        val dateCreatedStr = formatter.formatDateTime(mediaFile.dateTaken)
        val lastAccessedStr = formatter.formatDateTime(System.currentTimeMillis())
        val fileInfoStr = formatter.formatFileInfoWithPath(mediaFile.fileSize, mediaFile.folderName)
        
        return MediaFileUi(
            id = id,
            fileName = mediaFile.fileName,
            fileInfo = fileInfoStr,
            mediaType = mediaFile.mediaType,
            date = dateStr,
            fileSize = fileSizeStr,
            dimensions = "Unknown", // TODO: Extract dimensions from media metadata
            dateCreated = dateCreatedStr,
            lastAccessed = lastAccessedStr,
            modified = dateStr,
            path = mediaFile.folderName,
            uri = mediaFile.uri
        )
    }

    fun toUiModelList(mediaFiles: List<MediaFile>): List<MediaFileUi> {
        return mediaFiles.mapIndexed { index, mediaFile ->
            toUiModel(mediaFile, index.toString())
        }
    }
}
