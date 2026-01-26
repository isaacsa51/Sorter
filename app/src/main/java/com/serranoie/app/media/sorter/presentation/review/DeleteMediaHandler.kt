package com.serranoie.app.media.sorter.presentation.review

import android.app.Activity
import android.app.PendingIntent
import android.app.RecoverableSecurityException
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class to handle media deletion with user permission on Android 10+
 * 
 * Uses the recommended pattern:
 * 1. Try to delete files directly
 * 2. If RecoverableSecurityException is thrown, request permission via its IntentSender
 * 3. After permission granted, files are automatically deleted by the system
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
    
    suspend fun requestDeletePermission(uris: List<Uri>) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Try to delete files directly first (recommended Android pattern)
            var needsPermission = false
            val urisNeedingPermission = mutableListOf<Uri>()
            
            for (uri in uris) {
                try {
                    val deletedRows = activity.contentResolver.delete(uri, null, null)
                    if (deletedRows > 0) {
                        Log.d(TAG, "Successfully deleted $uri directly")
                    } else {
                        Log.w(TAG, "Failed to delete $uri (0 rows affected)")
                        urisNeedingPermission.add(uri)
                        needsPermission = true
                    }
                } catch (e: RecoverableSecurityException) {
                    // This is expected for files we don't own - collect them for batch permission request
                    Log.d(TAG, "RecoverableSecurityException for $uri, will request permission")
                    urisNeedingPermission.add(uri)
                    needsPermission = true
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException for $uri", e)
                    urisNeedingPermission.add(uri)
                    needsPermission = true
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error deleting $uri", e)
                }
            }
            
            if (needsPermission && urisNeedingPermission.isNotEmpty()) {
                // Use MediaStore.createDeleteRequest for remaining files
                Log.d(TAG, "Requesting permission to delete ${urisNeedingPermission.size} files")
                withContext(Dispatchers.Main) {
                    val pendingIntent = MediaStore.createDeleteRequest(activity.contentResolver, urisNeedingPermission)
                    launchPermissionRequest(pendingIntent)
                }
            } else {
                // All files deleted successfully without needing permission
                Log.d(TAG, "All ${uris.size} files deleted successfully")
                withContext(Dispatchers.Main) {
                    onPermissionGranted()
                }
            }
        } else {
            // Android 9 and below - try direct deletion
            Log.d(TAG, "Android < 10, attempting direct deletion")
            withContext(Dispatchers.Main) {
                onPermissionGranted()
            }
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
