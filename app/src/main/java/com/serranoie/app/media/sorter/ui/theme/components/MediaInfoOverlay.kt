package com.serranoie.app.media.sorter.ui.theme.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.ui.theme.util.ComponentPreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper
import com.serranoie.app.media.sorter.ui.theme.AureaSpacing

/**
 * Overlay component that displays file information and action buttons at the bottom
 * 
 * @param fileInfo The file information to display
 * @param isExpanded Whether the detailed info should be expanded
 * @param onExpandToggle Callback when the expand/collapse action is triggered
 * @param onOpenClick Callback when the open button is clicked
 * @param onShareClick Callback when the share button is clicked
 * @param videoSlider Optional composable for video slider (shown above text for videos)
 * @param modifier Modifier to be applied to the overlay
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MediaInfoOverlay(
    fileInfo: FileInfo,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit,
    videoSlider: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val spacing = AureaSpacing.current
    val cornerRadius = spacing.s
    
    SharedTransitionLayout(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.90f)
                        )
                    )
                )
                .padding(spacing.l),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (videoSlider != null && !isExpanded) {
                videoSlider()
                Spacer(modifier = Modifier.height(spacing.s))
            }
            
            Text(
                text = fileInfo.fileName,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().basicMarquee()
            )
            Spacer(modifier = Modifier.height(spacing.xs))

            AnimatedVisibility(
                visible = !isExpanded,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Text(
                    text = fileInfo.fileInfo,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onExpandToggle() }
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeOut(animationSpec = tween(200))
            ) {
                ExpandedInfoContent(
                    fileInfo = fileInfo,
                    onExpandToggle = onExpandToggle,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    spacing = spacing,
                    cornerRadius = cornerRadius
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            ActionButtons(
                isExpanded = isExpanded,
                onOpenClick = onOpenClick,
                onShareClick = onShareClick,
                onExpandToggle = onExpandToggle,
            )
        }
    }
}

/**
 * Expanded info content with shared element transitions
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ExpandedInfoContent(
    fileInfo: FileInfo,
    onExpandToggle: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    spacing: com.serranoie.app.media.sorter.ui.theme.PhiSpacing,
    cornerRadius: androidx.compose.ui.unit.Dp
) {
    with(sharedTransitionScope) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "info-container"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(200)),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                )
                .clickable { onExpandToggle() },
            shape = RoundedCornerShape(cornerRadius),
            color = Color.White.copy(alpha = 0.12f),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(spacing.m)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoItem(stringResource(R.string.media_info_label_file_size), fileInfo.fileSize)
                        Spacer(modifier = Modifier.height(spacing.m))
                        InfoItem(stringResource(R.string.media_info_label_date_created), fileInfo.dateCreated)
                        Spacer(modifier = Modifier.height(spacing.m))
                        InfoItem(stringResource(R.string.media_info_label_modified), fileInfo.modified)
                    }

                    Spacer(modifier = Modifier.width(spacing.m))

                    Column(modifier = Modifier.weight(1f)) {
                        InfoItem(stringResource(R.string.media_info_label_dimensions), fileInfo.dimensions)
                        Spacer(modifier = Modifier.height(spacing.m))
                        InfoItem(stringResource(R.string.media_info_label_last_accessed), fileInfo.lastAccessed)
                        Spacer(modifier = Modifier.height(spacing.m))
                        InfoItem(stringResource(R.string.media_info_label_path), fileInfo.path)
                    }
                }
            }
        }
    }
}

/**
 * Action buttons with shared element transitions for the info button
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SharedTransitionScope.ActionButtons(
    isExpanded: Boolean,
    onOpenClick: () -> Unit,
    onShareClick: () -> Unit,
    onExpandToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onOpenClick,
            modifier = Modifier.height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.media_info_btn_open),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.media_info_btn_open),
                style = MaterialTheme.typography.labelLargeEmphasized,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledIconButton(
                onClick = onShareClick,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.content_desc_share),
                    modifier = Modifier.size(20.dp)
                )
            }
            FilledIconButton(
                onClick = onExpandToggle,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isExpanded) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.White.copy(alpha = 0.15f)
                    },
                    contentColor = if (isExpanded) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        Color.White
                    }
                )
            ) {
                Icon(
                    imageVector = if (isExpanded) {
                        Icons.Default.Info
                    } else {
                        Icons.Outlined.Info
                    },
                    contentDescription = stringResource(R.string.content_desc_info),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Component for displaying a single info item with label and value
 */
@Composable
private fun InfoItem(
    label: String, 
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.65f),
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.95f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal
        )
    }
}

@ComponentPreview
@Composable
private fun MediaInfoOverlayCollapsedPreview() {
    PreviewWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F))
        ) {
            MediaInfoOverlay(
                fileInfo = FileInfo(
                    fileName = "Summer_Vacation_2024.jpg",
                    fileInfo = "4.8 MB • 2024-05-23",
                    fileSize = "4.8 MB",
                    dateCreated = "2024-05-23 14:30",
                    modified = "2024-05-24 09:15",
                    dimensions = "4032 × 3024",
                    lastAccessed = "2024-06-01 18:45",
                    path = "/storage/emulated/0/DCIM/Camera/IMG_20240523_143012.jpg"
                ),
                isExpanded = false,
                onExpandToggle = {},
                onOpenClick = {},
                onShareClick = {},
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MediaInfoOverlayExpandedPreview() {
    PreviewWrapper {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1B1F))
        ) {
            MediaInfoOverlay(
                fileInfo = FileInfo(
                    fileName = "Summer_Vacation_2024.jpg",
                    fileInfo = "4.8 MB • 2024-05-23",
                    fileSize = "4.8 MB",
                    dateCreated = "2024-05-23 14:30",
                    modified = "2024-05-24 09:15",
                    dimensions = "4032 × 3024",
                    lastAccessed = "2024-06-01 18:45",
                    path = "/storage/emulated/0/DCIM/Camera/"
                ),
                isExpanded = true,
                onExpandToggle = {},
                onOpenClick = {},
                onShareClick = {},
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}
