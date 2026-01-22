package com.serranoie.app.media.sorter.presentation.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

/**
 * Helper class to manage media deletion with user permission on Android 10+
 */
object MediaDeleteHelper {
    private const val TAG = "MediaDeleteHelper"

    const val DELETE_PERMISSION_REQUEST = 1001

    fun launchDeletePermissionDialog(
        activity: Activity,
        pendingIntent: PendingIntent
    ) {
        try {
            Log.d(TAG, "Launching delete permission dialog")
            activity.startIntentSenderForResult(
                pendingIntent.intentSender,
                DELETE_PERMISSION_REQUEST,
                null,
                0,
                0,
                0,
                null
            )
        } catch (e: IntentSender.SendIntentException) {
            Log.e(TAG, "Failed to launch delete permission dialog", e)
        }
    }

    fun launchDeletePermissionDialog(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        pendingIntent: PendingIntent
    ) {
        try {
            Log.d(TAG, "Launching delete permission dialog with ActivityResultLauncher")
            val request = IntentSenderRequest.Builder(pendingIntent).build()
            launcher.launch(request)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch delete permission dialog", e)
        }
    }

    fun isDeletePermissionGranted(resultCode: Int): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
