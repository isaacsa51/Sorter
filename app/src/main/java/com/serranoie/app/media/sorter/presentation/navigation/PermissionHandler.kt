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
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.serranoie.app.media.sorter.presentation.sorter.SorterViewModel

@Composable
fun PermissionHandler(
    hasPermissions: Boolean,
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
        val allGranted = permissions.values.all { it }
        
        if (allGranted) {
            Log.d("PermissionHandler", "Permissions granted, loading media files")
            try {
                sorterViewModel.loadMediaFiles()
                onPermissionsGranted()
            } catch (e: Exception) {
                Log.e("PermissionHandler", "Error loading media files", e)
                sorterViewModel.reset()
                onPermissionsGranted()
            }
        } else {
            Log.w("PermissionHandler", "Permissions denied")
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
        title = { Text("Storage Permission Required") },
        text = {
            Text(
                "This app needs access to your photos and videos to help you sort them. " +
                        "Without permission, you'll see sample data only.\n\n" +
                        "You can grant permission in Settings.",
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue with Sample Data")
            }
        }
    )
}


fun checkMediaPermissions(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
}

fun getRequiredPermissions(): Array<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
