package com.serranoie.app.media.sorter.presentation.review

import androidx.annotation.PluralsRes
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import com.serranoie.app.media.sorter.presentation.ui.theme.AureaSpacing
import com.serranoie.app.media.sorter.presentation.ui.theme.components.GridZoomLevel
import com.serranoie.app.media.sorter.presentation.ui.theme.components.PinchToZoomGridContainer
import com.serranoie.app.media.sorter.presentation.ui.theme.components.detectPinchGestures
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReviewScreen(
	deletedFiles: List<MediaFileUi>,
	repository: MediaRepository,
	useTrash: Boolean,
	onBack: () -> Unit = {},
	onSettings: () -> Unit = {},
	onRemoveItem: (MediaFileUi) -> Unit = {},
	onDeleteAll: () -> Unit = {}
) {
	var showDeleteConfirmDialog by remember { mutableStateOf(false) }
	var selectedMediaForFullscreen by remember { mutableStateOf<MediaFileUi?>(null) }
	val scrollBehavior =
		TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
	val gridState = rememberLazyStaggeredGridState()
	val coroutineScope = rememberCoroutineScope()
	val (deleteHandler, _) = rememberDeleteMediaHandler(
		repository = repository,
		onPermissionGranted = {
			onDeleteAll()
		},
		onPermissionDenied = {
			// TODO: Check what to do when user denied permission
		})

	val expandedFab by remember {
		derivedStateOf { gridState.firstVisibleItemIndex == 0 }
	}

	val zoomLevels = remember {
		listOf(
			GridZoomLevel(level = 0, columns = 1, nextLevel = 1, previousLevel = 0),
			GridZoomLevel(level = 1, columns = 2, nextLevel = 2, previousLevel = 0),
			GridZoomLevel(level = 2, columns = 3, nextLevel = 3, previousLevel = 1),
			GridZoomLevel(level = 3, columns = 4, nextLevel = 3, previousLevel = 2)
		)
	}

	Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
		ReviewTopBar(
			deletedCount = deletedFiles.size,
			onBack = onBack,
			onSettings = onSettings,
			scrollBehavior = scrollBehavior
		)
	}, floatingActionButton = {
		if (deletedFiles.isNotEmpty()) {
			ExtendedFloatingActionButton(
				onClick = { showDeleteConfirmDialog = true },
				expanded = expandedFab,
				modifier = Modifier.testTag("ReviewDeleteAllFab"),
				icon = {
					Icon(
						imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.review_btn_delete_all)
					)
				},
				text = {
					Text(
						text = stringResource(R.string.review_btn_delete_all),
						style = MaterialTheme.typography.labelLargeEmphasized,
					)
				},
				containerColor = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.error
			)
		}
	}) { paddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.testTag("ReviewScreen")
		) {
			if (deletedFiles.isEmpty()) {
				EmptyState(modifier = Modifier.testTag("ReviewEmptyState"))
			} else {
				PinchToZoomGridContainer(
					modifier = Modifier.fillMaxSize(), initialLevel = 1, zoomLevels = zoomLevels
				) { zoomLevel, onZoomLevelChange ->
					ZoomableStaggeredGrid(
						deletedFiles = deletedFiles,
						zoomLevel = zoomLevel,
						onZoomLevelChange = onZoomLevelChange,
						onRemoveItem = onRemoveItem,
						onItemDoubleTap = { file ->
							selectedMediaForFullscreen = file
						},
						gridState = gridState
					)
				}
			}

			if (showDeleteConfirmDialog) {
				DeleteAllConfirmationDialog(itemCount = deletedFiles.size, onConfirm = {
					showDeleteConfirmDialog = false
					val uris = deletedFiles.mapNotNull { it.uri }
					coroutineScope.launch {
						deleteHandler.requestDeletePermission(uris, useTrash)
					}
				}, onDismiss = {
					showDeleteConfirmDialog = false
				}, modifier = Modifier.testTag("ReviewDeleteAllDialog"))
			}

			selectedMediaForFullscreen?.let { media ->
				FullscreenMediaViewer(
					media = media,
					onDismiss = { selectedMediaForFullscreen = null },
					modifier = Modifier.testTag("ReviewFullscreenViewer")
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
	val colorScheme = MaterialTheme.colorScheme
	val spacing = AureaSpacing.current

	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(spacing.xl),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Icon(
			imageVector = Icons.Default.Delete,
			contentDescription = null,
			modifier = Modifier.size(80.dp),
			tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
		)
		Spacer(modifier = Modifier.height(spacing.m))
		Text(
			text = stringResource(R.string.review_empty_title),
			style = MaterialTheme.typography.titleLargeEmphasized,
			color = colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center
		)
		Spacer(modifier = Modifier.height(spacing.xs))
		Text(
			text = stringResource(R.string.review_empty_description),
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
	onRemoveItem: (MediaFileUi) -> Unit,
	onItemDoubleTap: (MediaFileUi) -> Unit = {},
	gridState: LazyStaggeredGridState
) {
	var zoom by remember { mutableStateOf(1f) }

	val motionScheme = MotionScheme.expressive()

	val zoomTransition: Float by animateFloatAsState(
		targetValue = zoom,
		animationSpec = motionScheme.defaultSpatialSpec(),
		label = "zoomTransition"
	)

	val spacing = AureaSpacing.current

	LazyVerticalStaggeredGrid(
		state = gridState,
		columns = StaggeredGridCells.Fixed(zoomLevel.columns),
		contentPadding = PaddingValues(spacing.m),
		horizontalArrangement = Arrangement.spacedBy(spacing.xs),
		verticalItemSpacing = spacing.xs,
		modifier = Modifier
			.fillMaxSize()
			.testTag("ReviewGrid")
			.graphicsLayer {
				scaleX = zoomTransition
				scaleY = zoomTransition
			}
			.pointerInput(Unit) {
				detectPinchGestures(pass = PointerEventPass.Initial, onGesture = { _, zoomChange ->
					val newScale = zoom * zoomChange
					when {
						newScale > 1.25f -> {
							onZoomLevelChange(zoomLevel.previousLevel)
							zoom = 1f
						}

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
				onRemove = { onRemoveItem(file) },
				onDoubleTap = { onItemDoubleTap(file) })
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReviewTopBar(
	deletedCount: Int,
	onBack: () -> Unit,
	onSettings: () -> Unit,
	scrollBehavior: TopAppBarScrollBehavior
) {
	MediumTopAppBar(
		title = {
		Column {
			Text(
				text = stringResource(R.string.review_title),
				style = MaterialTheme.typography.titleLargeEmphasized,
			)
			if (deletedCount > 0) {
			Text(
				text = pluralStringResource(
					id = R.plurals.review_items_count,
					count = deletedCount,
					deletedCount
				),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		}
	}, navigationIcon = {
		IconButton(onClick = onBack) {
			Icon(
				imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back)
			)
		}
	}, actions = {
		IconButton(onClick = onSettings) {
			Icon(
				imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.content_desc_settings)
			)
		}
	}, colors = TopAppBarDefaults.topAppBarColors(
		containerColor = MaterialTheme.colorScheme.surface,
		titleContentColor = MaterialTheme.colorScheme.onSurface
	), scrollBehavior = scrollBehavior
	)
}

@Composable
private fun SwipeableMediaGridItem(
	file: MediaFileUi, columnCount: Int, onRemove: () -> Unit, onDoubleTap: () -> Unit = {}
) {
	val offsetX = remember { Animatable(0f) }
	var isRemoving by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()
	val spacing = AureaSpacing.current
	val swipeThreshold = -200f

	AnimatedVisibility(
		visible = !isRemoving,
		exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
	) {
		Box(
			modifier = Modifier.fillMaxWidth()
		) {
			Box(
				modifier = Modifier
					.matchParentSize()
					.background(
						MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(spacing.s)
					)
					.padding(spacing.m), contentAlignment = Alignment.CenterEnd
			) {
				Row(
					horizontalArrangement = Arrangement.End,
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(
						imageVector = Icons.Default.Delete,
						contentDescription = stringResource(R.string.content_desc_delete),
						tint = MaterialTheme.colorScheme.error,
						modifier = Modifier.size(32.dp)
					)
				}
			}

			Box(modifier = Modifier
				.offset { IntOffset(offsetX.value.roundToInt(), 0) }
				.pointerInput(Unit) {
					detectHorizontalDragGestures(onDragEnd = {
						if (offsetX.value < swipeThreshold) {
							scope.launch {
								isRemoving = true
								delay(300)
								onRemove()
							}
						} else {
							scope.launch {
								offsetX.animateTo(
									targetValue = 0f, animationSpec = spring(
										dampingRatio = Spring.DampingRatioMediumBouncy,
										stiffness = Spring.StiffnessMedium
									)
								)
							}
						}
					}, onDragCancel = {
						scope.launch {
							offsetX.animateTo(
								targetValue = 0f, animationSpec = spring(
									dampingRatio = Spring.DampingRatioMediumBouncy,
									stiffness = Spring.StiffnessMedium
								)
							)
						}
					}, onHorizontalDrag = { _, dragAmount ->
						val newOffset = offsetX.value + dragAmount
						if (newOffset <= 0) {
							scope.launch {
								offsetX.snapTo(newOffset)
							}
						}
					})
				}) {
				MediaGridItem(
					file = file, columnCount = columnCount, onDoubleTap = onDoubleTap
				)
			}
		}
	}
}

@Composable
private fun MediaGridItem(
	file: MediaFileUi, columnCount: Int, onDoubleTap: () -> Unit = {}
) {
	val colorScheme = MaterialTheme.colorScheme
	val spacing = AureaSpacing.current

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
			.testTag("ReviewGridItem_${file.id}")
			.fillMaxWidth()
			.height(baseHeight * heightMultiplier)
			.pointerInput(Unit) {
				detectTapGestures(
					onDoubleTap = { onDoubleTap() })
			}, shape = RoundedCornerShape(spacing.s), colors = CardDefaults.cardColors(
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

				val imageRequest = remember(file.uri, file.mediaType) {
					ImageRequest.Builder(context).data(file.uri).crossfade(true).apply {
						// For videos, extract a frame as thumbnail
						if (file.mediaType == "video") {
							decoderFactory(VideoFrameDecoder.Factory())
						}
					}.build()
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
					.padding(spacing.xs),
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
						.padding(spacing.xs)
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
						.padding(spacing.xs)
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
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier
) {
	AlertDialog(modifier = modifier, onDismissRequest = onDismiss, icon = {
		Icon(
			imageVector = Icons.Default.Delete,
			contentDescription = null,
			tint = MaterialTheme.colorScheme.error,
			modifier = Modifier.size(32.dp)
		)
	}, title = {
		Text(
			text = stringResource(R.string.review_dialog_delete_all_title),
			style = MaterialTheme.typography.titleLargeEmphasized,
			textAlign = TextAlign.Center
		)
	}, text = {
		Text(
			text = pluralStringResource(
				id = R.plurals.review_dialog_delete_all_text,
				count = itemCount,
				itemCount
			),
			style = MaterialTheme.typography.bodyLarge,
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}, confirmButton = {
		Button(
			onClick = onConfirm,
			modifier = Modifier.testTag("ReviewDeleteAllConfirm"),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.error,
				contentColor = MaterialTheme.colorScheme.onError
			)
		) {
			Text(
				text = stringResource(R.string.review_btn_delete_all),
				style = MaterialTheme.typography.labelLargeEmphasized,
				fontWeight = FontWeight.Bold
			)
		}
	}, dismissButton = {
		TextButton(onClick = onDismiss, modifier = Modifier.testTag("ReviewDeleteAllCancel")) {
			Text(
				text = stringResource(R.string.review_btn_cancel), style = MaterialTheme.typography.labelLargeEmphasized
			)
		}
	})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenMediaViewer(
	media: MediaFileUi,
	onDismiss: () -> Unit,
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val spacing = AureaSpacing.current

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Color.Black)
	) {
		if (media.uri != null) {
			val imageRequest = remember(media.uri, media.mediaType) {
				ImageRequest.Builder(context).data(media.uri).crossfade(true).apply {
					if (media.mediaType == "video") {
						decoderFactory(VideoFrameDecoder.Factory())
					}
				}.build()
			}

			AsyncImage(
				model = imageRequest,
				contentDescription = media.fileName,
				modifier = Modifier
					.fillMaxSize()
					.pointerInput(Unit) {
						detectTapGestures(
							onTap = { onDismiss() })
					},
				contentScale = ContentScale.Fit
			)
		}

		// Close button
		Surface(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.padding(spacing.m),
			shape = CircleShape,
			color = Color.Black.copy(alpha = 0.5f)
		) {
			IconButton(onClick = onDismiss, modifier = Modifier.testTag("ReviewFullscreenClose")) {
				Icon(
					imageVector = Icons.Default.Close,
					contentDescription = stringResource(R.string.content_desc_close),
					tint = Color.White
				)
			}
		}

		// Media info overlay
		Surface(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.fillMaxWidth(),
			color = Color.Black.copy(alpha = 0.7f)
		) {
			Column(
				modifier = Modifier.padding(spacing.m)
			) {
				Text(
					text = media.fileName,
					style = MaterialTheme.typography.titleMedium,
					color = Color.White,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.height(spacing.xs))
				Text(
					text = media.fileInfo,
					style = MaterialTheme.typography.bodySmall,
					color = Color.White.copy(alpha = 0.8f)
				)
			}
		}
	}
}
