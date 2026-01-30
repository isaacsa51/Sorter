package com.serranoie.app.media.sorter.presentation.navigation

import android.util.Log
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import com.serranoie.app.media.sorter.data.settings.AppSettings
import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import com.serranoie.app.media.sorter.presentation.tutorial.TutorialScreen
import com.serranoie.app.media.sorter.presentation.review.ReviewScreen
import com.serranoie.app.media.sorter.presentation.settings.SettingsScreen
import com.serranoie.app.media.sorter.presentation.settings.SettingsViewModel
import com.serranoie.app.media.sorter.presentation.sorter.SorterMediaScreen
import com.serranoie.app.media.sorter.presentation.sorter.SorterViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

private fun getScreenOrder(screen: Screen): Int {
	return when (screen) {
		Screen.Onboard -> 0
		Screen.Sorter -> 1
		Screen.Review -> 2
		Screen.Settings -> 3
	}
}

@Composable
private fun HandlePredictiveBack(
	enabled: Boolean, backProgress: Animatable<Float, *>, onNavigateBack: () -> Unit
) {
	val scope = rememberCoroutineScope()

	PredictiveBackHandler(enabled = enabled) { progress ->
		try {
			progress.collect { backEvent ->
				scope.launch {
					backProgress.snapTo(backEvent.progress)
				}
			}
			scope.launch {
				backProgress.animateTo(
					targetValue = 1f, animationSpec = spring(
						dampingRatio = Spring.DampingRatioNoBouncy,
						stiffness = Spring.StiffnessMedium
					)
				)
				backProgress.snapTo(0f)
			}
			onNavigateBack()
		} catch (e: Exception) {
			scope.launch {
				backProgress.animateTo(
					targetValue = 0f, animationSpec = spring(
						dampingRatio = Spring.DampingRatioNoBouncy,
						stiffness = Spring.StiffnessMedium
					)
				)
				Log.e("AppNavHost", "Error in PredictiveBackHandler", e)
			}
		}
	}
}

@Composable
fun AppNavHost(
	currentScreen: Screen,
	appSettings: AppSettings,
	hasPermissions: Boolean,
	onRequestPermissions: () -> Unit,
	onNavigate: (NavigationAction) -> Unit
) {
	val sorterViewModel: SorterViewModel = hiltViewModel()
	val settingsViewModel: SettingsViewModel = hiltViewModel()
	val context = LocalContext.current
	val repository = remember {
		EntryPointAccessors.fromApplication<MediaRepositoryEntryPoint>(
			context.applicationContext
		).mediaRepository()
	}

	val backProgress = remember { Animatable(0f) }
	val backEnabled =
		currentScreen != Screen.Onboard && !(currentScreen == Screen.Sorter && appSettings.tutorialCompleted)

	HandlePredictiveBack(
		enabled = backEnabled,
		backProgress = backProgress,
		onNavigateBack = { onNavigate(NavigationAction.NavigateBack) })

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		AnimatedContent(
			targetState = currentScreen,
			modifier = Modifier
				.fillMaxSize()
				.graphicsLayer {
					val progress = backProgress.value
					translationX = progress * size.width * 0.15f
					scaleX = 1f - (progress * 0.1f)
					scaleY = 1f - (progress * 0.1f)
					alpha = 1f - (progress * 0.3f)
				},
			transitionSpec = {
				getScreenTransition(
					isForward = getScreenOrder(targetState) > getScreenOrder(initialState)
				)
			},
			label = "screen_transition"
		) { screen ->
			when (screen) {
				Screen.Onboard -> OnboardScreen(
					settingsViewModel = settingsViewModel,
					hasPermissions = hasPermissions,
					onRequestPermissions = onRequestPermissions,
					onNavigate = onNavigate
				)

				Screen.Sorter -> SorterScreen(
					sorterViewModel = sorterViewModel,
					settingsViewModel = settingsViewModel,
					appSettings = appSettings,
					onNavigate = onNavigate
				)

				Screen.Review -> ReviewScreenWrapper(
					sorterViewModel = sorterViewModel,
					repository = repository,
					appSettings = appSettings,
					onNavigate = onNavigate
				)

				Screen.Settings -> SettingsScreenWrapper(
					settingsViewModel = settingsViewModel,
					appSettings = appSettings,
					onNavigate = onNavigate
				)
			}
		}
	}
}

private fun getScreenTransition(isForward: Boolean) = if (isForward) {
	(slideInHorizontally(
		initialOffsetX = { 30 }, animationSpec = tween(300)
	) + fadeIn(
		animationSpec = tween(durationMillis = 250, delayMillis = 50)
	)) togetherWith (slideOutHorizontally(
		targetOffsetX = { -30 }, animationSpec = tween(300)
	) + fadeOut(
		animationSpec = tween(durationMillis = 200)
	))
} else {
	(slideInHorizontally(
		initialOffsetX = { -30 }, animationSpec = tween(300)
	) + fadeIn(
		animationSpec = tween(durationMillis = 250, delayMillis = 50)
	)) togetherWith (slideOutHorizontally(
		targetOffsetX = { 30 }, animationSpec = tween(300)
	) + fadeOut(
		animationSpec = tween(durationMillis = 200)
	))
}

