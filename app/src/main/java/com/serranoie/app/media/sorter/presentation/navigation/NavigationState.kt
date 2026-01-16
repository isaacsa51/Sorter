package com.serranoie.app.media.sorter.presentation.navigation

data class NavigationState(
    val currentScreen: Screen = Screen.Onboard,
    val hasPermissions: Boolean = false,
    val showPermissionDialog: Boolean = false
)

sealed class NavigationAction {
    data class NavigateTo(val screen: Screen) : NavigationAction()
    data object NavigateBack : NavigationAction()
    data class UpdatePermissions(val granted: Boolean) : NavigationAction()
    data class ShowPermissionDialog(val show: Boolean) : NavigationAction()
}
