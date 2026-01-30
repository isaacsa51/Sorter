package com.serranoie.app.media.sorter.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.presentation.util.Utils
import com.serranoie.app.media.sorter.presentation.util.Utils.strongHapticFeedback
import com.serranoie.app.media.sorter.presentation.util.Utils.toggleFeedback
import com.serranoie.app.media.sorter.presentation.util.Utils.weakHapticFeedback
import com.serranoie.app.media.sorter.ui.theme.AureaSpacing
import com.serranoie.app.media.sorter.ui.theme.components.CustomPaddedExpandableItem
import com.serranoie.app.media.sorter.ui.theme.components.CustomPaddedListItem
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
	onResetViewedHistory: () -> Unit = {},
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
						text = stringResource(R.string.settings_title),
						style = MaterialTheme.typography.titleLargeEmphasized,
					)
				}, navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(
							imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.content_desc_back)
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
			contentPadding = PaddingValues(bottom = aureaSpacing.m)
		) {
			item {
				PaddedListGroup(
					title = stringResource(R.string.settings_appearance_title)
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
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_theme_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_theme_description),
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
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_material_you_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_material_you_description),
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
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_bg_mode_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_bg_mode_description),
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Text(
							text = if (isBlurredBackgroundEnabled) stringResource(R.string.settings_bg_mode_blurred) else stringResource(R.string.settings_bg_mode_solid),
							style = MaterialTheme.typography.labelLarge,
							color = MaterialTheme.colorScheme.primary
						)
					}
				}
			}

			item {
				PaddedListGroup(
					title = stringResource(R.string.settings_playback_title)
				) {
					CustomPaddedListItem(
						onClick = onAutoPlayToggle, position = PaddedListItemPosition.Single
					) {
						Icon(
							imageVector = Icons.Default.PlayArrow,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_autoplay_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_autoplay_description),
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
					title = stringResource(R.string.settings_storage_title)
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
							Spacer(modifier = Modifier.width(aureaSpacing.m))

							Column(modifier = Modifier.weight(1f)) {
								Text(
									text = stringResource(R.string.settings_sync_deletion_title),
									style = MaterialTheme.typography.bodyLarge,
									color = MaterialTheme.colorScheme.onSurface
								)
								Text(
									text = stringResource(R.string.settings_sync_deletion_description),
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
							Switch(
								checked = syncFileToTrashBin, onCheckedChange = {
									onSyncFileToTrashBinToggle()
									view.toggleFeedback()
								}, modifier = Modifier.padding(end = aureaSpacing.xs)
							)
						},
						expandedContent = {
							HorizontalDivider(modifier = Modifier.padding(vertical = aureaSpacing.xs))

							Column(
								modifier = Modifier
									.fillMaxWidth()
									.padding(
										horizontal = aureaSpacing.xs, vertical = aureaSpacing.s
									)
							) {
								Row(
									modifier = Modifier.padding(bottom = aureaSpacing.s),
									verticalAlignment = Alignment.CenterVertically
								) {
									Icon(
										imageVector = Icons.Outlined.Info,
										contentDescription = stringResource(R.string.content_desc_info),
										tint = MaterialTheme.colorScheme.primary,
										modifier = Modifier.size(aureaSpacing.m)
									)
									Spacer(modifier = Modifier.width(aureaSpacing.xs))
									Text(
										text = stringResource(R.string.settings_sync_deletion_about),
										style = MaterialTheme.typography.labelLarge,
										color = MaterialTheme.colorScheme.primary,
										fontWeight = FontWeight.SemiBold
									)
								}

								Text(
									text = stringResource(R.string.settings_sync_deletion_enabled_label),
									style = MaterialTheme.typography.bodyMedium,
									fontWeight = FontWeight.SemiBold,
									color = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier.padding(bottom = aureaSpacing.xs)
								)

								Text(
									text = stringResource(R.string.settings_sync_deletion_enabled_desc),
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									modifier = Modifier.padding(bottom = aureaSpacing.m)
								)

								Text(
									text = stringResource(R.string.settings_sync_deletion_disabled_label),
									style = MaterialTheme.typography.bodyMedium,
									fontWeight = FontWeight.SemiBold,
									color = MaterialTheme.colorScheme.onSurface,
									modifier = Modifier.padding(bottom = aureaSpacing.xs)
								)

								Text(
									text = stringResource(R.string.settings_sync_deletion_disabled_desc),
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
						})
				}
			}

			item {
				PaddedListGroup(
					title = stringResource(R.string.settings_app_info_title)
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
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_about_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_about_description),
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
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_privacy_policy_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_privacy_policy_description),
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
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_report_bug_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_report_bug_description),
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
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_version_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_version_value),
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
			}

			item {
				PaddedListGroup(
					title = stringResource(R.string.settings_tutorial_title)
				) {
					CustomPaddedListItem(
						onClick = {
							onResetTutorial()
							view.toggleFeedback()
						}, position = PaddedListItemPosition.First
					) {
						Icon(
							imageVector = Icons.Default.Refresh,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = stringResource(R.string.settings_reset_tutorial_title),
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = stringResource(R.string.settings_reset_tutorial_description),
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
					
					CustomPaddedListItem(
						onClick = {
							onResetViewedHistory()
							view.strongHapticFeedback()
						}, position = PaddedListItemPosition.Last
					) {
						Icon(
							imageVector = Icons.Default.Refresh,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.primary
						)
						Spacer(modifier = Modifier.width(aureaSpacing.m))
						Column(modifier = Modifier.weight(1f)) {
							Text(
								text = "Reset Viewed History",
								style = MaterialTheme.typography.bodyLarge,
								color = MaterialTheme.colorScheme.onSurface
							)
							Text(
								text = "Clear all viewed media to see all dates again",
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
			onResetViewedHistory = {},
			onBack = {},
			syncFileToTrashBin = false,
			onSyncFileToTrashBinToggle = {})
	}
}
