package com.serranoie.app.media.sorter.presentation.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serranoie.app.media.sorter.data.update.model.UpdateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: UpdateManager
) : ViewModel() {

    private val _updateCheckResult = MutableStateFlow<UpdateCheckResponse?>(null)
    val updateCheckResult: StateFlow<UpdateCheckResponse?> = _updateCheckResult.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _isCheckingForUpdates = MutableStateFlow(false)
    val isCheckingForUpdates: StateFlow<Boolean> = _isCheckingForUpdates.asStateFlow()

    val currentVersionName: String
        get() = updateManager.currentVersionName

    val currentVersionCode: Int
        get() = updateManager.currentVersionCode

    fun checkForUpdates(forceCheck: Boolean = false) {
        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            try {
                val response = updateManager.checkForUpdates(forceCheck)
                _updateCheckResult.value = response
                _showUpdateDialog.value = response.hasUpdate
            } catch (e: Exception) {
                // Don't show error update response, just clear it
                _updateCheckResult.value = null
            } finally {
                _isCheckingForUpdates.value = false
            }
        }
    }

    fun showUpdateDialogFromNotification() {
        viewModelScope.launch {
            _isCheckingForUpdates.value = true
            delay(1000)
            val response = updateManager.checkForUpdates(forceCheck = true)
            _isCheckingForUpdates.value = false
            _updateCheckResult.value = response
            _showUpdateDialog.value = response.hasUpdate
        }
    }

    fun dismissUpdateDialog() {
        _showUpdateDialog.value = false
    }

    fun shouldForceUpdate(updateInfo: UpdateInfo?): Boolean {
        return updateManager.shouldForceUpdate(updateInfo)
    }

    fun markUpdateDismissed(version: String) {
        viewModelScope.launch {
            updateManager.markUpdateDismissed(version)
        }
    }

    fun getDismissedUpdateVersion(): String? {
        // This would need to be made suspending for proper implementation
        return null
    }
}