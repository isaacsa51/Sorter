package com.serranoie.app.media.sorter.presentation.sorter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.serranoie.app.media.sorter.ui.theme.components.BlurredMediaBackground
import com.serranoie.app.media.sorter.ui.theme.components.CompletionScreen
import com.serranoie.app.media.sorter.ui.theme.components.FileInfo
import com.serranoie.app.media.sorter.ui.theme.components.GestureGradient
import com.serranoie.app.media.sorter.ui.theme.components.GestureIndicator
import com.serranoie.app.media.sorter.ui.theme.components.MediaInfoOverlay
import com.serranoie.app.media.sorter.ui.theme.components.MediaTypeBadge
import com.serranoie.app.media.sorter.ui.theme.components.SorterTopAppBar
import com.serranoie.app.media.sorter.ui.theme.components.ZoomOverlay
import com.serranoie.app.media.sorter.ui.theme.components.ZoomableMediaContent
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import com.serranoie.app.media.sorter.ui.theme.util.DevicePreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SorterMediaScreen(
    currentFile: MediaFileUi?,
    isCompleted: Boolean,
    deletedCount: Int,
    useBlurredBackground: Boolean,
    onKeepCurrent: () -> Unit,
    onTrashCurrent: () -> MediaFileUi?, // Now returns the trashed file
    onUndoTrash: () -> Unit,
    onToggleBackground: () -> Unit,
    onBackToOnboarding: () -> Unit = {},
    onNavigateToReview: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    // Gesture progress states for animated icons
    var keepProgress by remember { mutableStateOf(0f) }
    var trashProgress by remember { mutableStateOf(0f) }

    // State for expandable info panel
    var isInfoExpanded by remember { mutableStateOf(false) }
    
    // State for zoom mode (shows overlay when true)
    var isZoomed by remember { mutableStateOf(false) }

    // Reset expanded state when file changes
    LaunchedEffect(currentFile?.id) {
        isInfoExpanded = false
        isZoomed = false
    }

    // Animated values for keep icon (bottom, green)
    val keepIconAlpha = animateFloatAsState(keepProgress, animationSpec = tween(240)).value
    val keepIconScale =
        animateFloatAsState(0.7f + 0.5f * keepProgress, animationSpec = tween(240)).value
    val keepIconOffset =
        animateFloatAsState((-32f + 60f * keepProgress), animationSpec = tween(240)).value

    // Animated values for trash icon (top, red)
    val trashIconAlpha = animateFloatAsState(trashProgress, animationSpec = tween(240)).value
    val trashIconScale =
        animateFloatAsState(0.7f + 0.5f * trashProgress, animationSpec = tween(240)).value
    val trashIconOffset =
        animateFloatAsState((32f - 60f * trashProgress), animationSpec = tween(240)).value

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        SharedTransitionLayout {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Blurred media background or solid color
	            BlurredMediaBackground(
		            uri = currentFile?.uri,
		            mediaType = currentFile?.mediaType ?: "image",
		            enabled = useBlurredBackground && currentFile != null && !isCompleted,
		            modifier = Modifier.fillMaxSize()
	            )
                if (isCompleted || currentFile == null) {
                    // Completion screen
	                CompletionScreen(
		                deletedCount = deletedCount,
		                onReviewDeleted = onNavigateToReview,
		                onBackToTutorial = onBackToOnboarding
	                )
                } else {
                    AnimatedVisibility(
                        visible = true,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                        GestureGradient(
	                        progress = trashProgress,
	                        color = colorScheme.error,
	                        isTop = true,
	                        modifier = Modifier
		                        .align(Alignment.TopCenter)
		                        .zIndex(1f)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = trashIconOffset.coerceAtLeast(0f).dp + 24.dp)
                                .zIndex(3f),
                            contentAlignment = Alignment.TopCenter
                        ) {
	                        GestureIndicator(
		                        visible = trashIconAlpha > 0.01f,
		                        icon = Icons.Default.Delete,
		                        text = "Release to delete",
		                        containerColor = colorScheme.errorContainer,
		                        contentColor = colorScheme.error,
		                        alpha = trashIconAlpha,
		                        scale = trashIconScale
	                        )
                        }

                        // Main swipeable card content
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
	                        SwipeableCard(
		                        //fileName = currentFile.fileName,
		                        //fileInfo = currentFile.fileInfo,
		                        modifier = Modifier.fillMaxSize(),
		                        onKeep = onKeepCurrent,
		                        onTrash = {
			                        val trashedFile = onTrashCurrent()
			                        
			                        // Show snackbar with undo action (2 second duration)
			                        trashedFile?.let { file ->
				                        scope.launch {
					                        // Use custom 2-second timeout with manual dismissal
					                        val snackbarJob = launch {
						                        kotlinx.coroutines.delay(2000)
						                        snackbarHostState.currentSnackbarData?.dismiss()
					                        }
					                        
					                        val result = snackbarHostState.showSnackbar(
						                        message = "${file.fileName} moved to trash",
						                        actionLabel = "UNDO",
						                        duration = SnackbarDuration.Indefinite,
						                        withDismissAction = false
					                        )
					                        
					                        snackbarJob.cancel() // Cancel auto-dismiss if action performed
					                        
					                        if (result == SnackbarResult.ActionPerformed) {
						                        onUndoTrash()
					                        }
				                        }
			                        }
		                        },
		                        onKeepGestureProgress = { keepProgress = it },
		                        onTrashGestureProgress = { trashProgress = it },
		                        cardContent = {
			                        Box(
				                        modifier = Modifier
					                        .fillMaxSize()
					                        .pointerInput(Unit) {
						                        // Only detect multi-finger pinch gestures
						                        awaitEachGesture {
							                        awaitFirstDown(requireUnconsumed = false)

							                        do {
								                        val event = awaitPointerEvent()

								                        // Only process if 2 or more fingers
								                        if (event.changes.size >= 2) {
									                        val zoom = event.calculateZoom()

									                        // Pinch out to open zoom
									                        if (zoom > 1.0f && !isZoomed) {
										                        isZoomed = true
										                        // Consume the event so it doesn't go to card
										                        event.changes.forEach { it.consume() }
									                        }
									                        // Pinch in to close zoom
									                        else if (zoom < 1.0f && isZoomed) {
										                        isZoomed = false
										                        event.changes.forEach { it.consume() }
									                        }
								                        }
								                        // Single finger: don't consume, let it pass to Card

							                        } while (event.changes.any { it.pressed })
						                        }
					                        }
			                        ) {
				                        // Media type badge at top-right
				                        MediaTypeBadge(
					                        mediaType = currentFile.mediaType,
					                        modifier = Modifier
						                        .zIndex(1f)
						                        .align(Alignment.TopStart)
						                        .padding(12.dp)
				                        )

				                        // Static (non-zoomable) media content in card
				                        ZoomableMediaContent(
					                        uri = currentFile.uri,
					                        fileName = currentFile.fileName,
					                        mediaType = currentFile.mediaType,
					                        sharedTransitionScope = this@SharedTransitionLayout,
					                        animatedVisibilityScope = this@AnimatedVisibility,
					                        isVisible = !isZoomed,
					                        modifier = Modifier.fillMaxSize()
				                        )

				                        // Bottom overlay with info and actions
				                        MediaInfoOverlay(
					                        fileInfo = FileInfo(
						                        fileName = currentFile.fileName,
						                        fileInfo = currentFile.fileInfo,
						                        fileSize = currentFile.fileSize,
						                        dateCreated = currentFile.dateCreated,
						                        modified = currentFile.modified,
						                        dimensions = currentFile.dimensions,
						                        lastAccessed = currentFile.lastAccessed,
						                        path = currentFile.path
					                        ),
					                        isExpanded = isInfoExpanded,
					                        onExpandToggle = { isInfoExpanded = !isInfoExpanded },
					                        onOpenClick = { /* Open action */ },
					                        onShareClick = { /* Share action */ },
					                        modifier = Modifier
						                        .align(Alignment.BottomStart)
						                        .zIndex(2f)
				                        )
			                        }
		                        })
                        }

	                        GestureGradient(
		                        progress = keepProgress,
		                        color = Color(0xFF4CAF50),
		                        isTop = false,
		                        modifier = Modifier
			                        .align(Alignment.BottomCenter)
			                        .zIndex(1f)
	                        )

                        // Keep Reveal at Bottom
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = keepIconOffset.coerceAtLeast(0f).dp + 80.dp)
                                .zIndex(3f), contentAlignment = Alignment.BottomCenter
                        ) {
	                        GestureIndicator(
		                        visible = keepIconAlpha > 0.01f,
		                        icon = Icons.Default.CheckCircle,
		                        text = "Release to keep",
		                        containerColor = MaterialTheme.colorScheme.surface,
		                        contentColor = Color(0xFF4CAF50),
		                        alpha = keepIconAlpha,
		                        scale = keepIconScale
	                        )
                        }

	                        SorterTopAppBar(
		                        date = currentFile.date,
		                        deletedCount = deletedCount,
		                        useBlurredBackground = useBlurredBackground,
		                        onToggleBackground = onToggleBackground,
		                        onNavigateToReview = onNavigateToReview,
		                        modifier = Modifier
			                        .align(Alignment.TopStart)
			                        .zIndex(2f)
	                        )
                        }
                        
                        // Zoom overlay - renders on top of everything when zooming
                        if (!isCompleted && currentFile != null) {
	                        ZoomOverlay(
		                        uri = currentFile.uri,
		                        fileName = currentFile.fileName,
		                        sharedTransitionScope = this@SharedTransitionLayout,
		                        animatedVisibilityScope = this@AnimatedVisibility,
		                        isVisible = isZoomed,
		                        onDismiss = { isZoomed = false },
		                        modifier = Modifier
			                        .fillMaxSize()
			                        .zIndex(100f)
	                        )
                        }
                    }
                }
            }
        }
    }
}

@DevicePreview
@Composable
fun SorterMediaScreenPreview() {
    PreviewWrapper {
        SorterMediaScreen(
            currentFile = MediaFileUi(
                id = "1",
                fileName = "Beach_Sunset_01.jpg",
                fileInfo = "2.5 MB • Yesterday",
                mediaType = "image",
                date = "Yesterday",
                fileSize = "2.5 MB",
                dimensions = "4032x3024",
                dateCreated = "2025-01-08 10:30 AM",
                lastAccessed = "2025-01-09 09:15 AM",
                modified = "Yesterday",
                path = "/photos/beach/"
            ),
            isCompleted = false,
            deletedCount = 3,
            useBlurredBackground = true,
            onKeepCurrent = {},
            onTrashCurrent = { null },
            onUndoTrash = {},
            onToggleBackground = {},
            onBackToOnboarding = {},
            onNavigateToReview = {}
        )
    }
}
