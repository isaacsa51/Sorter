package com.serranoie.app.media.sorter.presentation.tutorial

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.presentation.sorter.SwipeableCard
import com.serranoie.app.media.sorter.ui.theme.components.FileInfo
import com.serranoie.app.media.sorter.ui.theme.components.GestureIndicator
import com.serranoie.app.media.sorter.ui.theme.components.MediaInfoOverlay
import com.serranoie.app.media.sorter.ui.theme.util.DevicePreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper
import com.serranoie.app.media.sorter.ui.theme.AureaSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TutorialScreen(
	onGetStarted: () -> Unit = {}
) {
	val colorScheme = MaterialTheme.colorScheme
	val spacing = AureaSpacing.current

	var keepProgress by remember { mutableFloatStateOf(0f) }
	var trashProgress by remember { mutableFloatStateOf(0f) }

	val keepIconAlpha =
		animateFloatAsState(keepProgress, animationSpec = tween(240), label = "keepAlpha").value
	val keepIconScale = animateFloatAsState(
		0.7f + 0.5f * keepProgress, animationSpec = tween(240), label = "keepScale"
	).value
	val keepIconOffset = animateFloatAsState(
		(-32f + 60f * keepProgress), animationSpec = tween(240), label = "keepOffset"
	).value

	val trashIconAlpha =
		animateFloatAsState(trashProgress, animationSpec = tween(240), label = "trashAlpha").value
	val trashIconScale = animateFloatAsState(
		0.7f + 0.5f * trashProgress, animationSpec = tween(240), label = "trashScale"
	).value
	val trashIconOffset = animateFloatAsState(
		(32f - 60f * trashProgress), animationSpec = tween(240), label = "trashOffset"
	).value

	val infiniteTransition = rememberInfiniteTransition(label = "bounce")
	val bounceOffset by infiniteTransition.animateFloat(
		initialValue = 0f, targetValue = 24f, animationSpec = infiniteRepeatable(
			animation = keyframes {
				durationMillis = 1400
				0f at 0 with FastOutSlowInEasing
				24f at 600 with LinearOutSlowInEasing
				0f at 1400 with FastOutSlowInEasing
			}, repeatMode = RepeatMode.Restart
		), label = "bounceOffset"
	)

	val mockFileInfo = FileInfo(
		fileName = "IMG_Portrait-152124.jpg",
		fileInfo = "2.4 MB • Yesterday",
		fileSize = "2.4 MB",
		dateCreated = "Yesterday, 10:30 PM",
		modified = "Yesterday",
		dimensions = "1920x1080",
		lastAccessed = "Today, 9:15 AM",
		path = "/Pictures/"
	)
	var isInfoExpanded by remember { mutableStateOf(false) }

	val descriptionText = buildAnnotatedString {
		append("Clean up your gallery in seconds. ")
		withStyle(
			style = SpanStyle(
				color = colorScheme.error,
				fontStyle = MaterialTheme.typography.bodyLargeEmphasized.fontStyle
			)
		) {
			append("Up")
		}
		append(" removes, ")
		withStyle(
			style = SpanStyle(
				color = colorScheme.primary,
				fontStyle = MaterialTheme.typography.bodyLargeEmphasized.fontStyle
			)
		) {
			append("Down")
		}
		append(" saves.")
	}

	Scaffold(
		modifier = Modifier.fillMaxSize(),
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.background(colorScheme.surface)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(horizontal = spacing.M),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Spacer(modifier = Modifier.height(spacing.M))

				Text(
					text = "Swipe to\nOrganize",
					style = MaterialTheme.typography.displaySmallEmphasized.copy(fontWeight = FontWeight.Bold),
					color = colorScheme.onSurface,
					textAlign = TextAlign.Center,
				)

				Spacer(modifier = Modifier.height(spacing.M))

				Text(
					text = descriptionText,
					style = MaterialTheme.typography.bodyLarge,
					color = colorScheme.onSurfaceVariant,
					textAlign = TextAlign.Center,
				)

				Spacer(modifier = Modifier.height(spacing.S))

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(0.75f)
				) {
					// Top gesture indicator (trash/delete)
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.align(Alignment.TopCenter)
							.padding(top = trashIconOffset.coerceAtLeast(0f).dp + spacing.S)
							.zIndex(3f), contentAlignment = Alignment.TopCenter
					) {
						GestureIndicator(
							visible = trashIconAlpha > 0.01f,
							icon = Icons.Filled.Delete,
							text = "Delete",
							containerColor = colorScheme.errorContainer,
							contentColor = colorScheme.error,
							alpha = trashIconAlpha,
							scale = trashIconScale
						)
					}

					SwipeableCard(
						modifier = Modifier
							.fillMaxSize()
							.graphicsLayer {
								translationY = bounceOffset
							},
						onKeep = {},
						onTrash = {},
						onKeepGestureProgress = { progress -> keepProgress = progress },
						onTrashGestureProgress = { progress -> trashProgress = progress },
						cardContent = {
							val isPreview = LocalInspectionMode.current

							Box(
								modifier = Modifier.fillMaxSize()
							) {
								// Background image or placeholder
								if (isPreview) {
									// [INFO] This box is for preview only
									Box(
										modifier = Modifier
											.fillMaxSize()
											.background(
												Brush.verticalGradient(
													colors = listOf(
														Color(0xFF2E3440),
														Color(0xFF3B4252),
														Color(0xFF434C5E)
													)
												)
											)
									)
								} else {
									Image(
										painter = painterResource(id = R.drawable.img_mock_protrait_tutorial),
										contentDescription = "Demo media",
										modifier = Modifier.fillMaxSize(),
										contentScale = ContentScale.Crop
									)
								}

								MediaInfoOverlay(
									fileInfo = mockFileInfo,
									isExpanded = isInfoExpanded,
									onExpandToggle = { isInfoExpanded = !isInfoExpanded },
									onOpenClick = { /* Tutorial - no action */ },
									onShareClick = { /* Tutorial - no action */ },
									modifier = Modifier.align(Alignment.BottomStart)
								)
							}
						})

					// Bottom gesture indicator (keep/save)
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.align(Alignment.BottomCenter)
							.padding(bottom = keepIconOffset.coerceAtLeast(0f).dp + spacing.S)
							.zIndex(3f), contentAlignment = Alignment.BottomCenter
					) {
						GestureIndicator(
							visible = keepIconAlpha > 0.01f,
							icon = Icons.Filled.CheckCircle,
							text = "Save",
							containerColor = Color(0xFF4CAF50),
							contentColor = Color.White,
							alpha = keepIconAlpha,
							scale = keepIconScale
						)
					}
				}

				Spacer(modifier = Modifier.height(spacing.M))

				Button(
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = colorScheme.primary
					),
					shape = RoundedCornerShape(28.dp),
					onClick = onGetStarted,
				) {
					Text(
						text = "Start Organizing",
						style = MaterialTheme.typography.titleMediumEmphasized,
						color = colorScheme.onPrimary,
					)
				}
			}
		}
	}
}


@DevicePreview
@Composable
fun TutorialScreenPreview() {
	PreviewWrapper {
		TutorialScreen(
			onGetStarted = {})
	}
}
