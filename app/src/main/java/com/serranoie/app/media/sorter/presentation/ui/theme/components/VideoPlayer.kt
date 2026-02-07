package com.serranoie.app.media.sorter.presentation.ui.theme.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Video player component with play/pause controls using ExoPlayer
 *
 * @param uri The URI of the video to play
 * @param autoPlay Whether to automatically start playing when the video loads
 * @param seekToProgress External seek request (0f to 1f), set to trigger seeking
 * @param onProgressChanged Callback when video progress changes (0f to 1f)
 * @param onPlayingChanged Callback when playing state changes
 * @param sharedTransitionScope The SharedTransitionScope for shared element transitions
 * @param animatedVisibilityScope The AnimatedVisibilityScope for animations
 * @param isVisible Whether the video player is currently visible (used for transitions)
 * @param modifier Modifier for styling
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun VideoPlayer(
    uri: Uri?,
    autoPlay: Boolean,
    seekToProgress: Float? = null,
    onProgressChanged: (Float) -> Unit = {},
    onPlayingChanged: (Boolean) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(!autoPlay) }
    var progress by remember { mutableStateOf(0f) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = autoPlay
        }
    }

    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying) {
            val currentPosition = exoPlayer.currentPosition
            val duration = exoPlayer.duration
            if (duration > 0) {
                progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                onProgressChanged(progress)
            }
            delay(100) // Update every 100ms
        }
    }

    LaunchedEffect(seekToProgress) {
        seekToProgress?.let { targetProgress ->
            val duration = exoPlayer.duration
            if (duration > 0) {
                val targetPosition = (targetProgress * duration).toLong().coerceIn(0, duration)
                exoPlayer.seekTo(targetPosition)
                progress = targetProgress
                onProgressChanged(targetProgress)
            }
        }
    }

    LaunchedEffect(isPlaying, showControls) {
        if (isPlaying && showControls) {
            delay(3000)
            showControls = false
        }
    }

    LaunchedEffect(uri) {
        uri?.let {
            val mediaItem = MediaItem.fromUri(it)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            if (autoPlay) {
                exoPlayer.play()
                isPlaying = true
                onPlayingChanged(true)
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                onPlayingChanged(playing)
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0)
                    progress = 0f
                    onProgressChanged(0f)
                    showControls = true
                }
            }
        }
        exoPlayer.addListener(listener)
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        with(sharedTransitionScope) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isVisible) {
                            Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "media-content"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                        } else {
                            Modifier
                        }
                    )
            )
        }

        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
