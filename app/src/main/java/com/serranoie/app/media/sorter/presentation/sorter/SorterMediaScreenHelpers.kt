package com.serranoie.app.media.sorter.presentation.sorter

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import com.serranoie.app.media.sorter.presentation.ui.theme.AureaSpacing
import com.serranoie.app.media.sorter.presentation.ui.theme.components.FileInfo
import com.serranoie.app.media.sorter.presentation.ui.theme.components.GestureGradient
import com.serranoie.app.media.sorter.presentation.ui.theme.components.GestureIndicator
import com.serranoie.app.media.sorter.presentation.ui.theme.components.MediaContent
import com.serranoie.app.media.sorter.presentation.ui.theme.components.MediaInfoOverlay
import com.serranoie.app.media.sorter.presentation.ui.theme.components.MediaTypeBadge
import com.serranoie.app.media.sorter.presentation.ui.theme.components.VideoPlayer
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MediaGestureIndicators(
	keepProgress: Float,
	trashProgress: Float,
	keepIconAlpha: Float,
	keepIconScale: Float,
	keepIconOffset: Float,
	trashIconAlpha: Float,
	trashIconScale: Float,
	trashIconOffset: Float,
	colorScheme: ColorScheme,
	modifier: Modifier = Modifier
) {
	val spacing = AureaSpacing.current

	// Trash gradient (top)
	GestureGradient(
		progress = trashProgress,
		color = colorScheme.error,
		isTop = true,
		modifier = Modifier
			.fillMaxWidth()
			.zIndex(1f)
	)

	// Trash indicator (top)
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = trashIconOffset.coerceAtLeast(0f).dp + spacing.s)
			.zIndex(3f),
		contentAlignment = Alignment.TopCenter
	) {
		GestureIndicator(
			visible = trashIconAlpha > 0.01f,
			icon = Icons.Default.Delete,
			text = "Delete",
			containerColor = colorScheme.errorContainer,
			contentColor = colorScheme.error,
			alpha = trashIconAlpha,
			scale = trashIconScale
		)
	}

	// Keep gradient (bottom)
	GestureGradient(
		progress = keepProgress,
		color = Color(0xFF4CAF50),
		isTop = false,
		modifier = Modifier
			.fillMaxWidth()
			.zIndex(1f)
	)

	// Keep indicator (bottom)
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = keepIconOffset.coerceAtLeast(0f).dp + spacing.xl + spacing.xl)
			.zIndex(3f),
		contentAlignment = Alignment.BottomCenter
	) {
		GestureIndicator(
			visible = keepIconAlpha > 0.01f,
			icon = Icons.Default.CheckCircle,
			text = "Keep",
			containerColor = colorScheme.surface,
			contentColor = Color(0xFF4CAF50),
			alpha = keepIconAlpha,
			scale = keepIconScale
		)
	}
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MediaSwipeContent(
	currentFile: MediaFileUi,
	autoPlayVideos: Boolean,
	isInfoExpanded: Boolean,
	videoProgress: Float,
	isVideoPlaying: Boolean,
	seekToProgress: Float?,
	onVideoProgressChanged: (Float) -> Unit,
	onVideoPlayingChanged: (Boolean) -> Unit,
	onSeekToProgressChanged: (Float?) -> Unit,
	onKeep: () -> Unit,
	onTrash: () -> Unit,
	onKeepGestureProgress: (Float) -> Unit,
	onTrashGestureProgress: (Float) -> Unit,
	onZoomDetected: (Boolean) -> Unit,
	onInfoExpandedChanged: (Boolean) -> Unit,
	snackbarHostState: SnackbarHostState,
	sharedTransitionScope: SharedTransitionScope,
	animatedVisibilityScope: AnimatedVisibilityScope,
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val spacing = AureaSpacing.current

	SwipeableCard(
		modifier = modifier,
		onKeep = onKeep,
		onTrash = onTrash,
		onKeepGestureProgress = onKeepGestureProgress,
		onTrashGestureProgress = onTrashGestureProgress,
		cardContent = {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.pointerInput(Unit) {
						awaitEachGesture {
							awaitFirstDown(requireUnconsumed = false)
							do {
								val event = awaitPointerEvent()
								if (event.changes.size >= 2) {
									val zoom = event.calculateZoom()
									if (zoom > 1.0f) {
										onZoomDetected(true)
										event.changes.forEach { it.consume() }
									} else if (zoom < 1.0f) {
										onZoomDetected(false)
										event.changes.forEach { it.consume() }
									}
								}
							} while (event.changes.any { it.pressed })
						}
					}
			) {
				MediaTypeBadge(
					mediaType = currentFile.mediaType,
					fileName = currentFile.fileName,
					modifier = Modifier
						.zIndex(1f)
						.align(Alignment.TopStart)
						.padding(spacing.s)
				)

				if (currentFile.mediaType == "video") {
					VideoPlayer(
						uri = currentFile.uri,
						autoPlay = autoPlayVideos,
						seekToProgress = seekToProgress,
						onProgressChanged = onVideoProgressChanged,
						onPlayingChanged = onVideoPlayingChanged,
						sharedTransitionScope = sharedTransitionScope,
						animatedVisibilityScope = animatedVisibilityScope,
						isVisible = true,
						modifier = Modifier.fillMaxSize()
					)
				} else {
					MediaContent(
						uri = currentFile.uri,
						fileName = currentFile.fileName,
						mediaType = currentFile.mediaType,
						modifier = Modifier.fillMaxSize()
					)
				}

				MediaInfoOverlay(
					fileInfo = createFileInfo(currentFile),
					isExpanded = isInfoExpanded,
					onExpandToggle = { onInfoExpandedChanged(!isInfoExpanded) },
					onOpenClick = {
						handleOpenFile(currentFile, context, scope, snackbarHostState)
					},
					onShareClick = {
						handleShareFile(currentFile, context, scope, snackbarHostState)
					},
					videoSlider = if (currentFile.mediaType == "video") {
						{
							VideoProgressSlider(
								progress = videoProgress,
								isPlaying = isVideoPlaying,
								onProgressChange = { newProgress ->
									onSeekToProgressChanged(newProgress)
								}
							)
						}
					} else null,
					modifier = Modifier
						.align(Alignment.BottomStart)
						.zIndex(2f)
				)
			}
		}
	)
}

