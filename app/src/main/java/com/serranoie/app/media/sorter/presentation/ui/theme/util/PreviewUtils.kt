package com.serranoie.app.media.sorter.presentation.ui.theme.util

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.serranoie.app.media.sorter.presentation.ui.theme.SorterTheme

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
    SorterTheme {
        Surface {
            content()
        }
    }
}
