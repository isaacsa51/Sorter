package com.serranoie.app.media.sorter.presentation.ui.components

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
import androidx.compose.ui.res.stringResource
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.data.update.model.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    isCritical: Boolean,
    onDownload: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onDismiss = if (isCritical) {{}} else onDismissRequest

    val dialogTitle = stringResource(
        if (isCritical) R.string.update_dialog_title_critical 
        else R.string.update_dialog_title_normal
    )
    val dialogMessage = buildString {
        append(stringResource(R.string.update_dialog_message_new_version, updateInfo.versionName))
        if (isCritical) {
            append(stringResource(R.string.update_dialog_message_critical))
        }
        if (updateInfo.releaseNotes.isNotEmpty()) {
            append(stringResource(R.string.update_dialog_message_whats_new))
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
                Text(stringResource(R.string.update_dialog_btn_download))
            }
        },
        dismissButton = {
            if (!isCritical) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.update_dialog_btn_later))
                }
            }
        },
        modifier = modifier
    )
}