package com.serranoie.app.media.sorter.ui.theme.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.serranoie.app.media.sorter.ui.theme.util.ComponentPreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper

/**
 * Gradient component that appears at the edge of the screen during swipe gestures
 *
 * @param progress The gesture progress (0f to 1f)
 * @param color The color of the gradient
 * @param isTop Whether the gradient should appear at the top (true) or bottom (false)
 * @param modifier Modifier to be applied to the gradient
 */
@Composable
fun GestureGradient(
    progress: Float, color: Color, isTop: Boolean = true, modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = progress, animationSpec = tween(200), label = "gradientAlpha"
    )

    val gradientHeight = 250.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(gradientHeight)
            .background(
                brush = if (isTop) {
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.5f * animatedAlpha),
                            color.copy(alpha = 0.15f * animatedAlpha),
                            Color.Transparent
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            color.copy(alpha = 0.15f * animatedAlpha),
                            color.copy(alpha = 0.5f * animatedAlpha)
                        )
                    )
                }
            )
    )
}

@ComponentPreview
@Composable
private fun GestureGradientTopPreview() {
    PreviewWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F))
        ) {
            GestureGradient(
                progress = 1f,
                color = MaterialTheme.colorScheme.error,
                isTop = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@ComponentPreview
@Composable
private fun GestureGradientBottomPreview() {
    PreviewWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F))
        ) {
            GestureGradient(
                progress = 1f,
                color = Color(0xff55db5c),
                isTop = false,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@ComponentPreview
@Composable
private fun GestureGradientPartialPreview() {
    PreviewWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F))
        ) {
            GestureGradient(
                progress = 0.5f,
                color = MaterialTheme.colorScheme.error,
                isTop = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
