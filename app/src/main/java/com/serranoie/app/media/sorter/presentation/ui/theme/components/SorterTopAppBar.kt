@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.serranoie.app.media.sorter.presentation.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.serranoie.app.media.sorter.presentation.ui.theme.util.PreviewWrapper

/**
 * Top app bar for the Sorter screen with date badge, background toggle, and grid view button
 * 
 * @param date The date to display in the center badge
 * @param deletedCount The count of deleted items to show in the badge
 * @param useBlurredBackground Whether the blurred background is currently enabled
 * @param onNavigateToReview Callback when the grid view button is clicked
 * @param modifier Modifier to be applied to the app bar
 */
@Composable
fun SorterTopAppBar(
    date: String,
    deletedCount: Int,
    useBlurredBackground: Boolean = true,
    onNavigateToReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
//            FilledIconButton(
//                onClick = onToggleBackground,
//                modifier = Modifier
//                    .align(Alignment.CenterStart)
//                    .size(48.dp),
//                colors = IconButtonDefaults.filledIconButtonColors(
//                    containerColor = Color.Black.copy(alpha = 0.4f),
//                    contentColor = Color.White
//                )
//            ) {
//                Icon(
//                    imageVector = if (useBlurredBackground) {
//                        Icons.Filled.BlurOn
//                    } else {
//                        Icons.Outlined.BlurOn
//                    },
//                    contentDescription = "Toggle background",
//                    modifier = Modifier.size(24.dp)
//                )
//            }
            
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.4f),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = date,
                    modifier = Modifier.padding(
                        horizontal = 20.dp, 
                        vertical = 10.dp
                    ),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                FilledIconButton(
                    onClick = onNavigateToReview,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PermMedia,
                        contentDescription = "Review deleted files",
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (deletedCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp),
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text(
                            text = deletedCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SorterTopAppBarWithBadgePreview() {
    PreviewWrapper {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1B1F))
                .padding(vertical = 16.dp)
        ) {
            SorterTopAppBar(
                date = "May 23, 2024",
                deletedCount = 5,
                useBlurredBackground = true,
                onNavigateToReview = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SorterTopAppBarNoBadgePreview() {
    PreviewWrapper {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1C1B1F))
                .padding(vertical = 16.dp)
        ) {
            SorterTopAppBar(
                date = "May 23, 2024",
                deletedCount = 0,
                useBlurredBackground = false,
                onNavigateToReview = {}
            )
        }
    }
}
