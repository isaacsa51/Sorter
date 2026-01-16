package com.serranoie.app.media.sorter.presentation.sorter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serranoie.app.media.sorter.domain.GetMediaRandomBatchesUseCase
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.SorterMediaUseCase
import com.serranoie.app.media.sorter.domain.UndoManager
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
                        files.shuffled() // Shuffle within each date
                    }
                    
                    allMediaFiles = mediaFileMapper.toUiModelList(mediaFiles).toMutableList()
                    Log.d(TAG, "Converted to ${allMediaFiles.size} display files")
                    
                    resetSorter()
                }
                is Result.Error -> {
                    Log.e(TAG, "Error loading media files: ${result.error.message}")
                    _uiState.update { 
                        it.copy(
                            loadingState = LoadingState.Error(result.error),
                            error = result.error
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
                            loadingState = LoadingState.Error(result.error),
                            error = result.error
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
        advanceToNext()
    }
    
    private fun trashCurrentFile() {
        val file = _uiState.value.currentFile ?: return
        val currentIndex = _uiState.value.currentIndex
        
        undoManager.recordAction(file, currentIndex)
        
        _uiState.update { state ->
            state.copy(
                deletedFiles = state.deletedFiles + file,
                canUndo = true
            )
        }
        
        Log.d(TAG, "Trashed ${file.fileName} at index $currentIndex")
        
        viewModelScope.launch {
            _effects.send(
                SorterEffect.ShowUndoSnackbar(
                    file = file,
                    message = "${file.fileName} moved to trash"
                )
            )
        }
        
        advanceToNext()
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
        
        if (lastAction.previousIndex < allMediaFiles.size) {
            allMediaFiles.add(lastAction.previousIndex, lastAction.item)
        } else {
            allMediaFiles.add(lastAction.item)
        }
        
        _uiState.update { state ->
            state.copy(
                currentFile = lastAction.item,
                currentIndex = lastAction.previousIndex,
                isCompleted = false
            )
        }
        
        viewModelScope.launch {
            _effects.send(SorterEffect.ShowMessage("File restored"))
        }
        
        Log.d(TAG, "Restored ${lastAction.item.fileName} at index ${lastAction.previousIndex}")
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
        _uiState.update { state ->
            state.copy(
                deletedFiles = state.deletedFiles.filter { it.id != fileId }
            )
        }
        Log.d(TAG, "Removed file $fileId from deleted list. Remaining: ${_uiState.value.deletedCount}")
    }
    
    private fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
    
    private fun retryLoad() {
        loadMediaFilesRandom()
    }
    
    private fun advanceToNext() {
        _uiState.update { state ->
            if (state.currentIndex < allMediaFiles.size - 1) {
                val nextIndex = state.currentIndex + 1
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
}
