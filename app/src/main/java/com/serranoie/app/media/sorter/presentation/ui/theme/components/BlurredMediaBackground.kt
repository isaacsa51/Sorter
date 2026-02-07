package com.serranoie.app.media.sorter.presentation.ui.theme.components

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale

/**
 * Blurred media background component that creates an immersive backdrop
 * Uses Compose's built-in blur modifier which works on all Android versions
 *
 * @param uri The URI of the media file (image or video)
 * @param mediaType The type of media ("video" or other for images)
 * @param enabled Whether the blurred background is enabled
 * @param blurRadius The radius of the blur effect in dp (default 25f)
 * @param alpha The opacity of the blurred background (default 0.6f)
 * @param modifier Modifier to be applied to the background
 */
@Composable
fun BlurredMediaBackground(
	uri: Uri?,
	mediaType: String,
	enabled: Boolean = true,
	blurRadius: Float = 25f,
	alpha: Float = 0.6f,
	modifier: Modifier = Modifier
) {
	val colorScheme = MaterialTheme.colorScheme

	// Animate the alpha when toggling between blurred background and solid color
	val animatedAlpha by animateFloatAsState(
		targetValue = if (enabled && uri != null) alpha else 0f,
		animationSpec = tween(durationMillis = 300),
		label = "backgroundAlpha"
	)

	Box(modifier = modifier.fillMaxSize()) {
		// Solid color background (always present as fallback)
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(colorScheme.surface)
		)

		// Blurred media background (only when enabled and URI is available)
		if (uri != null) {
			val context = LocalContext.current

			// Build image request with lower quality for background
			// For videos, Coil will automatically extract a frame
			val imageRequest = remember(uri) {
				ImageRequest.Builder(context).data(uri)
					.size(400) // Lower resolution for better performance
					.scale(Scale.FIT).allowHardware(true) // Enable hardware acceleration
					.crossfade(true).build()
			}

			AsyncImage(
				model = imageRequest,
				contentDescription = null,
				modifier = Modifier
					.fillMaxSize()
					.blur(radius = blurRadius.dp) // Compose blur works on all Android versions
					.graphicsLayer {
						this.alpha = animatedAlpha
					},
				contentScale = ContentScale.Crop
			)

			// Dark overlay to ensure content readability
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(colorScheme.surface.copy(alpha = 0.3f))
					.graphicsLayer { this.alpha = animatedAlpha })
		}
	}
}
