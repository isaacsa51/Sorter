package com.serranoie.app.media.sorter.presentation.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.presentation.ui.theme.SorterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

	@get:Rule
	val composeRule = createAndroidComposeRule<ComponentActivity>()

	@Test
	fun settingsScreen_renders_and_back_calls_callback() {
		val context = composeRule.activity
		var backClicked = false

		composeRule.setContent {
			SorterTheme {
				SettingsScreen(
					appTheme = "System",
					isMaterialYouEnabled = true,
					isBlurredBackgroundEnabled = true,
					isAutoPlayEnabled = false,
					syncFileToTrashBin = false,
					onThemeChange = {},
					onMaterialYouToggle = {},
					onBlurredBackgroundToggle = {},
					onAutoPlayToggle = {},
					onSyncFileToTrashBinToggle = {},
					onResetTutorial = {},
					onResetViewedHistory = {},
					onBack = { backClicked = true },
					onCheckForUpdates = {},
					onDismissUpdateMessage = {}
				)
			}
		}

		composeRule.onNodeWithTag("SettingsScreen").assertExists()
		composeRule.onNodeWithText(context.getString(R.string.settings_title)).assertIsDisplayed()

		// Click back icon
		composeRule.onNodeWithTag("SettingsBackButton").performClick()
		composeRule.runOnIdle { assert(backClicked) }
	}

	@Test
	fun themeItem_opens_dialog_and_selecting_theme_calls_callback() {
		var themeSelected: String? = null

		composeRule.setContent {
			SorterTheme {
				SettingsScreen(
					appTheme = "System",
					isMaterialYouEnabled = true,
					isBlurredBackgroundEnabled = true,
					isAutoPlayEnabled = false,
					syncFileToTrashBin = false,
					onThemeChange = { themeSelected = it },
					onMaterialYouToggle = {},
					onBlurredBackgroundToggle = {},
					onAutoPlayToggle = {},
					onSyncFileToTrashBinToggle = {},
					onResetTutorial = {},
					onResetViewedHistory = {},
					onBack = {},
					onCheckForUpdates = {},
					onDismissUpdateMessage = {}
				)
			}
		}

		composeRule.onNodeWithTag("ThemePickerDialog").assertDoesNotExist()
		composeRule.onNodeWithTag("SettingsThemeItem").performClick()
		composeRule.onNodeWithTag("ThemePickerDialog").assertExists()

		// Dialog uses hardcoded English strings
		composeRule.onNodeWithText("Dark").performClick()
		composeRule.runOnIdle {
			assert(themeSelected == "Dark")
		}
	}

	@Test
	fun updateCheckMessage_shows_snackbar() {
		val message = "Update available!"

		composeRule.setContent {
			SorterTheme {
				SettingsScreen(
					appTheme = "System",
					isMaterialYouEnabled = true,
					isBlurredBackgroundEnabled = true,
					isAutoPlayEnabled = false,
					syncFileToTrashBin = false,
					updateCheckMessage = message,
					onThemeChange = {},
					onMaterialYouToggle = {},
					onBlurredBackgroundToggle = {},
					onAutoPlayToggle = {},
					onSyncFileToTrashBinToggle = {},
					onResetTutorial = {},
					onResetViewedHistory = {},
					onBack = {},
					onCheckForUpdates = {},
					onDismissUpdateMessage = {}
				)
			}
		}

		composeRule.waitUntil(timeoutMillis = 5_000) {
			composeRule.onAllNodesWithText(message).fetchSemanticsNodes().isNotEmpty()
		}
		composeRule.onNodeWithText(message).assertIsDisplayed()
	}

	@Test
	fun toggles_call_callbacks() {
		var materialYouToggles = 0
		var autoplayToggles = 0

		composeRule.setContent {
			SorterTheme {
				SettingsScreen(
					appTheme = "System",
					isMaterialYouEnabled = true,
					isBlurredBackgroundEnabled = true,
					isAutoPlayEnabled = false,
					syncFileToTrashBin = false,
					onThemeChange = {},
					onMaterialYouToggle = { materialYouToggles++ },
					onBlurredBackgroundToggle = {},
					onAutoPlayToggle = { autoplayToggles++ },
					onSyncFileToTrashBinToggle = {},
					onResetTutorial = {},
					onResetViewedHistory = {},
					onBack = {},
					onCheckForUpdates = {},
					onDismissUpdateMessage = {}
				)
			}
		}

		composeRule.onNodeWithTag("SettingsMaterialYouSwitch").performClick()
		composeRule.onNodeWithTag("SettingsAutoplaySwitch").performClick()

		composeRule.runOnIdle {
			assert(materialYouToggles == 1)
			assert(autoplayToggles == 1)
		}
	}
}
