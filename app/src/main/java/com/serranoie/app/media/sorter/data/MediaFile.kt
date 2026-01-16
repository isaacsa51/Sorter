package com.serranoie.app.media.sorter.data

import android.net.Uri
import java.time.LocalDate

data class MediaFile(
	val uri: Uri,
	val mediaType: String, // image, video
	val extension: String,
	val fileName: String,
	val folderName: String,
	val fileSize: Long,
	val fileDate: LocalDate,
	val previewUri: Uri,
	val dateTaken: Long // timestamp (ms)
)
