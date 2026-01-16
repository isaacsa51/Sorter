@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.serranoie.app.media.sorter.ui.theme.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serranoie.app.media.sorter.ui.theme.SorterTheme
import com.serranoie.app.media.sorter.ui.theme.util.DevicePreview

/**
 * Completion screen shown when all media files have been sorted
 *
 * @param deletedCount The number of deleted files
 * @param onReviewDeleted Callback when the review deleted button is clicked
 * @param onBackToTutorial Callback when the back to tutorial button is clicked
 * @param modifier Modifier to be applied to the screen
 */
@Composable
fun CompletionScreen(
    deletedCount: Int,
    onReviewDeleted: () -> Unit,
    onBackToTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                shape = CircleShape,
                color = colorScheme.primaryContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.size(120.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "🎉", style = MaterialTheme.typography.displayMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All Done!",
                style = MaterialTheme.typography.headlineLargeEmphasized,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You've sorted all your media files.",
                style = MaterialTheme.typography.bodyLargeEmphasized,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Review deleted files button
            if (deletedCount > 0) {
                Button(
                    onClick = onReviewDeleted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Review Deleted ($deletedCount)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            FilledTonalButton(
                onClick = onBackToTutorial, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Back to Tutorial",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@DevicePreview
@Composable
private fun CompletionScreenWithDeletedFilesPreview() {
    SorterTheme {
        CompletionScreen(deletedCount = 15, onReviewDeleted = {}, onBackToTutorial = {})
    }
}

@DevicePreview
@Composable
private fun CompletionScreenNoDeletedFilesPreview() {
    SorterTheme() {
        CompletionScreen(deletedCount = 0, onReviewDeleted = {}, onBackToTutorial = {})
    }
}
