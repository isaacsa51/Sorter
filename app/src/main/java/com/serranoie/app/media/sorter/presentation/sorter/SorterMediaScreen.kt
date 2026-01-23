package com.serranoie.app.media.sorter.presentation.sorter

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.serranoie.app.media.sorter.ui.theme.components.ActionFeedbackTint
import com.serranoie.app.media.sorter.ui.theme.components.ActionType
import com.serranoie.app.media.sorter.ui.theme.components.BlurredMediaBackground
import com.serranoie.app.media.sorter.ui.theme.components.CompletionScreen
import com.serranoie.app.media.sorter.ui.theme.components.FileInfo
import com.serranoie.app.media.sorter.ui.theme.components.GestureGradient
import com.serranoie.app.media.sorter.ui.theme.components.GestureIndicator
import com.serranoie.app.media.sorter.ui.theme.components.MediaInfoOverlay
import com.serranoie.app.media.sorter.ui.theme.components.MediaTypeBadge
import com.serranoie.app.media.sorter.ui.theme.components.SorterTopAppBar
import com.serranoie.app.media.sorter.ui.theme.components.VideoPlayer
import com.serranoie.app.media.sorter.ui.theme.components.ZoomOverlay
import com.serranoie.app.media.sorter.ui.theme.components.ZoomableMediaContent
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3
import com.serranoie.app.media.sorter.ui.theme.util.DevicePreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SorterMediaScreen(
    currentFile: MediaFileUi?,
    isCompleted: Boolean,
    deletedCount: Int,
    useBlurredBackground: Boolean,
    autoPlayVideos: Boolean,
    onKeepCurrent: () -> Unit,
    onTrashCurrent: () -> MediaFileUi?,
    onUndoTrash: () -> Unit,
    onToggleBackground: () -> Unit,
    onBackToOnboarding: () -> Unit = {},
    onNavigateToReview: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var videoProgress by remember { mutableStateOf(0f) }
    var isVideoPlaying by remember { mutableStateOf(autoPlayVideos) }
    var seekToProgress by remember { mutableStateOf<Float?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    var keepProgress by remember { mutableFloatStateOf(0f) }
    var trashProgress by remember { mutableStateOf(0f) }
    var isInfoExpanded by remember { mutableStateOf(false) }
    var isZoomed by remember { mutableStateOf(false) }
    var actionFeedback by remember { mutableStateOf<ActionType?>(null) }

    LaunchedEffect(currentFile?.id) {
        isInfoExpanded = false
        isZoomed = false
        videoProgress = 0f
        isVideoPlaying = autoPlayVideos
        seekToProgress = null
    }

    val keepIconAlpha = animateFloatAsState(keepProgress, animationSpec = tween(240)).value
    val keepIconScale =
        animateFloatAsState(0.7f + 0.5f * keepProgress, animationSpec = tween(240)).value
    val keepIconOffset =
        animateFloatAsState((-32f + 60f * keepProgress), animationSpec = tween(240)).value

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
	            BlurredMediaBackground(
		            uri = currentFile?.uri,
		            mediaType = currentFile?.mediaType ?: "image",
		            enabled = useBlurredBackground && currentFile != null && !isCompleted,
		            modifier = Modifier.fillMaxSize()
	            )
	            
	            ActionFeedbackTint(
		            actionType = actionFeedback,
		            onAnimationComplete = { actionFeedback = null },
		            modifier = Modifier
			            .fillMaxSize()
			            .zIndex(0.5f)
	            )
	            
                if (isCompleted || currentFile == null) {
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
		                        text = "Delete",
		                        containerColor = colorScheme.errorContainer,
		                        contentColor = colorScheme.error,
		                        alpha = trashIconAlpha,
		                        scale = trashIconScale
	                        )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
	                        SwipeableCard(
		                        modifier = Modifier.fillMaxSize(),
		                        onKeep = {
			                        actionFeedback = ActionType.Keep
			                        onKeepCurrent()
		                        },
		                        onTrash = {
			                        actionFeedback = ActionType.Trash
			                        val trashedFile = onTrashCurrent()
			                        
			                        trashedFile?.let { file ->
				                        scope.launch {
					                        val snackbarJob = launch {
						                        kotlinx.coroutines.delay(2000)
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
						                        awaitEachGesture {
							                        awaitFirstDown(requireUnconsumed = false)

							                        do {
								                        val event = awaitPointerEvent()

								                        if (event.changes.size >= 2) {
									                        val zoom = event.calculateZoom()

									                        if (zoom > 1.0f && !isZoomed) {
										                        isZoomed = true
										                        event.changes.forEach { it.consume() }
									                        }
									                        else if (zoom < 1.0f && isZoomed) {
										                        isZoomed = false
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
						                        .padding(12.dp)
				                        )

				                        if (currentFile.mediaType == "video") {
					                        VideoPlayer(
						                        uri = currentFile.uri,
						                        autoPlay = autoPlayVideos,
						                        seekToProgress = seekToProgress,
						                        onProgressChanged = { progress ->
							                        videoProgress = progress
							                        seekToProgress = null
						                        },
						                        onPlayingChanged = { playing ->
							                        isVideoPlaying = playing
						                        },
						                        sharedTransitionScope = this@SharedTransitionLayout,
						                        animatedVisibilityScope = this@AnimatedVisibility,
						                        isVisible = !isZoomed,
						                        modifier = Modifier.fillMaxSize()
					                        )
				                        } else {
					                        ZoomableMediaContent(
						                        uri = currentFile.uri,
						                        fileName = currentFile.fileName,
						                        mediaType = currentFile.mediaType,
						                        sharedTransitionScope = this@SharedTransitionLayout,
						                        animatedVisibilityScope = this@AnimatedVisibility,
						                        isVisible = !isZoomed,
						                        modifier = Modifier.fillMaxSize()
					                        )
				                        }

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
					                        onOpenClick = {
						                        currentFile.uri?.let { uri ->
							                        try {
								                        val intent = Intent(Intent.ACTION_VIEW).apply {
									                        setDataAndType(uri, currentFile.mediaType.let {
										                        when (it) {
											                        "video" -> "video/*"
											                        else -> "image/*"
										                        }
									                        })
									                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
								                        }
								                        val chooser = Intent.createChooser(intent, "Open with")
								                        context.startActivity(chooser)
							                        } catch (e: Exception) {
								                        scope.launch {
									                        snackbarHostState.showSnackbar(
										                        message = "No app available to open this file",
										                        duration = SnackbarDuration.Short
									                        )
								                        }
							                        }
						                        }
					                        },
					                        onShareClick = {
						                        currentFile.uri?.let { uri ->
							                        try {
								                        val intent = Intent(Intent.ACTION_SEND).apply {
									                        type = currentFile.mediaType.let {
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
							                        }
						                        }
					                        },
					                        videoSlider = if (currentFile.mediaType == "video") {
						                        {
							                        WavySlider3(
								                        value = videoProgress,
								                        onValueChange = { newProgress ->
									                        seekToProgress = newProgress
								                        },
								                        enabled = true,
								                        waveLength = 22.dp,
								                        waveHeight = if (isVideoPlaying) 4.dp else 2.dp,
								                        waveVelocity = if (isVideoPlaying) 8.dp to WaveDirection.HEAD else 0.dp to WaveDirection.HEAD,
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
					                        } else null,
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
		                        text = "Keep",
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
                        
                        if (!isCompleted && currentFile != null) {
	                        ZoomOverlay(
		                        uri = currentFile.uri,
		                        fileName = currentFile.fileName,
		                        mediaType = currentFile.mediaType,
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
            autoPlayVideos = true,
            onKeepCurrent = {},
            onTrashCurrent = { null },
            onUndoTrash = {},
            onToggleBackground = {},
            onBackToOnboarding = {},
            onNavigateToReview = {}
        )
    }
}
