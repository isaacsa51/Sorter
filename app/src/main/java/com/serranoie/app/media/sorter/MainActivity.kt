package com.serranoie.app.media.sorter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.presentation.navigation.AppNavHost
import com.serranoie.app.media.sorter.presentation.navigation.NavigationViewModel
import com.serranoie.app.media.sorter.presentation.navigation.PermissionHandler
import com.serranoie.app.media.sorter.presentation.settings.SettingsViewModel
import com.serranoie.app.media.sorter.presentation.sorter.SorterViewModel
import com.serranoie.app.media.sorter.presentation.update.UpdateViewModel
import com.serranoie.app.media.sorter.presentation.ui.components.UpdateDialog
import com.serranoie.app.media.sorter.presentation.ui.theme.SorterTheme
import com.serranoie.app.media.sorter.work.UpdateCheckWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			SorterApp(
				intent = intent
			)
		}
	}
}

@Composable
fun SorterApp(intent: Intent? = null) {
	val settingsViewModel: SettingsViewModel = hiltViewModel()
	val updateViewModel: UpdateViewModel = hiltViewModel()
	val appSettings by settingsViewModel.appSettings.collectAsState()
	val updateCheckResult by updateViewModel.updateCheckResult.collectAsState()
	val showUpdateDialog by updateViewModel.showUpdateDialog.collectAsState()
	val isCheckingForUpdates by updateViewModel.isCheckingForUpdates.collectAsState()
	val context = LocalContext.current
	val activity = LocalActivity.current as MainActivity

	val darkTheme = when (appSettings.themeMode) {
		ThemeMode.LIGHT -> false
		ThemeMode.DARK -> true
		ThemeMode.SYSTEM -> isSystemInDarkTheme()
	}

	fun openDownloadUrl(url: String) {
		val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
		activity.startActivity(intent)
	}

	fun scheduleUpdateChecks() {
		val workRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
			24, TimeUnit.HOURS
		).build()

		WorkManager.getInstance(context).enqueueUniquePeriodicWork(
			UpdateCheckWorker.WORK_NAME,
			ExistingPeriodicWorkPolicy.KEEP,
			workRequest
		)
	}

	SorterTheme(
		darkTheme = darkTheme,
		dynamicColor = appSettings.useDynamicColors,
		useAureaPadding = appSettings.syncTrashDeletion
	) {
		LaunchedEffect(Unit) {
			scheduleUpdateChecks()

			intent?.let {
				if (it.getBooleanExtra("show_update_dialog", false)) {
					updateViewModel.showUpdateDialogFromNotification()
				}
			}
		}

		val navigationViewModel: NavigationViewModel = hiltViewModel()
		val sorterViewModel: SorterViewModel = hiltViewModel()

		val navigationState by navigationViewModel.navigationState.collectAsState()

		val requestPermissions = PermissionHandler(
			showPermissionDialog = navigationState.showPermissionDialog,
			sorterViewModel = sorterViewModel,
			onPermissionsGranted = {
				navigationViewModel.updatePermissions(true)
			},
			onPermissionsDenied = {
				navigationViewModel.showPermissionDialog(true)
			},
			onDismissDialog = {
				navigationViewModel.showPermissionDialog(false)
			})

		AppNavHost(
			currentScreen = navigationState.currentScreen,
			appSettings = appSettings,
			hasPermissions = navigationState.hasPermissions,
			onRequestPermissions = requestPermissions,
			onNavigate = { action ->
				navigationViewModel.handleAction(action)
			})

		if (showUpdateDialog && updateCheckResult?.updateInfo != null) {
			val updateInfo = updateCheckResult!!.updateInfo!!
			val isCritical = updateViewModel.shouldForceUpdate(updateInfo)

			UpdateDialog(
				updateInfo = updateInfo,
				isCritical = isCritical,
				onDownload = {
					openDownloadUrl(updateInfo.downloadUrl)
					activity.lifecycleScope.launch {
						updateViewModel.markUpdateDismissed(updateInfo.versionName)
					}
				},
				onDismissRequest = {
					activity.lifecycleScope.launch {
						updateViewModel.markUpdateDismissed(updateInfo.versionName)
					}
					updateViewModel.dismissUpdateDialog()
				}
			)
		}
	}
}