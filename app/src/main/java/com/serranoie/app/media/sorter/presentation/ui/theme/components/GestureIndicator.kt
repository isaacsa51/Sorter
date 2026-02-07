@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.serranoie.app.media.sorter.presentation.ui.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.res.stringResource
import com.serranoie.app.media.sorter.R

/**
 * Gesture indicator component that shows feedback when swiping
 * 
 * @param visible Whether the indicator should be visible
 * @param icon The icon to display in the indicator
 * @param text The text label to show below the icon
 * @param containerColor The background color of the circular container
 * @param contentColor The color of the icon and text
 * @param alpha The alpha/opacity value for the indicator
 * @param scale The scale factor for the indicator size
 * @param modifier Modifier to be applied to the indicator
 */
@Composable
fun GestureIndicator(
    visible: Boolean,
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    alpha: Float,
    scale: Float,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200, easing = EaseOut)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(200, easing = EaseOut)
        ),
        exit = fadeOut(animationSpec = tween(150)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(150)
        ),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier
                    .size((64 * scale).dp)
                    .graphicsLayer { this.alpha = alpha }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = contentColor,
                        modifier = Modifier.size((36 * scale).dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (scale > 1f) 12.dp else 8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.graphicsLayer { this.alpha = alpha }
            )
        }
    }
}

@Preview
@Composable
private fun GestureIndicatorPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GestureIndicator(
                visible = true,
                icon = Icons.Default.Delete,
                text = stringResource(R.string.gesture_delete),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
                alpha = 1f,
                scale = 1f
            )
        }
    }
}

@Preview
@Composable
private fun GestureIndicatorKeepPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GestureIndicator(
                visible = true,
                icon = Icons.Default.CheckCircle,
                text = stringResource(R.string.gesture_keep),
                containerColor = Color(0xFF4CAF50),
                contentColor = MaterialTheme.colorScheme.surface,
                alpha = 1f,
                scale = 1.2f
            )
        }
    }
}
