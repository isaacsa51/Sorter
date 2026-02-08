package com.serranoie.app.media.sorter.presentation.review

import android.app.PendingIntent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import com.serranoie.app.media.sorter.presentation.ui.theme.SorterTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ReviewScreenTest {

	@get:Rule
	val composeRule = createAndroidComposeRule<ComponentActivity>()

	private val fakeRepository = object : MediaRepository {
		override suspend fun fetchMediaFiles(): Result<List<MediaFile>> = Result.Success(emptyList())
		override suspend fun getMediaByFolder(): Result<Map<String, List<MediaFile>>> = Result.Success(emptyMap())
		override suspend fun getMediaGroupedByDate(): Result<Map<LocalDate, List<MediaFile>>> = Result.Success(emptyMap())
		override suspend fun getMediaGroupedByDateFiltered(): Result<Map<LocalDate, List<MediaFile>>> = Result.Success(emptyMap())
		override suspend fun deleteMedia(uri: Uri): Result<Boolean> = Result.Success(true)
		override suspend fun deleteMultipleMedia(uris: List<Uri>): Result<Int> = Result.Success(uris.size)
		override fun createDeletionRequest(uris: List<Uri>, useTrash: Boolean): PendingIntent? = null
		override fun clearCache() {}
		override suspend fun markAsViewed(mediaId: Long) {}
		override suspend fun isViewed(mediaId: Long): Boolean = false
		override suspend fun clearViewedHistory() {}
		override suspend fun getViewedCount(): Int = 0
	}

	private fun dummyFile(id: String = "1") = MediaFileUi(
		id = id,
		fileName = "File_$id.jpg",
		fileInfo = "1.0 MB • Today",
		mediaType = "image",
		date = "Today",
		fileSize = "1.0 MB",
		dimensions = "100x100",
		dateCreated = "Today",
		lastAccessed = "Today",
		modified = "Today",
		path = "/"
	)

	@Test
	fun emptyState_is_shown_when_no_deleted_files() {
		composeRule.setContent {
			SorterTheme {
				ReviewScreen(
					deletedFiles = emptyList(),
					repository = fakeRepository,
					useTrash = false
				)
			}
		}

		composeRule.onNodeWithTag("ReviewScreen").assertExists()
		composeRule.onNodeWithTag("ReviewEmptyState").assertIsDisplayed()
		composeRule.onNodeWithTag("ReviewDeleteAllFab").assertDoesNotExist()
	}

	@Test
	fun deleteAll_flow_shows_dialog_cancel_dismisses_and_confirm_calls_callback() {
		var deleteAllCalls = 0

		composeRule.setContent {
			SorterTheme {
				ReviewScreen(
					deletedFiles = listOf(dummyFile("1"), dummyFile("2")),
					repository = fakeRepository,
					useTrash = false,
					onDeleteAll = { deleteAllCalls++ }
				)
			}
		}

		composeRule.onNodeWithTag("ReviewGrid").assertIsDisplayed()
		composeRule.onNodeWithTag("ReviewDeleteAllFab").performClick()
		composeRule.onNodeWithTag("ReviewDeleteAllDialog").assertExists()

		composeRule.onNodeWithTag("ReviewDeleteAllCancel").performClick()
		composeRule.onNodeWithTag("ReviewDeleteAllDialog").assertDoesNotExist()

		composeRule.onNodeWithTag("ReviewDeleteAllFab").performClick()
		composeRule.onNodeWithTag("ReviewDeleteAllConfirm").performClick()

		composeRule.waitUntil(timeoutMillis = 5_000) { deleteAllCalls == 1 }
	}

	@Test
	fun doubleTap_on_grid_item_opens_fullscreen_and_close_dismisses() {
		val file = dummyFile("42")

		composeRule.setContent {
			SorterTheme {
				ReviewScreen(
					deletedFiles = listOf(file),
					repository = fakeRepository,
					useTrash = false
				)
			}
		}

		composeRule.onNodeWithTag("ReviewGridItem_${file.id}")
			.performTouchInput { doubleClick() }

		composeRule.onNodeWithTag("ReviewFullscreenViewer").assertExists()
		composeRule.onNodeWithTag("ReviewFullscreenClose").performClick()
		composeRule.onNodeWithTag("ReviewFullscreenViewer").assertDoesNotExist()
	}
}
