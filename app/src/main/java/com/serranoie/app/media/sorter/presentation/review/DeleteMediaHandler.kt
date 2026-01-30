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
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper class to handle media deletion with user permission on Android 10+
 *
 * Uses the recommended pattern:
 * 1. Try to delete files directly
 * 2. If RecoverableSecurityException is thrown, request permission via its IntentSender
 * 3. After permission granted, files are automatically deleted by the system
 *
 * Supports both trash (30-day recovery) and permanent deletion based on user preference
 */
class DeleteMediaHandler(
	private val activity: Activity,
	private val repository: MediaRepository,
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

	suspend fun requestDeletePermission(uris: List<Uri>, useTrash: Boolean = false) =
		withContext(Dispatchers.IO) {
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
						Log.d(TAG, "RecoverableSecurityException for $uri, will request permission, $e")
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
					Log.d(
						TAG,
						"Requesting permission for ${urisNeedingPermission.size} files (useTrash: $useTrash)"
					)
					withContext(Dispatchers.Main) {
						val pendingIntent =
							repository.createDeletionRequest(urisNeedingPermission, useTrash)
						if (pendingIntent != null) {
							launchPermissionRequest(pendingIntent)
						} else {
							// Fallback to direct MediaStore.createDeleteRequest if repository method failed
							Log.w(
								TAG,
								"Repository createDeletionRequest returned null, using direct MediaStore call"
							)
							val fallbackIntent = MediaStore.createDeleteRequest(
								activity.contentResolver,
								urisNeedingPermission
							)
							launchPermissionRequest(fallbackIntent)
						}
					}
				} else {
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
	repository: MediaRepository,
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

	val handler = remember(repository) {
		DeleteMediaHandler(
			activity = activity,
			repository = repository,
			onPermissionGranted = onPermissionGranted,
			onPermissionDenied = onPermissionDenied
		)
	}

	handler.setLauncher(launcher)

	return Pair(handler, launcher)
}
