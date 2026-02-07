package com.serranoie.app.media.sorter.presentation.ui.theme.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Visual feedback component that briefly tints the screen when a gesture action is completed.
 * 
 * Features:
 * - Flash tint effect when action is triggered
 * - Auto-fades out after 250ms
 * - Works with both blurred and solid backgrounds
 * - Independent of background type
 * - Automatically resets state after animation
 * 
 * @param actionType The type of action (Keep or Trash), or null if no action
 * @param onAnimationComplete Callback when animation completes (resets state)
 * @param modifier Additional modifiers
 */
@Composable
fun ActionFeedbackTint(
    actionType: ActionType?,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(actionType) {
        if (actionType != null) {
            alpha.snapTo(0.15f)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 2000)
            )
            onAnimationComplete()
        }
    }
    
    val tintColor = when (actionType) {
        ActionType.Keep -> Color(0xFF4CAF50)
        ActionType.Trash -> Color(0xFFEF5350)
        null -> Color.Transparent
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tintColor.copy(alpha = alpha.value))
    )
}

enum class ActionType {
    Keep,
    Trash
}
