package com.serranoie.app.media.sorter.ui.theme.util

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
	showSystemUi = true,
)
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_NIGHT_YES,
    device = "spec:parent=pixel_5,navigation=buttons", showSystemUi = true,
)
annotation class DevicePreview

/**
 * Component preview with small size (100x100 dp)
 * Useful for small UI components like badges, chips, and icons
 */
@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = false,
    widthDp = 100,
    heightDp = 100
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    showSystemUi = false,
    widthDp = 100,
    heightDp = 100,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class ComponentPreviewSmall

/**
 * Component preview with medium size (200x150 dp)
 * Useful for medium-sized components like cards and list items
 */
@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = false,
    widthDp = 200,
    heightDp = 150
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    showSystemUi = false,
    widthDp = 200,
    heightDp = 150,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class ComponentPreviewMedium

/**
 * Component preview with large size (320x200 dp)
 * Useful for larger components and dialogs
 */
@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = false,
    widthDp = 320,
    heightDp = 600
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    showSystemUi = false,
    widthDp = 320,
    heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class ComponentPreviewLarge

/**
 * Component preview with custom size
 * Use this as a template to create your own size variations
 */
@Preview(
    name = "Light Mode",
    showBackground = true,
    showSystemUi = false,
    widthDp = 320,
    heightDp = 600
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    showSystemUi = false,
	widthDp = 320,
	heightDp = 600,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
annotation class ComponentPreview
