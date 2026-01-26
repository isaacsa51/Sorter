package com.serranoie.app.media.sorter.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * AUREA SPACING USAGE EXAMPLES
 *
 * This file demonstrates various ways to use the Aurea (Golden Ratio) spacing system.
 * The system is automatically enabled/disabled based on user settings and provides
 * harmonious spacing values based on the golden ratio φ ≈ 1.618
 */

// ============================================================================
// METHOD 1: Using the phiPadding() Modifier (Simplest)
// ============================================================================

@Composable
fun ExampleWithPhiPaddingModifier() {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.phiPadding(SpacingLevel.M)  // Applies Aurea padding if enabled
	) {
		Text("This uses phi padding")
		Text("Spacing adjusts based on golden ratio")
	}
}

// ============================================================================
// METHOD 2: Using AureaSpacing Object (Direct Access)
// ============================================================================

@Composable
fun ExampleWithDirectAccess() {
	val spacing = AureaSpacing.current
	val isAureaEnabled = AureaSpacing.isEnabled

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				horizontal = spacing.M,
				vertical = spacing.S
			)
	) {
		Text(
			text = "Aurea spacing: ${if (isAureaEnabled) "Enabled" else "Disabled"}",
			style = MaterialTheme.typography.bodyLarge
		)

		Spacer(modifier = Modifier.height(spacing.L))

		Text(
			text = "Using different spacing levels",
			modifier = Modifier.padding(top = spacing.XS)
		)
	}
}

// ============================================================================
// METHOD 3: Using SpacingLevel.toDP() Extension
// ============================================================================

@Composable
fun ExampleWithSpacingLevel() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Text(
			"Extra Small Spacing",
			modifier = Modifier.padding(SpacingLevel.XS.toDP())
		)

		Text(
			"Small Spacing",
			modifier = Modifier.padding(SpacingLevel.S.toDP())
		)

		Text(
			"Medium Spacing",
			modifier = Modifier.padding(SpacingLevel.M.toDP())
		)

		Text(
			"Large Spacing",
			modifier = Modifier.padding(SpacingLevel.L.toDP())
		)

		Text(
			"Extra Large Spacing",
			modifier = Modifier.padding(SpacingLevel.XL.toDP())
		)
	}
}

// ============================================================================
// METHOD 4: Conditional Spacing (Aurea vs Standard)
// ============================================================================

@Composable
fun ExampleWithConditionalSpacing() {
	val spacing = AureaSpacing.current
	val useAurea = AureaSpacing.isEnabled

	// Manually choose between Aurea and standard spacing
	val verticalPadding = if (useAurea) spacing.L else 24.dp

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = verticalPadding)
	) {
		Text("This demonstrates conditional spacing")
	}
}

// ============================================================================
// METHOD 5: Complex Layout with Multiple Spacing Levels
// ============================================================================

@Composable
fun ExampleComplexLayout() {
	val spacing = AureaSpacing.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = spacing.M)
	) {
		// Header with large spacing
		Text(
			text = "Header",
			style = MaterialTheme.typography.headlineMedium,
			modifier = Modifier.padding(vertical = spacing.L)
		)

		// Content with medium spacing
		repeat(3) { index ->
			Text(
				text = "Item ${index + 1}",
				modifier = Modifier.padding(vertical = spacing.S)
			)
		}

		// Footer with extra large spacing
		Text(
			text = "Footer",
			modifier = Modifier.padding(top = spacing.XL)
		)
	}
}

// ============================================================================
// SPACING LEVEL REFERENCE
// ============================================================================

/**
 * Spacing Levels (on 360dp reference screen):
 *
 * XS (Extra Small) ≈ 6.08dp   - For tight spacing, small gaps
 * S  (Small)       ≈ 9.89dp   - For compact layouts
 * M  (Medium)      = 16.00dp  - Standard Material spacing
 * L  (Large)       ≈ 25.89dp  - For section spacing
 * XL (Extra Large) ≈ 41.89dp  - For major section breaks
 *
 * All values scale with screen size and maintain golden ratio relationships
 * Each level is φ (phi ≈ 1.618) times the previous level
 */

// ============================================================================
// MATHEMATICAL RELATIONSHIPS
// ============================================================================

/**
 * Golden Ratio Properties:
 *
 * φ = (1 + √5) / 2 ≈ 1.618033988749895
 *
 * Relationships:
 * XS = M / φ²
 * S  = M / φ
 * M  = base (16dp)
 * L  = M × φ
 * XL = M × φ²
 *
 * This ensures:
 * S / XS = φ
 * M / S  = φ
 * L / M  = φ
 * XL / L = φ
 *
 * Creating perfect harmony throughout the UI!
 */
/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */
@Preview
@Composable
private fun ExampleWithPhiPaddingModifierPreview() {
	ExampleWithPhiPaddingModifier(
	)
}
/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */

/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */
@Preview
@Composable
private fun ExampleWithDirectAccessPreview() {
	ExampleWithDirectAccess(
	)
}
/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */

/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */
@Preview
@Composable
private fun ExampleWithSpacingLevelPreview() {
	ExampleWithSpacingLevel(
	)
}
/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */

/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */
@Preview
@Composable
private fun ExampleWithConditionalSpacingPreview() {
	ExampleWithConditionalSpacing(
	)
}
/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */

/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */
@Preview
@Composable
private fun ExampleComplexLayoutPreview() {
	ExampleComplexLayout(
	)
}
/**
 * Auto-generated by Compose Preview Generator.
 * Upgrade your license to remove auto-generated comments.
 * https://plugins.jetbrains.com/plugin/25716-compose-preview-generator
 */

