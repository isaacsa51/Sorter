package com.serranoie.app.media.sorter.presentation.sorter

import android.content.ContentUris
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serranoie.app.media.sorter.domain.media.DeleteMediaUseCase
import com.serranoie.app.media.sorter.domain.media.GetMediaRandomBatchesUseCase
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.media.SorterMediaUseCase
import com.serranoie.app.media.sorter.domain.UndoManager
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import com.serranoie.app.media.sorter.presentation.mapper.MediaFileMapper
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SorterViewModel @Inject constructor(
	private val sorterMediaUseCase: SorterMediaUseCase,
	private val getMediaRandomBatchesUseCase: GetMediaRandomBatchesUseCase,
	private val deleteMediaUseCase: DeleteMediaUseCase,
	private val mediaRepository: MediaRepository,
	private val mediaFileMapper: MediaFileMapper,
	private val undoManager: UndoManager<MediaFileUi>
) : ViewModel() {

	companion object {
		private const val TAG = "SorterViewModel"
	}

	private val _uiState = MutableStateFlow(SorterUiState())
	val uiState: StateFlow<SorterUiState> = _uiState.asStateFlow()

	private val _effects = Channel<SorterEffect>(Channel.BUFFERED)
	val effects = _effects.receiveAsFlow()

	private var allMediaFiles = mutableListOf<MediaFileUi>()

	fun onEvent(event: SorterEvent) {
		when (event) {
			is SorterEvent.LoadMediaFilesRandom -> loadMediaFilesRandom()
			is SorterEvent.LoadMediaFilesChronological -> loadMediaFilesChronological()
			is SorterEvent.KeepCurrentFile -> keepCurrentFile()
			is SorterEvent.TrashCurrentFile -> trashCurrentFile()
			is SorterEvent.UndoLastTrash -> undoLastTrash()
			is SorterEvent.ResetSorter -> resetSorter()
			is SorterEvent.ClearUndoStack -> clearUndoStack()
			is SorterEvent.RemoveFromDeleted -> removeFromDeleted(event.fileId)
			is SorterEvent.DismissError -> dismissError()
			is SorterEvent.RetryLoad -> retryLoad()
		}
	}

	fun loadMediaFiles() = onEvent(SorterEvent.LoadMediaFilesRandom)
	fun keepCurrent() = onEvent(SorterEvent.KeepCurrentFile)
	fun trashCurrent(): MediaFileUi? {
		val file = _uiState.value.currentFile
		onEvent(SorterEvent.TrashCurrentFile)
		return file
	}

	fun undoTrash() = onEvent(SorterEvent.UndoLastTrash)
	fun removeFromDeleted(file: MediaFileUi) = onEvent(SorterEvent.RemoveFromDeleted(file.id))
	fun reset() = onEvent(SorterEvent.ResetSorter)
	fun getUndoCount(): Int = undoManager.getUndoCount()

	private fun loadMediaFilesRandom() {
		viewModelScope.launch {
			Log.d(TAG, "Loading media files in random order")

			_uiState.update { it.copy(loadingState = LoadingState.Loading) }

			when (val result = getMediaRandomBatchesUseCase()) {
				is Result.Success -> {
					val randomBatches = result.data
					Log.d(TAG, "Fetched ${randomBatches.size} date batches")

					val mediaFiles = randomBatches.flatMap { (date, files) ->
						Log.d(TAG, "Date $date: ${files.size} files")
						files.shuffled()
					}

					allMediaFiles = mediaFileMapper.toUiModelList(mediaFiles).toMutableList()
					Log.d(TAG, "Converted to ${allMediaFiles.size} display files")

					resetSorter()
				}

				is Result.Error -> {
					Log.e(TAG, "Error loading media files: ${result.error.message}")
					_uiState.update {
						it.copy(
							loadingState = LoadingState.Error(result.error), error = result.error
						)
					}
					_effects.send(SorterEffect.ShowError(result.error.getFullMessage()))
				}

				is Result.Loading -> {
					// Already in loading state
				}
			}
		}
	}

	fun loadMediaFilesChronological() {
		viewModelScope.launch {
			Log.d(TAG, "Loading media files in chronological order")

			_uiState.update { it.copy(loadingState = LoadingState.Loading) }

			when (val result = sorterMediaUseCase()) {
				is Result.Success -> {
					val mediaFiles = result.data
					Log.d(TAG, "Fetched ${mediaFiles.size} media files")

					allMediaFiles = mediaFileMapper.toUiModelList(mediaFiles).toMutableList()
					Log.d(TAG, "Converted to ${allMediaFiles.size} display files")

					resetSorter()
				}

				is Result.Error -> {
					Log.e(TAG, "Error loading media files: ${result.error.message}")
					_uiState.update {
						it.copy(
							loadingState = LoadingState.Error(result.error), error = result.error
						)
					}
					_effects.send(SorterEffect.ShowError(result.error.getFullMessage()))
				}

				is Result.Loading -> {
					// Already in loading state
				}
			}
		}
	}

	private fun keepCurrentFile() {
		val file = _uiState.value.currentFile
		
		file?.uri?.let { uri ->
			viewModelScope.launch {
				try {
					val mediaId = ContentUris.parseId(uri)
					mediaRepository.markAsViewed(mediaId)
					Log.d(TAG, "Marked kept file ${file.fileName} (ID: $mediaId) as viewed")
				} catch (e: Exception) {
					Log.w(TAG, "Failed to mark file as viewed: ${e.message}")
				}
			}
		}
		
		_uiState.update { state ->
			val nextIndex = state.currentIndex + 1
			if (nextIndex < allMediaFiles.size) {
				state.copy(
					currentFile = allMediaFiles[nextIndex], 
					currentIndex = nextIndex
				)
			} else {
				state.copy(
					currentFile = null, 
					isCompleted = true
				)
			}
		}
	}

	private fun trashCurrentFile() {
		val file = _uiState.value.currentFile ?: return
		val currentIndex = _uiState.value.currentIndex

		file.uri?.let { uri ->
			viewModelScope.launch {
				try {
					val mediaId = ContentUris.parseId(uri)
					mediaRepository.markAsViewed(mediaId)
					Log.d(TAG, "Marked trashed file ${file.fileName} (ID: $mediaId) as viewed")
				} catch (e: Exception) {
					Log.w(TAG, "Failed to mark file as viewed: ${e.message}")
				}
			}
		}

		undoManager.recordAction(file, currentIndex)

		allMediaFiles.removeAt(currentIndex)

		_uiState.update { state ->
			state.copy(
				deletedFiles = state.deletedFiles + file, canUndo = true
			)
		}

		Log.d(TAG, "Trashed ${file.fileName} at index $currentIndex")

		viewModelScope.launch {
			_effects.send(
				SorterEffect.ShowUndoSnackbar(
					file = file, message = "Media moved to trash"
				)
			)
		}

		showCurrentOrComplete()
	}

	private fun undoLastTrash() {
		val lastAction = undoManager.popLastAction() ?: return

		Log.d(TAG, "Undoing trash for ${lastAction.item.fileName}")

		_uiState.update { state ->
			state.copy(
				deletedFiles = state.deletedFiles.filter { it.id != lastAction.item.id },
				canUndo = undoManager.canUndo()
			)
		}

		val insertIndex = lastAction.previousIndex.coerceAtMost(allMediaFiles.size)
		allMediaFiles.add(insertIndex, lastAction.item)

		_uiState.update { state ->
			state.copy(
				currentFile = lastAction.item,
				currentIndex = insertIndex,
				isCompleted = false
			)
		}

		viewModelScope.launch {
			_effects.send(SorterEffect.ShowMessage("File restored"))
		}

		Log.d(TAG, "Restored ${lastAction.item.fileName} at index $insertIndex")
	}

	private fun resetSorter() {
		_uiState.update {
			SorterUiState(
				loadingState = LoadingState.Success,
				currentFile = allMediaFiles.firstOrNull(),
				currentIndex = 0,
				totalFiles = allMediaFiles.size,
				deletedFiles = emptyList(),
				isCompleted = false,
				canUndo = false,
				error = null
			)
		}
	}

	fun clearUndoStack() {
		undoManager.clear()
		_uiState.update { it.copy(canUndo = false) }
		Log.d(TAG, "Undo stack cleared")
	}

	private fun removeFromDeleted(fileId: String) {
		viewModelScope.launch {
			val fileToDelete = _uiState.value.deletedFiles.find { it.id == fileId }

			_uiState.update { state ->
				state.copy(
					deletedFiles = state.deletedFiles.filter { it.id != fileId })
			}

			if (fileToDelete?.uri != null) {
				Log.d(TAG, "Deleting file $fileId (${fileToDelete.fileName}) from storage")

				when (val result = deleteMediaUseCase(fileToDelete.uri)) {
					is Result.Success -> {
						Log.d(TAG, "Successfully deleted ${fileToDelete.fileName} from storage")
						_effects.send(SorterEffect.ShowMessage("Deleted ${fileToDelete.fileName}"))
					}

					is Result.Error -> {
						Log.e(
							TAG,
							"Failed to delete ${fileToDelete.fileName}: ${result.error.message}"
						)
						_effects.send(SorterEffect.ShowMessage("Removed from list (file may already be deleted)"))
					}

					is Result.Loading -> {
						// Should not happen
					}
				}
			} else {
				Log.w(TAG, "File $fileId has no URI, removed from list")
			}

			Log.d(TAG, "Remaining files in deleted list: ${_uiState.value.deletedCount}")
		}
	}

	fun deleteAllReviewedFiles() {
		viewModelScope.launch {
			val filesToDelete = _uiState.value.deletedFiles
			val totalCount = filesToDelete.size

			if (totalCount == 0) {
				Log.d(TAG, "No files to delete")
				return@launch
			}

			Log.d(TAG, "Permanently deleting $totalCount reviewed files from storage")

			val uris = filesToDelete.mapNotNull { it.uri }

			if (uris.isEmpty()) {
				Log.w(TAG, "No valid URIs found for deletion")
				_effects.send(SorterEffect.ShowError("Unable to delete files: No valid URIs"))
				return@launch
			}

			when (val result = deleteMediaUseCase.deleteMultiple(uris)) {
				is Result.Success -> {
					val deletedCount = result.data
					Log.d(
						TAG, "Successfully deleted $deletedCount of $totalCount files from storage"
					)

					_uiState.update { state ->
						state.copy(deletedFiles = emptyList())
					}

					_effects.send(
						SorterEffect.ShowMessage(
							"Permanently deleted $deletedCount file${if (deletedCount != 1) "s" else ""}"
						)
					)

					if (deletedCount < totalCount) {
						val failedCount = totalCount - deletedCount
						Log.w(TAG, "Failed to delete $failedCount files")
						_effects.send(
							SorterEffect.ShowError("Warning: $failedCount file${if (failedCount != 1) "s" else ""} could not be deleted")
						)
					}
				}

				is Result.Error -> {
					Log.e(TAG, "Error deleting files: ${result.error.message}")
					_effects.send(SorterEffect.ShowError("Failed to delete files: ${result.error.getFullMessage()}"))
				}

				is Result.Loading -> {
					// Should not happen
				}
			}
		}
	}

	fun clearDeletedFilesAfterPermissionGrant() {
		viewModelScope.launch {
			val deletedCount = _uiState.value.deletedCount
			Log.d(TAG, "Clearing $deletedCount files from state after system deletion via MediaStore.createDeleteRequest")

			// Note: MediaStore.createDeleteRequest() handles the actual file deletion when user grants permission.
			// The system automatically deletes the files, so we just need to clear the UI state.
			_uiState.update { state ->
				state.copy(deletedFiles = emptyList())
			}

			_effects.send(
				SorterEffect.ShowMessage(
					"Permanently deleted $deletedCount file${if (deletedCount != 1) "s" else ""}"
				)
			)
		}
	}

	private fun dismissError() {
		_uiState.update { it.copy(error = null) }
	}

	private fun retryLoad() {
		loadMediaFilesRandom()
	}

	private fun showCurrentOrComplete() {
		_uiState.update { state ->
			if (state.currentIndex < allMediaFiles.size) {
				state.copy(
					currentFile = allMediaFiles[state.currentIndex], 
					currentIndex = state.currentIndex
				)
			} else {
				state.copy(
					currentFile = null, 
					isCompleted = true
				)
			}
		}
	}
}
