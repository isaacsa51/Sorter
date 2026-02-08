package com.serranoie.app.media.sorter.presentation.tutorial

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
class TutorialScreenTest {

	@get:Rule
	val composeRule = createAndroidComposeRule<ComponentActivity>()

	@Test
	fun tutorialScreen_renders_core_content() {
		val context = composeRule.activity
		composeRule.setContent {
			SorterTheme {
				TutorialScreen()
			}
		}

		composeRule.onNodeWithTag("TutorialScreen").assertExists()
		composeRule.onNodeWithText(context.getString(R.string.tutorial_title)).assertIsDisplayed()
		composeRule.onNodeWithTag("TutorialGetStartedButton").assertIsDisplayed()
		composeRule.onNodeWithContentDescription(context.getString(R.string.content_desc_demo_media))
			.assertExists()
	}

	@Test
	fun getStartedButton_calls_callback() {
		var clicked = false
		composeRule.setContent {
			SorterTheme {
				TutorialScreen(
					onGetStarted = { clicked = true }
				)
			}
		}

		composeRule.onNodeWithTag("TutorialGetStartedButton").performClick()
		composeRule.runOnIdle {
			assert(clicked)
		}
	}
}
