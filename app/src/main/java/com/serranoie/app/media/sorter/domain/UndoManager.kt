package com.serranoie.app.media.sorter.domain

import javax.inject.Inject

/**
 * Action that can be undone.
 * @param T The type of item being acted upon
 */
data class UndoAction<T>(
    val item: T,
    val previousIndex: Int,
    val timestamp: Long = System.currentTimeMillis()
)

class UndoManager<T> @Inject constructor() {
    
    private val undoStack = mutableListOf<UndoAction<T>>()

    fun recordAction(item: T, previousIndex: Int) {
        undoStack.add(
            UndoAction(
                item = item,
                previousIndex = previousIndex,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun peekLastAction(): UndoAction<T>? {
        return undoStack.lastOrNull()
    }

    fun popLastAction(): UndoAction<T>? {
        return if (undoStack.isNotEmpty()) {
            undoStack.removeAt(undoStack.size - 1)
        } else {
            null
        }
    }
    
    fun getUndoCount(): Int = undoStack.size
    
    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun clear() {
        undoStack.clear()
    }

    fun getAllActions(): List<UndoAction<T>> = undoStack.toList()
}