@Composable
private fun OnboardScreen(
	settingsViewModel: SettingsViewModel,
	hasPermissions: Boolean,
	onRequestPermissions: () -> Unit,
	onNavigate: (NavigationAction) -> Unit
) {
	TutorialScreen(
		onGetStarted = {
			settingsViewModel.markTutorialCompleted()
			if (hasPermissions) {
				onNavigate(NavigationAction.NavigateTo(Screen.Sorter))
			} else {
				onRequestPermissions()
				onNavigate(NavigationAction.NavigateTo(Screen.Sorter))
			}
		})
}

@Composable
private fun SorterScreen(
	sorterViewModel: SorterViewModel,
	settingsViewModel: SettingsViewModel,
	appSettings: AppSettings,
	onNavigate: (NavigationAction) -> Unit
) {
	val uiState by sorterViewModel.uiState.collectAsState()

	SorterMediaScreen(
		currentFile = uiState.currentFile,
		isCompleted = uiState.isCompleted,
		deletedCount = uiState.deletedCount,
		useBlurredBackground = appSettings.useBlurredBackground,
		autoPlayVideos = appSettings.autoPlayVideos,
		onKeepCurrent = { sorterViewModel.keepCurrent() },
		onTrashCurrent = { sorterViewModel.trashCurrent() },
		onUndoTrash = { sorterViewModel.undoTrash() },
		onBackToOnboarding = if (!appSettings.tutorialCompleted) {
			{ onNavigate(NavigationAction.NavigateTo(Screen.Onboard)) }
		} else null,
		onNavigateToReview = {
			onNavigate(NavigationAction.NavigateTo(Screen.Review))
		},
		onNavigateToSettings = {
			onNavigate(NavigationAction.NavigateTo(Screen.Settings))
		})
}

@Composable
private fun ReviewScreenWrapper(
	sorterViewModel: SorterViewModel,
	repository: MediaRepository,
	appSettings: AppSettings,
	onNavigate: (NavigationAction) -> Unit
) {
	val uiState by sorterViewModel.uiState.collectAsState()

	ReviewScreen(
		deletedFiles = uiState.deletedFiles,
		repository = repository,
		useTrash = appSettings.syncTrashDeletion,
		onBack = { onNavigate(NavigationAction.NavigateBack) },
		onSettings = { onNavigate(NavigationAction.NavigateTo(Screen.Settings)) },
		onRemoveItem = { file -> sorterViewModel.removeFromDeleted(file) },
		onDeleteAll = { sorterViewModel.clearDeletedFilesAfterPermissionGrant() })
}

@Composable
private fun SettingsScreenWrapper(
	settingsViewModel: SettingsViewModel,
	appSettings: AppSettings,
	onNavigate: (NavigationAction) -> Unit
) {
	SettingsScreen(
		appTheme = getThemeString(appSettings.themeMode),
		isMaterialYouEnabled = appSettings.useDynamicColors,
		isBlurredBackgroundEnabled = appSettings.useBlurredBackground,
		isAutoPlayEnabled = appSettings.autoPlayVideos,
		syncFileToTrashBin = appSettings.syncTrashDeletion,
		onThemeChange = { theme -> settingsViewModel.setThemeMode(getThemeMode(theme)) },
		onMaterialYouToggle = { settingsViewModel.toggleDynamicColors() },
		onBlurredBackgroundToggle = { settingsViewModel.toggleBlurredBackground() },
		onAutoPlayToggle = { settingsViewModel.toggleAutoPlayVideos() },
		onSyncFileToTrashBinToggle = { settingsViewModel.toggleSyncTrashDeletion() },
		onResetTutorial = {
			settingsViewModel.resetTutorial()
			onNavigate(NavigationAction.NavigateTo(Screen.Onboard))
		},
		onResetViewedHistory = { settingsViewModel.resetViewedHistory() },
		onBack = { onNavigate(NavigationAction.NavigateBack) })
}

private fun getThemeString(themeMode: ThemeMode): String {
	return when (themeMode) {
		ThemeMode.LIGHT -> "Light"
		ThemeMode.DARK -> "Dark"
		ThemeMode.SYSTEM -> "System"
	}
}

private fun getThemeMode(theme: String): ThemeMode {
	return when (theme) {
		"Light" -> ThemeMode.LIGHT
		"Dark" -> ThemeMode.DARK
		else -> ThemeMode.SYSTEM
	}
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MediaRepositoryEntryPoint {
	fun mediaRepository(): MediaRepository
}
