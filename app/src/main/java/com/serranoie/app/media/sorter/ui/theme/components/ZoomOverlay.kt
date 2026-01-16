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
import androidx.compose.runtime.LaunchedEffect
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
import kotlin.math.roundToInt
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable

/**
 * Zoom overlay that appears on top of the card when user zooms
 * - Black semi-transparent background
 * - Fully zoomable image
 * - Single tap to dismiss
 * - Double-tap when zoomed out to dismiss
 * - Swipe down to dismiss (when not zoomed in)
 * - Pinch-to-close handled by parent
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ZoomOverlay(
    uri: Uri?,
    fileName: String,
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
            // Track vertical drag offset for swipe-to-dismiss
            var offsetY by remember { mutableFloatStateOf(0f) }
            val dismissThreshold = 200f // Pixels to swipe down before dismissing
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { onDismiss() }
            ) {
                if (uri != null) {
                    val imageRequest = remember(uri) {
                        ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(300)
                            .build()
                    }

                    val zoomableState = rememberZoomableState()
                    
                    // Track zoom level
                    val currentScale = zoomableState.contentTransformation.scale.scaleX
                    val isZoomedOut = currentScale <= 1.01f // At or near default scale
                    
                    // Track double-tap when zoomed out
                    var lastTapTime by remember { mutableStateOf(0L) }
                    val doubleTapThreshold = 300L // ms

                    // Fully zoomable image on top
                    SubcomposeAsyncImage(
                        model = imageRequest,
                        contentDescription = fileName,
                        modifier = Modifier
                            .fillMaxSize()
                            .sharedElement(
                                rememberSharedContentState(key = "media-${uri}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .offset { IntOffset(0, offsetY.roundToInt()) }
                            .graphicsLayer {
                                // Fade out as user swipes down
                                alpha = (1f - (offsetY / dismissThreshold).coerceIn(0f, 1f))
                            }
                            .pointerInput(isZoomedOut) {
                                // Only allow swipe-to-dismiss when not zoomed in
                                if (isZoomedOut) {
                                    detectDragGestures(
                                        onDrag = { _, dragAmount ->
                                            // Only allow downward swipes
                                            if (dragAmount.y > 0) {
                                                offsetY = (offsetY + dragAmount.y).coerceAtLeast(0f)
                                            }
                                        },
                                        onDragEnd = {
                                            if (offsetY >= dismissThreshold) {
                                                // Dismiss if swiped far enough
                                                onDismiss()
                                            }
                                            // Reset offset
                                            offsetY = 0f
                                        },
                                        onDragCancel = {
                                            // Reset offset if drag is cancelled
                                            offsetY = 0f
                                        }
                                    )
                                }
                            }
                            .zoomable(
                                state = zoomableState,
                                onClick = { 
                                    val currentTime = System.currentTimeMillis()
                                    
                                    // Check for double-tap when zoomed out
                                    if (isZoomedOut && (currentTime - lastTapTime) < doubleTapThreshold) {
                                        // Double-tap detected while zoomed out → Close
                                        onDismiss()
                                        lastTapTime = 0L
                                    } else {
                                        // Single tap → Update timestamp
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
