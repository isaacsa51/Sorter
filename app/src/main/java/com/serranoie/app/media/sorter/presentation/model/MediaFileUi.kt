package com.serranoie.app.media.sorter.presentation.model

import android.net.Uri

data class MediaFileUi(
    val id: String,
    val fileName: String,
    val fileInfo: String,
    val mediaType: String,
    val date: String,
    val fileSize: String,
    val dimensions: String,
    val dateCreated: String,
    val lastAccessed: String,
    val modified: String,
    val path: String,
    val uri: Uri? = null
)
