package com.serranoie.app.media.sorter.presentation.sorter

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SorterMediaScreenTest {

	@get:Rule
	val composeRule = createAndroidComposeRule<ComponentActivity>()

	private fun launchScreen(
		isZoomed: Boolean = false,
		isCompleted: Boolean = false,
		deletedCount: Int = 0,
		file: MediaFileUi? = dummyImageFile(),
	) {
		composeRule.setContent {
			SorterMediaScreen(
				currentFile = file,
				isCompleted = isCompleted,
				deletedCount = deletedCount,
				useBlurredBackground = true,
				autoPlayVideos = false,
				onKeepCurrent = {},
				onTrashCurrent = { file },
				onUndoTrash = {},
				onBackToOnboarding = {},
				onNavigateToReview = {},
				onNavigateToSettings = {}
			)
		}
		if (isZoomed) composeRule.runOnIdle {
			composeRule.onNodeWithTag("ZoomOverlay", useUnmergedTree = true).assertExists()
		}
	}

	@Test
	fun completionScreen_shows_when_completed() {
		launchScreen(isCompleted = true, deletedCount = 2)
		composeRule.onNodeWithTag("CompletionScreen").assertIsDisplayed()
		// Verify the review button exists (deletedCount > 0)
		composeRule.onNodeWithText("2", substring = true).assertExists()
	}

	@Test
	fun normalScreen_shows_zoomable_media_and_info_overlay() {
		launchScreen(isCompleted = false)
		composeRule.onNodeWithTag("MediaFileName", useUnmergedTree = true).assertExists()
		composeRule.onAllNodesWithText("Beach_Sunset_01.jpg", ignoreCase = true, substring = true)
			.assertCountEquals(2) // file name in card + info overlay
		composeRule.onNodeWithTag("CompletionScreen").assertDoesNotExist()
	}

	@Test
	fun zoomOverlay_not_shown_when_not_zoomed() {
		launchScreen(isZoomed = false)
		composeRule.onNodeWithTag("ZoomOverlay", useUnmergedTree = true).assertDoesNotExist()
	}

	@Test
	fun backPress_navigates_when_not_zoomed() {
		launchScreen(isZoomed = false)
		// Simulate back press
		composeRule.activityRule.scenario.onActivity {
			it.onBackPressedDispatcher.onBackPressed()
		}
		// TODO: assert navigation, e.g. callback tracked (use mock in real test)
	}

	@After
	fun tearDown() {
		// Clean up if needed
	}

	companion object {
		fun dummyImageFile() = MediaFileUi(
			id = "1",
			fileName = "Beach_Sunset_01.jpg",
			fileInfo = "2.5 MB • Yesterday",
			mediaType = "image",
			date = "Yesterday",
			fileSize = "2.5 MB",
			dimensions = "4032x3024",
			dateCreated = "2025-01-08 10:30 AM",
			lastAccessed = "2025-01-09 09:15 AM",
			modified = "Yesterday",
			path = "/photos/beach/",
		)
	}
}
