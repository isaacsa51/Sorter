package com.serranoie.app.media.sorter.presentation.ui.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs

/**
 * Custom pinch gesture detector
 * Provides smooth zoom detection with configurable thresholds
 */
suspend fun PointerInputScope.detectPinchGestures(
    pass: PointerEventPass = PointerEventPass.Main,
    onGestureStart: (PointerInputChange) -> Unit = {},
    onGesture: (centroid: Offset, zoom: Float) -> Unit,
    onGestureEnd: (PointerInputChange) -> Unit = {}
) {
    awaitEachGesture {
        var zoom = 1f
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop

        val down: PointerInputChange = awaitFirstDown(requireUnconsumed = false, pass = pass)
        onGestureStart(down)

        var pointer = down
        var pointerId = down.id

        do {
            val event = awaitPointerEvent(pass = pass)
            val canceled = event.changes.any { it.isConsumed }

            if (!canceled) {
                val pointerInputChange =
                    event.changes.firstOrNull { it.id == pointerId } ?: event.changes.first()
                pointerId = pointerInputChange.id
                pointer = pointerInputChange

                val zoomChange = event.calculateZoom()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize

                    if (zoomMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f) {
                        onGesture(centroid, zoomChange)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })

        onGestureEnd(pointer)
    }
}

data class GridZoomLevel(
    val level: Int, val columns: Int, val nextLevel: Int, val previousLevel: Int
)

/**
 * Wrapper composable that manages multiple zoom levels with smooth transitions
 * Based on the iOS Photos app behavior described in the article
 * Uses Material 3 MotionScheme with emphasized easing for full-screen transitions
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PinchToZoomGridContainer(
    modifier: Modifier = Modifier, initialLevel: Int = 1, zoomLevels: List<GridZoomLevel> = listOf(
        GridZoomLevel(level = 0, columns = 1, nextLevel = 1, previousLevel = 0),
        GridZoomLevel(level = 1, columns = 2, nextLevel = 2, previousLevel = 0),
        GridZoomLevel(level = 2, columns = 3, nextLevel = 3, previousLevel = 1),
        GridZoomLevel(level = 3, columns = 4, nextLevel = 3, previousLevel = 2)
    ), content: @Composable (zoomLevel: GridZoomLevel, onZoomLevelChange: (Int) -> Unit) -> Unit
) {
    var currentLevel by remember { mutableStateOf(initialLevel) }

    val motionScheme = MotionScheme.expressive()

    zoomLevels.forEach { zoomLevel ->
        AnimatedVisibility(
            visible = currentLevel == zoomLevel.level, enter = scaleIn(
                animationSpec = motionScheme.slowSpatialSpec(), initialScale = 0.92f
            ) + fadeIn(
                animationSpec = motionScheme.slowEffectsSpec()
            ), exit = scaleOut(
                animationSpec = motionScheme.slowSpatialSpec(), targetScale = 1.08f
            ) + fadeOut(
                animationSpec = motionScheme.slowEffectsSpec()
            ), modifier = modifier
        ) {
            content(zoomLevel) { newLevel ->
                currentLevel = newLevel.coerceIn(0, zoomLevels.size - 1)
            }
        }
    }
}

/**
 * Modifier extension that adds pinch-to-zoom capability with zoom transition animation
 * Uses Material 3 MotionScheme defaultSpatialSpec for smooth in-gesture scaling
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.pinchToZoomGrid(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    zoomThresholdIn: Float = 1.25f,
    zoomThresholdOut: Float = 0.75f
): Modifier {
    var zoom by remember { mutableStateOf(1f) }

    val motionScheme = MotionScheme.expressive()

    val zoomTransition: Float by animateFloatAsState(
        targetValue = zoom,
        animationSpec = motionScheme.defaultSpatialSpec(),
        label = "zoomTransition"
    )

    return this
        .graphicsLayer {
            scaleX = zoomTransition
            scaleY = zoomTransition
        }
        .pointerInput(Unit) {
            detectPinchGestures(pass = PointerEventPass.Initial, onGesture = { _, zoomChange ->
                val newScale = zoom * zoomChange
                when {
                    newScale.compareTo(zoomThresholdIn) > 0 -> {
                        onZoomIn()
                        zoom = 1f
                    }

                    newScale.compareTo(zoomThresholdOut) < 0 -> {
                        onZoomOut()
                        zoom = 1f
                    }

                    else -> {
                        zoom = newScale
                    }
                }
            }, onGestureEnd = {
                zoom = 1f
            })
        }
}
