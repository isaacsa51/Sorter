package com.serranoie.app.media.sorter.presentation.settings

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.serranoie.app.media.sorter.presentation.util.Utils
import com.serranoie.app.media.sorter.presentation.util.Utils.strongHapticFeedback
import com.serranoie.app.media.sorter.presentation.util.Utils.toggleFeedback
import com.serranoie.app.media.sorter.presentation.util.Utils.weakHapticFeedback
import com.serranoie.app.media.sorter.ui.theme.AureaSpacing
import com.serranoie.app.media.sorter.ui.theme.SorterTheme
import com.serranoie.app.media.sorter.ui.theme.components.CustomPaddedExpandableItem
import com.serranoie.app.media.sorter.ui.theme.components.CustomPaddedListItem
import com.serranoie.app.media.sorter.ui.theme.components.CustomSettingsItem
import com.serranoie.app.media.sorter.ui.theme.components.FlexibleListGroup
import com.serranoie.app.media.sorter.ui.theme.components.PaddedListGroup
import com.serranoie.app.media.sorter.ui.theme.components.PaddedListItemPosition
import com.serranoie.app.media.sorter.ui.theme.util.DevicePreview
import com.serranoie.app.media.sorter.ui.theme.util.PreviewWrapper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
	appTheme: String,
	isMaterialYouEnabled: Boolean,
	isBlurredBackgroundEnabled: Boolean,
	isAutoPlayEnabled: Boolean,
	syncFileToTrashBin: Boolean,
	onThemeChange: (String) -> Unit,
	onMaterialYouToggle: () -> Unit,
	onBlurredBackgroundToggle: () -> Unit,
	onAutoPlayToggle: () -> Unit,
	onSyncFileToTrashBinToggle: () -> Unit = {},
	onResetTutorial: () -> Unit = {},
	onBack: () -> Unit = {}
) {
	var showThemeDialog by remember { mutableStateOf(false) }
	var isStorageInfoExpanded by remember { mutableStateOf(false) }
	val scrollBehavior =
		TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
	val aureaSpacing = AureaSpacing.current
	val context = LocalContext.current
	val view = LocalView.current


	Scaffold(
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
			LargeTopAppBar(
				title = {
					Text(
						text = "Settings",
						style = MaterialTheme.typography.titleLargeEmphasized,
					)
				}, navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
						)
					}
				}, colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surface,
					titleContentColor = MaterialTheme.colorScheme.onSurface
				), scrollBehavior = scrollBehavior
			)
		}) { paddingValues ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues),
			contentPadding = PaddingValues(bottom = aureaSpacing.M)
		) {
			item {
				PaddedListGroup(
					title = "Appearance"
				) {
					CustomPaddedListItem(
						onClick = {
							showThemeDialog = true
							view.weakHapticFeedback()
						}, position = PaddedListItemPosition.First
					) {
						Icon(
							imageVector = Icons.Default.Brightness4,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "App Theme",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Choose your preferred theme",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Text(
							text = appTheme,
							style = MaterialTheme.typography.labelLarge,
							color = MaterialTheme.colorScheme.primary
						)
					}

					CustomPaddedListItem(
						onClick = onMaterialYouToggle, position = PaddedListItemPosition.Middle
					) {
						Icon(
							imageVector = Icons.Default.Palette,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Material You",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Dynamic colors from wallpaper",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Switch(
							checked = isMaterialYouEnabled, onCheckedChange = {
								onMaterialYouToggle()
								view.toggleFeedback()
							})
					}

					CustomPaddedListItem(
						onClick = {
							onBlurredBackgroundToggle()
							view.strongHapticFeedback()
						}, position = PaddedListItemPosition.Last
					) {
						Icon(
							imageVector = Icons.Default.Brightness4,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Background Mode",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Sorter screen background style",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Text(
							text = if (isBlurredBackgroundEnabled) "Blurred" else "Solid",
							style = MaterialTheme.typography.labelLarge,
							color = MaterialTheme.colorScheme.primary
						)
					}
				}
			}

			item {
				PaddedListGroup(
					title = "Playback"
				) {
					CustomPaddedListItem(
						onClick = onAutoPlayToggle, position = PaddedListItemPosition.Single
					) {
						Icon(
							imageVector = Icons.Default.PlayArrow,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Auto-Play Videos",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Automatically play videos when reviewing",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Switch(
							checked = isAutoPlayEnabled, onCheckedChange = {
								onAutoPlayToggle()
								view.toggleFeedback()
							})
					}
				}
			}

			item {
				PaddedListGroup(
					title = "Storage"
				) {
					CustomPaddedExpandableItem(
						isExpanded = isStorageInfoExpanded,
						onToggleExpanded = {
							isStorageInfoExpanded = !isStorageInfoExpanded
							view.weakHapticFeedback()
						},
						position = PaddedListItemPosition.Single,
						defaultContent = {
							Icon(
								imageVector = Icons.Rounded.Storage,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.primary
							)
							Spacer(modifier = Modifier.width(aureaSpacing.M))

							Column(modifier = Modifier.weight(1f)) {
								Text(
									text = "Sync media deletion",
									style = MaterialTheme.typography.bodyLarge,
									color = MaterialTheme.colorScheme.onSurface
								)
								Text(
									text = "Tap to learn more",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
							Switch(
								checked = syncFileToTrashBin, onCheckedChange = {
									onSyncFileToTrashBinToggle()
									view.toggleFeedback()
								}, modifier = Modifier.padding(end = aureaSpacing.XS)
							)
						},
						expandedContent = {
							HorizontalDivider(modifier = Modifier.padding(vertical = aureaSpacing.XS))

							Column(
								modifier = Modifier
									.fillMaxWidth()
									.padding(
										horizontal = aureaSpacing.XS, vertical = aureaSpacing.S
									)
							) {
								Row(
									modifier = Modifier.padding(bottom = aureaSpacing.S),
									verticalAlignment = Alignment.CenterVertically
								) {
									Icon(
										imageVector = Icons.Outlined.Info,
										contentDescription = "Information",
										tint = MaterialTheme.colorScheme.primary,
										modifier = Modifier.size(aureaSpacing.M)
									)
									Spacer(modifier = Modifier.width(aureaSpacing.XS))
									Text(
										text = "About this feature",
										style = MaterialTheme.typography.labelLarge,
										color = MaterialTheme.colorScheme.primary,
										fontWeight = FontWeight.SemiBold
									)
								}

								Text(
									text = "When enabled (Android 11+):",
									style = MaterialTheme.typography.bodyMedium,
									fontWeight = FontWeight.SemiBold,
									color = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier.padding(bottom = aureaSpacing.XS)
								)

								Text(
									text = "• Files move to your phone's Trash Bin\n• Automatically deleted after 30 days\n• Can be recovered before permanent deletion",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									modifier = Modifier.padding(bottom = aureaSpacing.M)
								)

								Text(
									text = "When disabled:",
									style = MaterialTheme.typography.bodyMedium,
									fontWeight = FontWeight.SemiBold,
									color = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier.padding(bottom = aureaSpacing.XS)
								)

								Text(
									text = "• Files are permanently deleted immediately\n• No recovery option available",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
						})
				}
			}

			item {
				PaddedListGroup(
					title = "App Information"
				) {
					CustomPaddedListItem(
						onClick = {
							Utils.openWebLink(context, "https://www.github.com/isaacsa51/Sorter")
							view.weakHapticFeedback()
						}, position = PaddedListItemPosition.First
					) {
						Icon(
							imageVector = Icons.Default.Info,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "About",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Learn more about Media Sorter",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					CustomPaddedListItem(
						onClick = {
							Utils.openWebLink(context, "https://www.github.com/isaacsa51/Sorter")
							view.weakHapticFeedback()
						}, position = PaddedListItemPosition.Middle
					) {
						Icon(
							imageVector = Icons.Default.Policy,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Privacy Policy",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Read our privacy policy",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					CustomPaddedListItem(
						onClick = {
							Utils.openWebLink(
								context, "https://www.github.com/isaacsa51/Sorter/issues/new"
							)
							view.weakHapticFeedback()
						}, position = PaddedListItemPosition.Middle
					) {
						Icon(
							imageVector = Icons.Default.BugReport,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Report a Bug",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Help us improve the app",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					CustomPaddedListItem(
						onClick = {
							view.weakHapticFeedback()
						}, position = PaddedListItemPosition.Last
					) {
						Icon(
							imageVector = Icons.Default.Info,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Version",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "1.0.0",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
			}

			item {
				PaddedListGroup(
					title = "Tutorial"
				) {
					CustomPaddedListItem(
						onClick = {
							onResetTutorial()
							view.toggleFeedback()
						}, position = PaddedListItemPosition.Single
					) {
						Icon(
							imageVector = Icons.Default.Refresh,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.M))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Reset Tutorial",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Show the tutorial again on next launch",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
			}
		}

		if (showThemeDialog) {
			ThemePickerDialog(
				currentTheme = appTheme,
				onThemeSelected = onThemeChange,
				onDismiss = { showThemeDialog = false })
		}
	}
}

@DevicePreview
@Composable
fun SettingsScreenPreview() {
	PreviewWrapper {
		SettingsScreen(
			appTheme = "System",
			isMaterialYouEnabled = true,
			isBlurredBackgroundEnabled = true,
			isAutoPlayEnabled = true,
			onThemeChange = {},
			onMaterialYouToggle = {},
			onBlurredBackgroundToggle = {},
			onAutoPlayToggle = {},
			onResetTutorial = {},
			onBack = {},
			syncFileToTrashBin = false,
			onSyncFileToTrashBinToggle = {})
	}
}
