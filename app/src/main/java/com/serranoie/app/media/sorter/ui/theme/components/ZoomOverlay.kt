package com.serranoie.app.media.sorter.ui.theme.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import kotlin.math.roundToInt

/**
 * Full-screen zoom overlay with shared element transition support.
 * 
 * Features:
 * - Shared element transition with shape morphing (rounded card → square overlay)
 * - Black semi-transparent background
 * - Fully zoomable image using Telephoto library
 * - Double-tap when zoomed out to dismiss
 * - Swipe down to dismiss (when not zoomed in)
 * - Pinch gestures handled by parent
 * 
 * @param uri The URI of the media to display
 * @param fileName The name of the file (for content description)
 * @param mediaType The type of media ("image" or "video")
 * @param sharedTransitionScope The scope managing shared transitions
 * @param animatedVisibilityScope The scope for animated visibility
 * @param isVisible Whether the overlay is visible
 * @param onDismiss Callback when user wants to close the overlay
 * @param modifier Additional modifiers
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomOverlay(
    uri: Uri?,
    fileName: String,
    mediaType: String = "image",
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    with(sharedTransitionScope) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = modifier
        ) {
            var offsetY by remember { mutableFloatStateOf(0f) }
            val dismissThreshold = 200f // Pixels to swipe down before dismissing
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { onDismiss() }
            ) {
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
                    val zoomableState = rememberZoomableState()
                    
                    val currentScale = zoomableState.contentTransformation.scale.scaleX
                    val isZoomedOut = currentScale <= 1.01f // At or near default scale
                    
                    var lastTapTime by remember { mutableStateOf(0L) }
                    val doubleTapThreshold = 300L // ms
                    SubcomposeAsyncImage(
                        model = imageRequest,
                        contentDescription = fileName,
                        modifier = Modifier
                            .fillMaxSize()
                            .sharedBoundsRevealWithShapeMorph(
                                sharedContentState = rememberSharedContentState(key = sharedElementKey),
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = this@AnimatedVisibility,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                                restingShapeCornerRadius = 0.dp,
                                targetShapeCornerRadius = 24.dp,
                                renderInOverlayDuringTransition = true,
                                keepChildrenSizePlacement = true
                            )
                            .offset { IntOffset(0, offsetY.roundToInt()) }
                            .graphicsLayer {
                                alpha = (1f - (offsetY / dismissThreshold).coerceIn(0f, 1f))
                            }
                            .pointerInput(isZoomedOut) {
                                if (isZoomedOut) {
                                    detectDragGestures(
                                        onDrag = { _, dragAmount ->
                                            if (dragAmount.y > 0) {
                                                offsetY = (offsetY + dragAmount.y).coerceAtLeast(0f)
                                            }
                                        },
                                        onDragEnd = {
                                            if (offsetY >= dismissThreshold) {
                                                onDismiss()
                                            }
                                            offsetY = 0f
                                        },
                                        onDragCancel = {
                                            offsetY = 0f
                                        }
                                    )
                                }
                            }
                            .zoomable(
                                state = zoomableState,
                                onClick = { 
                                    val currentTime = System.currentTimeMillis()
                                    
                                    if (isZoomedOut && (currentTime - lastTapTime) < doubleTapThreshold) {
                                        onDismiss()
                                        lastTapTime = 0L
                                    } else {
                                        lastTapTime = currentTime
                                    }
                                }
                            ),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    )
                }
            }
        }
    }
}
