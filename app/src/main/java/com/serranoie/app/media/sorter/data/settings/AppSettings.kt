package com.serranoie.app.media.sorter.data.settings

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColors: Boolean = true,
    val useBlurredBackground: Boolean = true,
    val tutorialCompleted: Boolean = false
)

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
