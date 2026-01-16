package com.serranoie.app.media.sorter.presentation.sorter

sealed class SorterEvent {

    data object LoadMediaFilesRandom : SorterEvent()

    data object LoadMediaFilesChronological : SorterEvent()

    data object KeepCurrentFile : SorterEvent()

    data object TrashCurrentFile : SorterEvent()

    data object UndoLastTrash : SorterEvent()

    data object ResetSorter : SorterEvent()

    data object ClearUndoStack : SorterEvent()

    data class RemoveFromDeleted(val fileId: String) : SorterEvent()

    data object DismissError : SorterEvent()

    data object RetryLoad : SorterEvent()
}