@Composable
private fun VideoProgressSlider(
	progress: Float,
	isPlaying: Boolean,
	onProgressChange: (Float) -> Unit
) {
	WavySlider3(
		value = progress,
		onValueChange = onProgressChange,
		enabled = true,
		waveLength = 22.dp,
		waveHeight = if (isPlaying) 4.dp else 2.dp,
		waveVelocity = if (isPlaying) 8.dp to WaveDirection.HEAD else 0.dp to WaveDirection.HEAD,
		waveThickness = 3.dp,
		trackThickness = 3.dp,
		colors = SliderDefaults.colors(
			thumbColor = MaterialTheme.colorScheme.primary,
			activeTrackColor = MaterialTheme.colorScheme.primary,
			inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
		),
		modifier = Modifier.fillMaxWidth()
	)
}

private fun createFileInfo(file: MediaFileUi): FileInfo {
	return FileInfo(
		fileName = file.fileName,
		fileInfo = file.fileInfo,
		fileSize = file.fileSize,
		dateCreated = file.dateCreated,
		modified = file.modified,
		dimensions = file.dimensions,
		lastAccessed = file.lastAccessed,
		path = file.path
	)
}

private fun handleOpenFile(
	file: MediaFileUi,
	context: android.content.Context,
	scope: CoroutineScope,
	snackbarHostState: SnackbarHostState
) {
	file.uri?.let { uri ->
		try {
			val intent = Intent(Intent.ACTION_VIEW).apply {
				setDataAndType(uri, file.mediaType.let {
					when (it) {
						"video" -> "video/*"
						else -> "image/*"
					}
				})
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}
			context.startActivity(intent)
		} catch (e: Exception) {
			scope.launch {
				snackbarHostState.showSnackbar(
					message = "No app available to open this file",
					duration = SnackbarDuration.Short
				)
			}
			Log.e("SorterMediaScreen", "Error opening file", e)
		}
	}
}

private fun handleShareFile(
	file: MediaFileUi,
	context: android.content.Context,
	scope: CoroutineScope,
	snackbarHostState: SnackbarHostState
) {
	file.uri?.let { uri ->
		try {
			val intent = Intent(Intent.ACTION_SEND).apply {
				type = file.mediaType.let {
					when (it) {
						"video" -> "video/*"
						else -> "image/*"
					}
				}
				putExtra(Intent.EXTRA_STREAM, uri)
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}
			val chooser = Intent.createChooser(intent, "Share via")
			context.startActivity(chooser)
		} catch (e: Exception) {
			scope.launch {
				snackbarHostState.showSnackbar(
					message = "Unable to share file",
					duration = SnackbarDuration.Short
				)
			}
			Log.e("SorterMediaScreen", "Error sharing file", e)
		}
	}
}

fun handleTrashWithUndo(
	scope: CoroutineScope,
	snackbarHostState: SnackbarHostState,
	onTrash: () -> MediaFileUi?,
	onUndo: () -> Unit
) {
	val trashedFile = onTrash()
	trashedFile?.let {
		scope.launch {
			val snackbarJob = launch {
				delay(2000)
				snackbarHostState.currentSnackbarData?.dismiss()
			}

			val result = snackbarHostState.showSnackbar(
				message = "Media moved to trash",
				actionLabel = "UNDO",
				duration = SnackbarDuration.Indefinite,
				withDismissAction = false
			)

			snackbarJob.cancel()

			if (result == SnackbarResult.ActionPerformed) {
				onUndo()
			}
		}
	}
}
