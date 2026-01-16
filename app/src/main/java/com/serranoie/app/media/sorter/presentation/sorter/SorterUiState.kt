package com.serranoie.app.media.sorter.presentation.sorter

import com.serranoie.app.media.sorter.domain.AppError
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi

data class SorterUiState(
    val loadingState: LoadingState = LoadingState.Idle,
    val currentFile: MediaFileUi? = null,
    val currentIndex: Int = 0,
    val totalFiles: Int = 0,
    val deletedFiles: List<MediaFileUi> = emptyList(),
    val isCompleted: Boolean = false,
    val canUndo: Boolean = false,
    val error: AppError? = null
) {
    val deletedCount: Int
        get() = deletedFiles.size

    val progress: Float
        get() = if (totalFiles > 0) currentIndex.toFloat() / totalFiles.toFloat() else 0f

    val isLoading: Boolean
        get() = loadingState is LoadingState.Loading

    val hasError: Boolean
        get() = error != null

    val remainingFiles: Int
        get() = totalFiles - currentIndex
}

sealed class LoadingState {

    data object Idle : LoadingState()

    data object Loading : LoadingState()

    data object Success : LoadingState()

    data class Error(val error: AppError) : LoadingState()
}
