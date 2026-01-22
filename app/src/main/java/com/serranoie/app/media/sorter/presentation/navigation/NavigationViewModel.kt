package com.serranoie.app.media.sorter.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serranoie.app.media.sorter.domain.settings.GetAppSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val getAppSettings: GetAppSettingsUseCase
) : ViewModel() {
    
    private val _navigationState = MutableStateFlow(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    
    init {
        viewModelScope.launch {
            getAppSettings().collect { settings ->
                if (settings.tutorialCompleted && _navigationState.value.currentScreen == Screen.Onboard) {
                    navigateTo(Screen.Sorter)
                }
            }
        }
    }
    
    fun navigateTo(screen: Screen) {
        _navigationState.update { it.copy(currentScreen = screen) }
    }

    fun navigateBack() {
	    val previousScreen = when (val currentScreen = _navigationState.value.currentScreen) {
            Screen.Sorter -> Screen.Onboard
            Screen.Review -> Screen.Sorter
            Screen.Settings -> Screen.Review
            else -> currentScreen
        }
        navigateTo(previousScreen)
    }

    fun updatePermissions(granted: Boolean) {
        _navigationState.update { it.copy(hasPermissions = granted) }
    }

    fun showPermissionDialog(show: Boolean) {
        _navigationState.update { it.copy(showPermissionDialog = show) }
    }

    fun handleAction(action: NavigationAction) {
        when (action) {
            is NavigationAction.NavigateTo -> navigateTo(action.screen)
            is NavigationAction.NavigateBack -> navigateBack()
            is NavigationAction.UpdatePermissions -> updatePermissions(action.granted)
            is NavigationAction.ShowPermissionDialog -> showPermissionDialog(action.show)
        }
    }
}
