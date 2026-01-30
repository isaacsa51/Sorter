package com.serranoie.app.media.sorter.ui.theme.components

import android.net.Uri

/**
 * Shared element key for identifying media images across different composables.
 * Uses URI to uniquely identify each media file.
 */
data class ImageElementKey(val uri: Uri?)
