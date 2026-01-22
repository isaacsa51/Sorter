package com.serranoie.app.media.sorter.presentation.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import com.serranoie.app.media.sorter.ui.theme.components.PinchToZoomGridContainer
import com.serranoie.app.media.sorter.ui.theme.components.GridZoomLevel
import com.serranoie.app.media.sorter.ui.theme.components.detectPinchGestures
import com.serranoie.app.media.sorter.ui.theme.util.DevicePreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ReviewScreen(
	deletedFiles: List<MediaFileUi>,
	onBack: () -> Unit = {},
	onSettings: () -> Unit = {},
	onInfo: () -> Unit = {},
	onRemoveItem: (MediaFileUi) -> Unit = {},
	onDeleteAll: () -> Unit = {}
) {
	var showDeleteConfirmDialog by remember { mutableStateOf(false) }
	
	// Setup delete media handler for Android 10+
	val (deleteHandler, _) = rememberDeleteMediaHandler(
		onPermissionGranted = {
			// Permission granted, proceed with deletion
			onDeleteAll()
		},
		onPermissionDenied = {
			// User denied permission
			// Could show a message here
		}
	)
	
	// Define zoom levels: 1 column, 2 columns, 3 columns, 4 columns
	val zoomLevels = remember {
		listOf(
			GridZoomLevel(level = 0, columns = 1, nextLevel = 1, previousLevel = 0),
			GridZoomLevel(level = 1, columns = 2, nextLevel = 2, previousLevel = 0),
			GridZoomLevel(level = 2, columns = 3, nextLevel = 3, previousLevel = 1),
			GridZoomLevel(level = 3, columns = 4, nextLevel = 3, previousLevel = 2)
		)
	}

	Scaffold(
		topBar = {
			ReviewTopBar(
				deletedCount = deletedFiles.size,
				onBack = onBack,
				onSettings = onSettings,
				onInfo = onInfo
			)
		},
		floatingActionButton = {
			if (deletedFiles.isNotEmpty()) {
				ExtendedFloatingActionButton(
					onClick = { showDeleteConfirmDialog = true },
					icon = {
						Icon(
							imageVector = Icons.Default.Delete,
							contentDescription = "Delete all"
						)
					},
					text = {
						Text(
							text = "Delete All (${deletedFiles.size})",
							style = MaterialTheme.typography.labelLarge,
							fontWeight = FontWeight.SemiBold
						)
					},
					containerColor = MaterialTheme.colorScheme.errorContainer,
					contentColor = MaterialTheme.colorScheme.error
				)
			}
		}
	) { paddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
		) {
			if (deletedFiles.isEmpty()) {
				EmptyState()
			} else {
				PinchToZoomGridContainer(
					modifier = Modifier.fillMaxSize(), initialLevel = 1, // Start with 2 columns
					zoomLevels = zoomLevels
				) { zoomLevel, onZoomLevelChange ->
					ZoomableStaggeredGrid(
						deletedFiles = deletedFiles,
						zoomLevel = zoomLevel,
						onZoomLevelChange = onZoomLevelChange,
						onRemoveItem = onRemoveItem
					)
				}

				ZoomHint(
					itemCount = deletedFiles.size, modifier = Modifier.align(Alignment.BottomCenter)
				)
			}
			
			if (showDeleteConfirmDialog) {
				DeleteAllConfirmationDialog(
					itemCount = deletedFiles.size,
					onConfirm = {
						showDeleteConfirmDialog = false
						// Request permission first on Android 10+
						val uris = deletedFiles.mapNotNull { it.uri }
						deleteHandler.requestDeletePermission(uris)
					},
					onDismiss = {
						showDeleteConfirmDialog = false
					}
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyState() {
	val colorScheme = MaterialTheme.colorScheme

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Icon(
			imageVector = Icons.Default.Delete,
			contentDescription = null,
			modifier = Modifier.size(80.dp),
			tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
		)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = "No deleted media",
			style = MaterialTheme.typography.labelMediumEmphasized,
			color = colorScheme.onSurfaceVariant,
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = "Files you delete will appear here for review",
			style = MaterialTheme.typography.bodyMedium,
			color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
			textAlign = TextAlign.Center
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ZoomableStaggeredGrid(
	deletedFiles: List<MediaFileUi>, 
	zoomLevel: GridZoomLevel, 
	onZoomLevelChange: (Int) -> Unit,
	onRemoveItem: (MediaFileUi) -> Unit
) {
	var zoom by remember { mutableStateOf(1f) }

	val motionScheme = MotionScheme.expressive()

	val zoomTransition: Float by animateFloatAsState(
		targetValue = zoom,
		animationSpec = motionScheme.defaultSpatialSpec(),
		label = "zoomTransition"
	)

	LazyVerticalStaggeredGrid(
		columns = StaggeredGridCells.Fixed(zoomLevel.columns),
		contentPadding = PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalItemSpacing = 8.dp,
		modifier = Modifier
			.fillMaxSize()
			.graphicsLayer {
				scaleX = zoomTransition
				scaleY = zoomTransition
			}
			.pointerInput(Unit) {
				detectPinchGestures(pass = PointerEventPass.Initial, onGesture = { _, zoomChange ->
					val newScale = zoom * zoomChange
					when {
						// Pinch out (zoom > 1.25) = zoom in = fewer columns = larger items
						newScale > 1.25f -> {
							onZoomLevelChange(zoomLevel.previousLevel)
							zoom = 1f
						}
						// Pinch in (zoom < 0.75) = zoom out = more columns = smaller items
						newScale < 0.75f -> {
							onZoomLevelChange(zoomLevel.nextLevel)
							zoom = 1f
						}

						else -> {
							zoom = newScale
						}
					}
				}, onGestureEnd = {
					zoom = 1f
				})
			}) {
		items(deletedFiles, key = { it.id }) { file ->
			SwipeableMediaGridItem(
				file = file, 
				columnCount = zoomLevel.columns,
				onRemove = { onRemoveItem(file) }
			)
		}
	}
}

@Composable
private fun ZoomHint(
	itemCount: Int, modifier: Modifier = Modifier
) {
	val colorScheme = MaterialTheme.colorScheme

	Surface(
		modifier = modifier.padding(16.dp),
		shape = RoundedCornerShape(20.dp),
		color = colorScheme.surfaceVariant.copy(alpha = 0.9f),
		tonalElevation = 4.dp
	) {
		Text(
			text = "Pinch to zoom • $itemCount item${if (itemCount != 1) "s" else ""}",
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			style = MaterialTheme.typography.labelMedium,
			color = colorScheme.onSurfaceVariant
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReviewTopBar(
	deletedCount: Int, onBack: () -> Unit, onSettings: () -> Unit, onInfo: () -> Unit
) {
	val colorScheme = MaterialTheme.colorScheme

	TopAppBar(
		title = {
		Column {
			Text(
				text = "Review Deleted",
				style = MaterialTheme.typography.titleLargeEmphasized,
			)
			if (deletedCount > 0) {
				Text(
					text = "$deletedCount item${if (deletedCount != 1) "s" else ""}",
					style = MaterialTheme.typography.bodySmall,
					color = colorScheme.onSurfaceVariant
				)
			}
		}
	}, navigationIcon = {
		IconButton(onClick = onBack) {
			Icon(
				imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
			)
		}
	}, actions = {
		IconButton(onClick = onSettings) {
			Icon(
				imageVector = Icons.Default.Settings, contentDescription = "Settings"
			)
		}
	}, colors = TopAppBarDefaults.topAppBarColors(
		containerColor = colorScheme.surface, titleContentColor = colorScheme.onSurface
	)
	)
}

@Composable
private fun SwipeableMediaGridItem(
	file: MediaFileUi,
	columnCount: Int,
	onRemove: () -> Unit
) {
	val offsetX = remember { Animatable(0f) }
	var isRemoving by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()
	val swipeThreshold = -200f
	
	AnimatedVisibility(
		visible = !isRemoving,
		exit = fadeOut(animationSpec = tween(300)) + 
			   shrinkVertically(animationSpec = tween(300))
	) {
		Box(
			modifier = Modifier.fillMaxWidth()
		) {
			Box(
				modifier = Modifier
					.matchParentSize()
					.background(
						MaterialTheme.colorScheme.errorContainer,
						RoundedCornerShape(12.dp)
					)
					.padding(16.dp),
				contentAlignment = Alignment.CenterEnd
			) {
				Row(
					horizontalArrangement = Arrangement.End,
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(
						imageVector = Icons.Default.Delete,
						contentDescription = "Delete",
						tint = MaterialTheme.colorScheme.error,
						modifier = Modifier.size(32.dp)
					)
				}
			}
			
			Box(
				modifier = Modifier
					.offset { IntOffset(offsetX.value.roundToInt(), 0) }
					.pointerInput(Unit) {
						detectHorizontalDragGestures(
							onDragEnd = {
								if (offsetX.value < swipeThreshold) {
									scope.launch {
										isRemoving = true
										delay(300)
										onRemove()
									}
								} else {
									scope.launch {
										offsetX.animateTo(
											targetValue = 0f,
											animationSpec = spring(
												dampingRatio = Spring.DampingRatioMediumBouncy,
												stiffness = Spring.StiffnessMedium
											)
										)
									}
								}
							},
							onDragCancel = {
								scope.launch {
									offsetX.animateTo(
										targetValue = 0f,
										animationSpec = spring(
											dampingRatio = Spring.DampingRatioMediumBouncy,
											stiffness = Spring.StiffnessMedium
										)
									)
								}
							},
							onHorizontalDrag = { _, dragAmount ->
								val newOffset = offsetX.value + dragAmount
								if (newOffset <= 0) {
									scope.launch {
										offsetX.snapTo(newOffset)
									}
								}
							}
						)
					}
			) {
				MediaGridItem(
					file = file,
					columnCount = columnCount
				)
			}
		}
	}
}

@Composable
private fun MediaGridItem(
	file: MediaFileUi, columnCount: Int
) {
	val colorScheme = MaterialTheme.colorScheme

	val heightMultiplier = when (file.id.hashCode() % 3) {
		0 -> 1.2f
		1 -> 1.0f
		else -> 1.4f
	}

	val baseHeight = when (columnCount) {
		1 -> 300.dp
		2 -> 180.dp
		3 -> 140.dp
		else -> 120.dp
	}

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(baseHeight * heightMultiplier),
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = if (file.mediaType == "video") {
				colorScheme.tertiaryContainer
			} else {
				colorScheme.primaryContainer
			}
		)
	) {
		Box(modifier = Modifier.fillMaxSize()) {
			if (file.uri != null) {
				val context = LocalContext.current

				val imageRequest = remember(file.uri) {
					ImageRequest.Builder(context).data(file.uri).crossfade(true).build()
				}

				AsyncImage(
					model = imageRequest,
					contentDescription = file.fileName,
					modifier = Modifier.fillMaxSize(),
					contentScale = ContentScale.Crop
				)
			} else {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(
							if (file.mediaType == "video") {
								colorScheme.tertiaryContainer
							} else {
								colorScheme.primaryContainer
							}
						), contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = if (file.mediaType == "video") {
							Icons.Default.VideoLibrary
						} else {
							Icons.Default.Image
						}, contentDescription = null, modifier = Modifier.size(
							when (columnCount) {
								1 -> 80.dp
								2 -> 60.dp
								3 -> 48.dp
								else -> 40.dp
							}
						), tint = if (file.mediaType == "video") {
							colorScheme.onTertiaryContainer.copy(alpha = 0.3f)
						} else {
							colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
						}
					)
				}
			}

			Surface(
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(8.dp),
				shape = CircleShape,
				color = colorScheme.surface.copy(alpha = 0.8f)
			) {
				Icon(
					imageVector = if (file.mediaType == "video") {
						Icons.Default.VideoLibrary
					} else {
						Icons.Default.Image
					},
					contentDescription = null,
					modifier = Modifier
						.padding(6.dp)
						.size(14.dp),
					tint = colorScheme.onSurface
				)
			}

			if (columnCount <= 2) {
				Box(
					modifier = Modifier
						.align(Alignment.BottomStart)
						.fillMaxWidth()
						.background(
							Color.Black.copy(alpha = 0.6f)
						)
						.padding(8.dp)
				) {
					Text(
						text = file.fileName,
						style = MaterialTheme.typography.labelSmall,
						color = Color.White,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						fontSize = if (columnCount == 1) 12.sp else 10.sp
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DeleteAllConfirmationDialog(
	itemCount: Int,
	onConfirm: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		icon = {
			Icon(
				imageVector = Icons.Default.Delete,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.error,
				modifier = Modifier.size(32.dp)
			)
		},
		title = {
			Text(
				text = "Delete All Files?",
				style = MaterialTheme.typography.titleLargeEmphasized,
				textAlign = TextAlign.Center
			)
		},
		text = {
			Text(
				text = "Are you sure you want to permanently delete all $itemCount file${if (itemCount != 1) "s" else ""}? This action cannot be undone.",
				style = MaterialTheme.typography.bodyLarge,
				textAlign = TextAlign.Center,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		},
		confirmButton = {
			Button(
				onClick = onConfirm,
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.error,
					contentColor = MaterialTheme.colorScheme.onError
				)
			) {
				Text(
					text = "Delete All",
					style = MaterialTheme.typography.labelLargeEmphasized,
					fontWeight = FontWeight.Bold
				)
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(
					text = "Cancel",
					style = MaterialTheme.typography.labelLargeEmphasized
				)
			}
		}
	)
}

@DevicePreview
@Composable
fun ReviewScreenPreview() {
	PreviewWrapper {
		ReviewScreen(
			deletedFiles = listOf(
			MediaFileUi(
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
			), MediaFileUi(
				id = "2",
				fileName = "Family_Dinner.mp4",
				fileInfo = "15.8 MB • 2 days ago",
				mediaType = "video",
				date = "Jan 5, 2025",
				fileSize = "15.8 MB",
				dimensions = "1920x1080",
				dateCreated = "2025-01-05 06:45 PM",
				lastAccessed = "2025-01-07 03:20 PM",
				modified = "2 days ago",
				path = "/videos/family/"
			), MediaFileUi(
				id = "3",
				fileName = "Mountain_Trip.jpg",
				fileInfo = "3.2 MB • 3 days ago",
				mediaType = "image",
				date = "Jan 4, 2025",
				fileSize = "3.2 MB",
				dimensions = "3840x2160",
				dateCreated = "2025-01-04 02:15 PM",
				lastAccessed = "2025-01-06 11:00 AM",
				modified = "3 days ago",
				path = "/photos/nature/mountains/"
			)
		), onBack = {}, onSettings = {}, onInfo = {}, onRemoveItem = {}, onDeleteAll = {})
	}
}
