package com.serranoie.app.media.sorter.presentation.ui.theme.components

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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis

/**
 * Static media content displayed in the card with shared element transition support.
 * Uses sharedBoundsRevealWithShapeMorph for smooth animated transitions.
 * 
 * @param uri The URI of the media to display
 * @param fileName The name of the file (for content description)
 * @param mediaType The type of media ("image" or "video")
 * @param sharedTransitionScope The scope managing shared transitions
 * @param animatedVisibilityScope The scope for animated visibility
 * @param isVisible Whether this view is currently visible (controls transition)
 * @param modifier Additional modifiers
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
            val imageRequest = remember(uri, mediaType) {
                ImageRequest.Builder(context)
                    .data(uri)
                    .crossfade(300)
                    .apply {
                        if (mediaType == "video") {
                            decoderFactory { result, options, _ ->
                                VideoFrameDecoder(result.source, options)
                            }
                            videoFrameMillis(1000)
                        }
                    }
                    .build()
            }

            val sharedElementKey = ImageElementKey(uri)
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = fileName,
                modifier = modifier
                    .then(
                        if (isVisible) {
                            Modifier.sharedBoundsRevealWithShapeMorph(
                                sharedContentState = rememberSharedContentState(key = sharedElementKey),
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                restingShapeCornerRadius = 24.dp, // Card corner radius
                                targetShapeCornerRadius = 0.dp,   // Zoom overlay (square)
                                renderInOverlayDuringTransition = true,
                                keepChildrenSizePlacement = true
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
