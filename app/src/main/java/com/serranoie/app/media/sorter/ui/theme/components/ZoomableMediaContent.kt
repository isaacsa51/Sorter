package com.serranoie.app.media.sorter.ui.theme.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Static media content (non-zoomable) - displays image in card
 * No gesture detection here - all handled by parent
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomableMediaContent(
    uri: Uri?,
    fileName: String,
    mediaType: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    with(sharedTransitionScope) {
        if (uri != null) {
            val imageRequest = remember(uri) {
                ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(300)
                    .build()
            }

            // Static image - no gesture detection (handled by parent Box)
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = fileName,
                modifier = modifier
                    .then(
                        if (isVisible) {
                            Modifier.sharedElement(
                                rememberSharedContentState(key = "media-${uri}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        } else Modifier
                    ),
                contentScale = ContentScale.Crop,
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
            MediaPlaceholder(
                mediaType = mediaType,
                errorText = null
            )
        }
    }
}
