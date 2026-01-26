package com.serranoie.app.media.sorter.presentation.navigation

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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.serranoie.app.media.sorter.data.settings.AppSettings
import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.presentation.tutorial.TutorialScreen
import com.serranoie.app.media.sorter.presentation.review.ReviewScreen
import com.serranoie.app.media.sorter.presentation.settings.SettingsScreen
import com.serranoie.app.media.sorter.presentation.settings.SettingsViewModel
import com.serranoie.app.media.sorter.presentation.sorter.SorterMediaScreen
import com.serranoie.app.media.sorter.presentation.sorter.SorterViewModel

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
    val scope = rememberCoroutineScope()
    
    val animationDuration = 300
    val fadeInDuration = 250
    val fadeOutDuration = 200
    val slideDistance = 30
    
    val backProgress = remember { Animatable(0f) }
    
    fun getScreenOrder(screen: Screen): Int {
        return when (screen) {
            Screen.Onboard -> 0
            Screen.Sorter -> 1
            Screen.Review -> 2
            Screen.Settings -> 3
        }
    }
    
    PredictiveBackHandler(enabled = currentScreen != Screen.Onboard) { progress ->
        try {
            progress.collect { backEvent ->
                scope.launch {
                    backProgress.snapTo(backEvent.progress)
                }
            }
            scope.launch {
                backProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                backProgress.snapTo(0f)
            }
            onNavigate(NavigationAction.NavigateBack)
        } catch (e: Exception) {
            scope.launch {
                backProgress.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
        }
    }
    
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
                val isForward = getScreenOrder(targetState) > getScreenOrder(initialState)
                
                if (isForward) {
                    (slideInHorizontally(
                        initialOffsetX = { slideDistance },
                        animationSpec = tween(animationDuration)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = fadeInDuration, delayMillis = 50)
                    )) togetherWith (slideOutHorizontally(
                        targetOffsetX = { -slideDistance },
                        animationSpec = tween(animationDuration)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = fadeOutDuration)
                    ))
                } else {
                    (slideInHorizontally(
                        initialOffsetX = { -slideDistance },
                        animationSpec = tween(animationDuration)
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = fadeInDuration, delayMillis = 50)
                    )) togetherWith (slideOutHorizontally(
                        targetOffsetX = { slideDistance },
                        animationSpec = tween(animationDuration)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = fadeOutDuration)
                    ))
                }
            },
            label = "screen_transition"
        ) { screen ->
        when (screen) {
        Screen.Onboard -> {
            TutorialScreen(
                onGetStarted = {
                    settingsViewModel.markTutorialCompleted()
                    
                    if (hasPermissions) {
                        onNavigate(NavigationAction.NavigateTo(Screen.Sorter))
                    } else {
                        onRequestPermissions()
                        onNavigate(NavigationAction.NavigateTo(Screen.Sorter))
                    }
                }
            )
        }
        
        Screen.Sorter -> {
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
                onToggleBackground = { settingsViewModel.toggleBlurredBackground() },
                onBackToOnboarding = {
                    onNavigate(NavigationAction.NavigateTo(Screen.Onboard))
                },
                onNavigateToReview = {
                    onNavigate(NavigationAction.NavigateTo(Screen.Review))
                }
            )
        }
        
        Screen.Review -> {
            val uiState by sorterViewModel.uiState.collectAsState()
            
            ReviewScreen(
                deletedFiles = uiState.deletedFiles,
                onBack = {
                    onNavigate(NavigationAction.NavigateBack)
                },
                onSettings = {
                    onNavigate(NavigationAction.NavigateTo(Screen.Settings))
                },
                onInfo = {
                    onNavigate(NavigationAction.NavigateTo(Screen.Settings))
                },
                onRemoveItem = { file ->
                    sorterViewModel.removeFromDeleted(file)
                },
                onDeleteAll = {
                    sorterViewModel.clearDeletedFilesAfterPermissionGrant()
                }
            )
        }

        Screen.Settings -> {
            SettingsScreen(
                appTheme = when (appSettings.themeMode) {
					ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.SYSTEM -> "System"
                },
                isMaterialYouEnabled = appSettings.useDynamicColors,
                isBlurredBackgroundEnabled = appSettings.useBlurredBackground,
                isAutoPlayEnabled = appSettings.autoPlayVideos,
                syncFileToTrashBin = appSettings.syncTrashDeletion,
                onThemeChange = { theme ->
                    val themeMode = when (theme) {
                        "Light" -> ThemeMode.LIGHT
                        "Dark" -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    }
                    settingsViewModel.setThemeMode(themeMode)
                },
                onMaterialYouToggle = { settingsViewModel.toggleDynamicColors() },
                onBlurredBackgroundToggle = { settingsViewModel.toggleBlurredBackground() },
                onAutoPlayToggle = { settingsViewModel.toggleAutoPlayVideos() },
                onSyncFileToTrashBinToggle = { settingsViewModel.toggleSyncTrashDeletion() },
                onResetTutorial = {
                    settingsViewModel.resetTutorial()
                    onNavigate(NavigationAction.NavigateTo(Screen.Onboard))
                },
                onBack = {
                    onNavigate(NavigationAction.NavigateBack)
                }
            )
        }
        }
    }
    }
}
