package com.serranoie.app.media.sorter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.presentation.navigation.AppNavHost
import com.serranoie.app.media.sorter.presentation.navigation.NavigationAction
import com.serranoie.app.media.sorter.presentation.navigation.NavigationViewModel
import com.serranoie.app.media.sorter.presentation.navigation.PermissionHandler
import com.serranoie.app.media.sorter.presentation.navigation.Screen
import com.serranoie.app.media.sorter.presentation.settings.SettingsViewModel
import com.serranoie.app.media.sorter.presentation.sorter.SorterViewModel
import com.serranoie.app.media.sorter.ui.theme.SorterTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity - Entry point of the application
 * Handles theme application and navigation setup
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SorterApp()
        }
    }
}

/**
 * Root composable that applies theme based on settings
 */
@Composable
fun SorterApp() {
    // Get settings ViewModel
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appSettings by settingsViewModel.appSettings.collectAsState()
    
    // Determine if we should use dark theme
    val darkTheme = when (appSettings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    SorterTheme(
        darkTheme = darkTheme,
        dynamicColor = appSettings.useDynamicColors
    ) {
        SorterAppContent()
    }
}

/**
 * Main app content with navigation
 */
@Composable
fun SorterAppContent() {
    val navigationViewModel: NavigationViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val sorterViewModel: SorterViewModel = hiltViewModel()
    
    val navigationState by navigationViewModel.navigationState.collectAsState()
    val appSettings by settingsViewModel.appSettings.collectAsState()
    
    val requestPermissions = PermissionHandler(
        hasPermissions = navigationState.hasPermissions,
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
        }
    )
    
    BackHandler(enabled = navigationState.currentScreen != Screen.Onboard) {
        navigationViewModel.handleAction(NavigationAction.NavigateBack)
    }
    
    AppNavHost(
        currentScreen = navigationState.currentScreen,
        appSettings = appSettings,
        hasPermissions = navigationState.hasPermissions,
        onRequestPermissions = requestPermissions,
        onNavigate = { action ->
            navigationViewModel.handleAction(action)
        }
    )
}