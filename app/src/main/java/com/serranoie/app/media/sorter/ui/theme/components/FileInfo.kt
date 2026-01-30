package com.serranoie.app.media.sorter.ui.theme.components

/**
 * Data class representing file information to be displayed
 */
data class FileInfo(
    val fileName: String,
    val fileInfo: String,
    val fileSize: String,
    val dateCreated: String,
    val modified: String,
    val dimensions: String,
    val lastAccessed: String,
    val path: String
)
