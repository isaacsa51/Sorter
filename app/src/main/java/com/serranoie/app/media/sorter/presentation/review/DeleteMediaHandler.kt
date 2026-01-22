package com.serranoie.app.media.sorter.presentation.review

import android.app.Activity
import android.app.PendingIntent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Helper class to handle media deletion with user permission on Android 10+
 */
class DeleteMediaHandler(
    private val activity: Activity,
    private val onPermissionGranted: () -> Unit,
    private val onPermissionDenied: () -> Unit
) {
    companion object {
        private const val TAG = "DeleteMediaHandler"
    }
    
    private var deleteRequestLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    
    fun setLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.deleteRequestLauncher = launcher
    }
    
    fun requestDeletePermission(uris: List<Uri>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(activity.contentResolver, uris)
            launchPermissionRequest(pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pendingIntent = MediaStore.createDeleteRequest(activity.contentResolver, uris)
            launchPermissionRequest(pendingIntent)
        } else {
            Log.d(TAG, "No delete request needed for Android < 10")
            onPermissionGranted()
        }
    }
    
    private fun launchPermissionRequest(pendingIntent: PendingIntent) {
        try {
            val request = IntentSenderRequest.Builder(pendingIntent).build()
            deleteRequestLauncher?.launch(request)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch delete permission request", e)
            onPermissionDenied()
        }
    }
}

@Composable
fun rememberDeleteMediaHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit = {}
): Pair<DeleteMediaHandler, ActivityResultLauncher<IntentSenderRequest>> {
    val context = LocalContext.current
    val activity = context as? Activity 
        ?: throw IllegalStateException("Context must be an Activity")
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("DeleteMediaHandler", "User granted delete permission")
            onPermissionGranted()
        } else {
            Log.d("DeleteMediaHandler", "User denied delete permission")
            onPermissionDenied()
        }
    }
    
    val handler = remember {
        DeleteMediaHandler(
            activity = activity,
            onPermissionGranted = onPermissionGranted,
            onPermissionDenied = onPermissionDenied
        )
    }
    
    handler.setLauncher(launcher)
    
    return Pair(handler, launcher)
}
