@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.serranoie.app.media.sorter.presentation.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.serranoie.app.media.sorter.presentation.ui.theme.util.ComponentPreview
import com.serranoie.app.media.sorter.presentation.ui.theme.util.PreviewWrapper

/**
 * Badge component that displays the media type (e.g., JPG, MP4)
 *
 * @param mediaType The type of media ("video" or other for images)
 * @param fileName The filename to extract the extension from
 * @param modifier Modifier to be applied to the badge
 */
@Composable
fun MediaTypeBadge(
    mediaType: String,
    fileName: String = "",
    modifier: Modifier = Modifier
) {
    val isVideo = mediaType == "video"
    val colorScheme = MaterialTheme.colorScheme
    
    val extension = fileName.substringAfterLast('.', "").uppercase()
        .takeIf { it.isNotEmpty() } 
        ?: if (isVideo) "MP4" else "JPG" // Fallback if no extension found

    Surface(
        modifier = modifier, shape = RoundedCornerShape(16.dp), 
        color = colorScheme.secondaryContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp, vertical = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isVideo) {
                    Icons.Default.VideoLibrary
                } else {
                    Icons.Default.Image
                }, 
                contentDescription = null, 
                modifier = Modifier.size(18.dp), 
                tint = colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = extension,
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = colorScheme.onSecondaryContainer
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MediaTypeBadgeImagePreview() {
    PreviewWrapper {
        Column {

            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                MediaTypeBadge(mediaType = "image")
            }

            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                MediaTypeBadge(mediaType = "video")
            }
        }
    }
}
