package com.serranoie.app.media.sorter.ui.theme.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared element key for identifying media images across different composables.
 * Uses URI to uniquely identify each media file.
 */
data class ImageElementKey(val uri: Uri?)

/**
 * Custom modifier that creates a shared bounds transition with shape morphing.
 * 
 * Features:
 * - Animates bounds size and position
 * - Morphs shape corner radius (e.g., 24dp rounded to 0dp square)
 * - Transitions background color seamlessly
 * - Uses skipToLookaheadSize() to prevent children interference
 * 
 * @param sharedContentState The shared content state for the element
 * @param sharedTransitionScope The scope managing the shared transition
 * @param animatedVisibilityScope The scope for animated visibility
 * @param resizeMode How the element should resize during transition
 * @param restingShapeCornerRadius Corner radius when element is at rest (visible state)
 * @param targetShapeCornerRadius Corner radius when transitioning (enter/exit)
 * @param renderInOverlayDuringTransition Whether to render in overlay layer
 * @param keepChildrenSizePlacement Whether to skip children size to lookahead
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedBoundsRevealWithShapeMorph(
    sharedContentState: SharedTransitionScope.SharedContentState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
    restingShapeCornerRadius: Dp = 0.dp,
    targetShapeCornerRadius: Dp = 0.dp,
    renderInOverlayDuringTransition: Boolean = true,
    keepChildrenSizePlacement: Boolean = true,
): Modifier {
    with(sharedTransitionScope) {
        // Animate corner radius based on transition state
        val animatedCornerRadius = animatedVisibilityScope.transition.animateDp(
            label = "cornerRadius"
        ) { state ->
            when (state) {
                EnterExitState.PreEnter -> targetShapeCornerRadius
                EnterExitState.Visible -> restingShapeCornerRadius
                EnterExitState.PostExit -> targetShapeCornerRadius
            }
        }

        val clipShape = RoundedCornerShape(animatedCornerRadius.value)
        
        // Conditionally apply size skipping for children
        val modifier = if (keepChildrenSizePlacement) {
            Modifier.skipToLookaheadSize()
        } else {
            Modifier
        }

        return this@sharedBoundsRevealWithShapeMorph
            .sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = resizeMode,
                clipInOverlayDuringTransition = OverlayClip(clipShape),
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            )
            .then(modifier)
    }
}
