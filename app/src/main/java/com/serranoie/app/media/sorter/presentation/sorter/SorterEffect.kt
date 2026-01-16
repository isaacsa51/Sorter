package com.serranoie.app.media.sorter.presentation.sorter

import com.serranoie.app.media.sorter.presentation.model.MediaFileUi

sealed class SorterEffect {
    data class ShowUndoSnackbar(
        val file: MediaFileUi,
        val message: String
    ) : SorterEffect()

    data class ShowMessage(val message: String) : SorterEffect()

    data class ShowError(val message: String) : SorterEffect()

    data class Navigate(val destination: NavigationDestination) : SorterEffect()

    data object RequestPermissions : SorterEffect()
}

sealed class NavigationDestination {
    data object Review : NavigationDestination()
    data object Settings : NavigationDestination()
    data object Onboarding : NavigationDestination()
}
