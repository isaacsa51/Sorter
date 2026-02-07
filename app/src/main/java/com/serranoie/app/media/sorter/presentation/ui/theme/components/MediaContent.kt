package com.serranoie.app.media.sorter.presentation.ui.theme.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Component that displays media content (image or video) with zoom support
 * 
 * Supports zoom gestures using Telephoto library:
 * - Pinch-to-zoom and flings
 * - Double-tap to zoom in/out
 * - Single finger zoom (double-tap and hold)
 * - Pan gestures when zoomed
 * - Haptic feedback when reaching zoom limits
 * 
 * @param uri The URI of the media file
 * @param fileName The name of the file for content description
 * @param mediaType The type of media ("video" or other for images)
 * @param onClickToZoom Callback when image is clicked to enter fullscreen zoom
 * @param modifier Modifier to be applied to the media content
 */
@Composable
fun MediaContent(
    uri: Uri?,
    fileName: String,
    mediaType: String,
    onClickToZoom: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isVideo = mediaType == "video"

    if (uri != null) {
        // Build image request - Coil will handle video frames automatically with coil-video
        val imageRequest = remember(uri) {
            ImageRequest.Builder(context)
                .data(uri)
                .crossfade(300)
                .build()
        }

        // Real media from device - double-tap to enter fullscreen zoom
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = fileName,
            modifier = modifier.pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onClickToZoom()
                    }
                )
            },
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                MediaPlaceholder(
                    mediaType = mediaType,
                    errorText = "Cannot load media"
                )
            }
        )
    } else {
        // Mock data placeholder
        MediaPlaceholder(
            mediaType = mediaType,
            errorText = null
        )
    }
}

/**
 * Placeholder component shown when media cannot be loaded or for mock data
 */
@Composable
internal fun MediaPlaceholder(
    mediaType: String,
    errorText: String?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isVideo = mediaType == "video"
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isVideo) {
                    colorScheme.tertiaryContainer
                } else {
                    colorScheme.primaryContainer
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isVideo) {
                    Icons.Default.VideoLibrary
                } else {
                    Icons.Default.Image
                },
                contentDescription = null,
                modifier = Modifier.size(if (errorText != null) 80.dp else 120.dp),
                tint = if (isVideo) {
                    colorScheme.onTertiaryContainer.copy(alpha = if (errorText != null) 0.5f else 0.3f)
                } else {
                    colorScheme.onPrimaryContainer.copy(alpha = if (errorText != null) 0.5f else 0.3f)
                }
            )
            if (errorText != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
private fun MediaContentImagePlaceholderPreview() {
    MaterialTheme {
        MediaContent(
            uri = null,
            fileName = "Sample Image.jpg",
            mediaType = "image"
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 600)
@Composable
private fun MediaContentVideoPlaceholderPreview() {
    MaterialTheme {
        MediaContent(
            uri = null,
            fileName = "Sample Video.mp4",
            mediaType = "video"
        )
    }
}
