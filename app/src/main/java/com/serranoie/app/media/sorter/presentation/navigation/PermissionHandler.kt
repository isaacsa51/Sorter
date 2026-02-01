package com.serranoie.app.media.sorter.presentation.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.presentation.sorter.SorterViewModel

@Composable
fun PermissionHandler(
    showPermissionDialog: Boolean,
    sorterViewModel: SorterViewModel,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit,
    onDismissDialog: () -> Unit
): () -> Unit {
    val context = LocalContext.current
    
    // Check permissions on first composition
    LaunchedEffect(Unit) {
        val granted = checkMediaPermissions(context)
        if (granted) {
            onPermissionsGranted()
            try {
                sorterViewModel.loadMediaFiles()
            } catch (e: Exception) {
                Log.e("PermissionHandler", "Error loading media files", e)
                sorterViewModel.reset()
            }
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("PermissionHandler", "Permissions result: $permissions")
        
        val mediaPermissionsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (permissions[Manifest.permission.READ_MEDIA_IMAGES] == true &&
             permissions[Manifest.permission.READ_MEDIA_VIDEO] == true)
        } else {
            permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
        }
        
        if (mediaPermissionsGranted) {
            Log.d("PermissionHandler", "Media permissions granted, loading media files")
            try {
                sorterViewModel.loadMediaFiles()
                onPermissionsGranted()
            } catch (e: Exception) {
                Log.e("PermissionHandler", "Error loading media files", e)
                sorterViewModel.reset()
                onPermissionsGranted()
            }
        } else {
            Log.w("PermissionHandler", "Media permissions denied")
            onPermissionsDenied()
        }
    }
    
    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = {
                onDismissDialog()
                sorterViewModel.reset()
                onPermissionsGranted()
            },
            onGoToSettings = {
                onDismissDialog()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
    
    return {
        val permissions = getRequiredPermissions()
        Log.d("PermissionHandler", "Requesting permissions: ${permissions.joinToString()}")
        permissionLauncher.launch(permissions)
    }
}

@Composable
private fun PermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_dialog_title)) },
        text = {
            Text(
                stringResource(R.string.permission_dialog_text),
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text(stringResource(R.string.permission_btn_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.permission_btn_continue))
            }
        }
    )
}


fun checkMediaPermissions(context: Context): Boolean {
    val mediaPermissionsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    // Note: Notification permission is not required for core functionality
    // It's requested but not required for the app to work
    return mediaPermissionsGranted
}

fun getRequiredPermissions(): Array<String> {
    val permissions = mutableListOf<String>()
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    
    return permissions.toTypedArray()
}
