package com.serranoie.app.media.sorter.presentation.sorter

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.serranoie.app.media.sorter.R
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    onKeep: () -> Unit,
    onTrash: () -> Unit,
    onKeepGestureProgress: (Float) -> Unit = {},
    onTrashGestureProgress: (Float) -> Unit = {},
    cardContent: @Composable () -> Unit
) {
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var lastVibeProgress by remember { mutableStateOf(0f) }

    val keepProgress by remember {
        derivedStateOf { if (offsetY.value > 0) (offsetY.value / 180f).coerceIn(0f, 1f) else 0f }
    }
    val trashProgress by remember {
        derivedStateOf { if (offsetY.value < 0) (offsetY.value / -180f).coerceIn(0f, 1f) else 0f }
    }

    LaunchedEffect(keepProgress) { onKeepGestureProgress(keepProgress) }
    LaunchedEffect(trashProgress) { onTrashGestureProgress(trashProgress) }

    val cardScale by remember {
        derivedStateOf {
            when {
                offsetY.value < 0 -> 1 - (abs(offsetY.value) / 1200f)
                offsetY.value > 0 -> 1f + (keepProgress * 0.05f)
                else -> 1f
            }
        }
    }

    val cardPaddingValue by remember {
        derivedStateOf {
            if (offsetY.value > 0) {
                8f * (1f - keepProgress * 0.8f)
            } else {
                8f
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            ),
            modifier = Modifier
                .padding(cardPaddingValue.dp)
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.roundToInt()) }
                .scale(cardScale)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            val newOffset = offsetY.value + dragAmount
                            scope.launch {
                                offsetY.snapTo(newOffset)
                            }

                            val currentTrashProgress = if (newOffset < 0) (newOffset / -180f).coerceIn(0f, 1f) else 0f
                            val currentKeepProgress = if (newOffset > 0) (newOffset / 180f).coerceIn(0f, 1f) else 0f

                            if (newOffset < 0 && currentTrashProgress - lastVibeProgress > 0.08f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastVibeProgress = currentTrashProgress
                            } else if (newOffset > 0 && currentKeepProgress - lastVibeProgress > 0.08f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastVibeProgress = currentKeepProgress
                            }
                        },
                        onDragEnd = {
                            when {
                                offsetY.value < -180 -> {
                                    scope.launch {
                                        offsetY.animateTo(-1200f, spring(stiffness = Spring.StiffnessMedium))
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onTrash()
                                        offsetY.snapTo(0f)
                                    }
                                }
                                offsetY.value > 180 -> {
                                    scope.launch {
                                        offsetY.animateTo(1200f, spring(stiffness = Spring.StiffnessMedium))
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onKeep()
                                        offsetY.snapTo(0f)
                                    }
                                }
                                else -> scope.launch {
                                    offsetY.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                }
                            }
                            onKeepGestureProgress(0f)
                            onTrashGestureProgress(0f)
                            lastVibeProgress = 0f
                        }
                    )
                }
        ) {
            cardContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun SwipeableCardPreview() {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        SwipeableCard(
            onKeep = {},
            onTrash = {},
            cardContent = {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Placeholder Image
                    Image(
                        painter = painterResource(id = R.drawable.demo_image),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Type Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "JPG",
                                style = MaterialTheme.typography.labelMedium,
                                color = colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom info overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Madeira Trip Photo.jpg",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "4.8 MB • 2024-05-23",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { },
                                modifier = Modifier.height(40.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = colorScheme.primary,
                                    contentColor = colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Open", fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { }) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color.White
                                    )
                                }
                                IconButton(onClick = { }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}
