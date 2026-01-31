package com.serranoie.app.media.sorter.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.serranoie.app.media.sorter.update.model.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    isCritical: Boolean,
    onDownload: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onDismiss = if (isCritical) {{}} else onDismissRequest

    val dialogTitle = if (isCritical) "Critical Update Required" else "Update Available"
    val dialogMessage = buildString {
        append("A new version (${updateInfo.versionName}) is available.\n")
        if (isCritical) {
            append("This update is required to continue using the app.\n")
        }
        if (updateInfo.releaseNotes.isNotEmpty()) {
            append("\nWhat's new:\n")
            append(updateInfo.releaseNotes.take(300))
            if (updateInfo.releaseNotes.length > 300)
                append("...")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            val icon = if (isCritical) Icons.Default.Shield else Icons.Default.Download
            val color = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogMessage)
        },
        confirmButton = {
            Button(onClick = onDownload) {
                Text("Download Update")
            }
        },
        dismissButton = {
            if (!isCritical) {
                TextButton(onClick = onDismissRequest) {
                    Text("Later")
                }
            }
        },
        modifier = modifier
    )
}