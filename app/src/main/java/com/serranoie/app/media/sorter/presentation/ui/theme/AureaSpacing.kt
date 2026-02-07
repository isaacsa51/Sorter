package com.serranoie.app.media.sorter.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import kotlin.math.pow

/**
 * The Golden Ratio (φ - phi)
 * Found throughout nature and creates visual harmony
 */
private const val PHI = 1.618034f

/**
 * Spacing levels based on the golden ratio
 */
enum class SpacingLevel {
    XS,  // Extra Small: base ÷ φ² ≈ 6dp
    S,   // Small: base ÷ φ ≈ 10dp
    M,   // Medium: base = 16dp
    L,   // Large: base × φ ≈ 26dp
    XL   // Extra Large: base × φ² ≈ 42dp
}

/**
 * Data class holding all Aurea spacing values
 * Each level relates to others by the golden ratio φ
 */
data class PhiSpacing(
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp
)

/**
 * CompositionLocal for Aurea spacing values
 */
val LocalAureaSpacing = compositionLocalOf {
    PhiSpacing(
        xs = 6.dp,
        s = 10.dp,
        m = 16.dp,
        l = 26.dp,
        xl = 42.dp
    )
}

/**
 * CompositionLocal for toggling Aurea padding on/off
 */
val LocalUseAureaPadding = compositionLocalOf { false }

/**
 * Provider component that calculates and provides Aurea spacing values
 * based on screen size and the golden ratio
 * 
 * @param useAureaPadding Whether to enable Aurea padding system
 * @param content The composable content that will have access to Aurea spacing
 */
@Composable
fun AureaScaleProvider(
    useAureaPadding: Boolean,
    content: @Composable () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.toFloat()
    
    // Calculate scale factor based on screen width (360dp reference)
    // Coerce to max 1.5x to prevent excessive scaling on tablets
    val scaleFactor = (screenWidthDp / 360f).coerceAtMost(1.5f)
    
    // Base unit that scales with screen size
    val baseUnit = 16.dp * scaleFactor
    
    // Calculate all spacing levels using golden ratio
    val spacing = PhiSpacing(
        xs = baseUnit / PHI.pow(2),  // ≈ 6.08dp on 360dp screen
        s = baseUnit / PHI,           // ≈ 9.89dp
        m = baseUnit,                 // = 16.00dp
        l = baseUnit * PHI,           // ≈ 25.89dp
        xl = baseUnit * PHI.pow(2)    // ≈ 41.89dp
    )
    
    CompositionLocalProvider(
        LocalAureaSpacing provides spacing,
        LocalUseAureaPadding provides useAureaPadding
    ) {
        content()
    }
}

/**
 * Extension function to get a specific spacing level value
 */
@Composable
fun SpacingLevel.toDP(): Dp {
    val spacing = LocalAureaSpacing.current
    return when (this) {
        SpacingLevel.XS -> spacing.xs
        SpacingLevel.S -> spacing.s
        SpacingLevel.M -> spacing.m
        SpacingLevel.L -> spacing.l
        SpacingLevel.XL -> spacing.xl
    }
}

/**
 * Convenience modifier to apply Aurea padding if enabled
 * Falls back to regular padding if Aurea padding is disabled
 * 
 * @param level The spacing level to apply
 * @param all If true, apply to all sides; otherwise apply to top/bottom only
 */
@Composable
fun Modifier.phiPadding(
    level: SpacingLevel = SpacingLevel.M,
    all: Boolean = false
): Modifier {
    val useAurea = LocalUseAureaPadding.current
    val spacing = level.toDP()
    
    return if (useAurea) {
        if (all) {
            this.padding(spacing)
        } else {
            this.padding(vertical = spacing)
        }
    } else {
        // Fallback to standard Material padding
        val standardPadding = when (level) {
            SpacingLevel.XS -> 4.dp
            SpacingLevel.S -> 8.dp
            SpacingLevel.M -> 16.dp
            SpacingLevel.L -> 24.dp
            SpacingLevel.XL -> 32.dp
        }
        if (all) {
            this.padding(standardPadding)
        } else {
            this.padding(vertical = standardPadding)
        }
    }
}

/**
 * Convenience composable to get current Aurea spacing values
 */
object AureaSpacing {
    val current: PhiSpacing
        @Composable
        get() = LocalAureaSpacing.current
    
    val isEnabled: Boolean
        @Composable
        get() = LocalUseAureaPadding.current
}
