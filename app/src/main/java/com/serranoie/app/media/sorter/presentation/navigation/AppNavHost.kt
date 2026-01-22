package com.serranoie.app.media.sorter.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.serranoie.app.media.sorter.data.settings.AppSettings
import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.presentation.tutorial.OnBoardScreen
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
    
    when (currentScreen) {
        Screen.Onboard -> {
            OnBoardScreen(
                onGetStarted = {
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
                    sorterViewModel.deleteAllReviewedFiles()
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
                onBack = {
                    onNavigate(NavigationAction.NavigateBack)
                }
            )
        }
    }
}
