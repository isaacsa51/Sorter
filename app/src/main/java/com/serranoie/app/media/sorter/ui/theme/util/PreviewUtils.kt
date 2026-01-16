package com.serranoie.app.media.sorter.ui.theme.util

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.serranoie.app.media.sorter.ui.theme.SorterTheme

@Composable
fun PreviewWrapper(content: @Composable () -> Unit) {
    SorterTheme {
        Surface {
            content()
        }
    }
}
