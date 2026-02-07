package com.serranoie.app.media.sorter.data.datasource

import android.app.PendingIntent
import android.app.RecoverableSecurityException
import android.app.RemoteAction
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.serranoie.app.media.sorter.domain.AppError
import com.serranoie.app.media.sorter.domain.Result
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class AndroidMediaDataSourceTest {

	private lateinit var mockContext: Context
	private lateinit var mockContentResolver: ContentResolver
	private lateinit var mockCursor: Cursor
	private lateinit var androidMediaDataSource: AndroidMediaDataSource

	private val testDispatcher = StandardTestDispatcher()

	@OptIn(ExperimentalCoroutinesApi::class)
	@Before
	fun setUp() {
		Dispatchers.setMain(testDispatcher)
		
		mockContext = mockk(relaxed = true)
		mockContentResolver = mockk(relaxed = true)
		mockCursor = mockk(relaxed = true)

		every { mockContext.contentResolver } returns mockContentResolver

		// Mock static methods
		mockkStatic(Log::class)
		mockkStatic(ContentUris::class)
		mockkStatic(MediaStore::class)
		mockkStatic(Build.VERSION::class)
		
		// Mock Log methods to avoid "not mocked" errors
		every { Log.d(any(), any()) } returns 0
		every { Log.e(any(), any()) } returns 0
		every { Log.e(any(), any(), any()) } returns 0

		androidMediaDataSource = AndroidMediaDataSource(mockContext)
	}

	@After
	fun tearDown() {
		Dispatchers.resetMain()
		unmockkAll()
		clearStaticMockk(Log::class)
		clearStaticMockk(ContentUris::class)
		clearStaticMockk(MediaStore::class)
		clearStaticMockk(Build.VERSION::class)
	}

	@Test
	fun `DEBUG_fetchImages`() = runTest {
		// Arrange - Very simple setup
		val mockUri = mockk<Uri>(relaxed = true)
		every { ContentUris.withAppendedId(any(), any()) } returns mockUri
		
		val testCursor = mockk<Cursor>(relaxed = true)
		every { testCursor.moveToNext() } returnsMany listOf(true, false)
		every { testCursor.getColumnIndexOrThrow(any()) } returns 0
		every { testCursor.getLong(any()) } returns 1000L
		every { testCursor.getString(any()) } returns "test.jpg"
		every { testCursor.getInt(any()) } returns 100
		every { testCursor.close() } returns Unit
		
		every { mockContentResolver.query(any<Uri>(), any(), any(), any(), any()) } returns testCursor
		
		// Act
		val result = androidMediaDataSource.fetchImages()
		
		// Debug
		println("DEBUG Result type: ${result::class.simpleName}")
		when (result) {
			is Result.Success -> println("DEBUG Success with ${result.data.size} items")
			is Result.Error -> println("DEBUG Error: ${result.error}")
			is Result.Loading -> println("DEBUG Loading")
		}
		
		// Assert
		assertTrue("Expected Success but got ${result::class.simpleName}", result is Result.Success<*>)
	}

	@Test
	fun `fetchImages_returnsSuccess_whenCursorHasData`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)
		every { ContentUris.withAppendedId(any(), any()) } returns mockUri

		setupCursorWithData(mockCursor, listOf(
			mapOf(
				"id" to 1L,
				"name" to "test.jpg",
				"size" to 1024L,
				"dateTaken" to 1609459200000L,
				"dateAdded" to 1609459200L,
				"dateModified" to 1609459200L,
				"bucket" to "Camera"
			)
		))

		every {
			mockContentResolver.query(
				any<Uri>(),
				any(),
				any(),
				any(),
				any()
			)
		} returns mockCursor

		// Act
		val result = androidMediaDataSource.fetchImages()

		// Assert
		assertTrue(result is Result.Success<*>)
		val mediaList = (result as Result.Success).data
		assertEquals(1, mediaList.size)
		assertEquals("test.jpg", mediaList[0].fileName)
		assertEquals("image", mediaList[0].mediaType)
	}

	@Test
	fun `fetchImages_returnsNoMediaFoundError_whenCursorIsEmpty`() = runTest {
		// Arrange
		setupCursorWithData(mockCursor, emptyList())
		every {
			mockContentResolver.query(
				any<Uri>(),
				any(),
				any(),
				any(),
				any()
			)
		} returns mockCursor

		// Act
		val result = androidMediaDataSource.fetchImages()

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.NoMediaFoundError)
	}

	@Test
	fun `fetchImages_returnsPermissionError_whenSecurityExceptionOccurs`() = runTest {
		// Arrange
		every {
			mockContentResolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				any(),
				null,
				null,
				any()
			)
		} throws SecurityException("Permission denied")

		// Act
		val result = androidMediaDataSource.fetchImages()

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.PermissionError)
	}

	@Test
	fun `fetchImages_returnsMediaLoadError_whenGeneralExceptionOccurs`() = runTest {
		// Arrange
		every {
			mockContentResolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				any(),
				null,
				null,
				any()
			)
		} throws RuntimeException("Database error")

		// Act
		val result = androidMediaDataSource.fetchImages()

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.MediaLoadError)
		assertTrue((result.error as AppError.MediaLoadError).details?.contains("Failed to load images") == true)
	}

	@Test
	fun `fetchImages_handlesMultipleImages_correctly`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)
		every { ContentUris.withAppendedId(any(), any()) } returns mockUri

		setupCursorWithData(mockCursor, listOf(
			mapOf(
				"id" to 1L,
				"name" to "image1.jpg",
				"size" to 1024L,
				"dateTaken" to 1609459200000L,
				"dateAdded" to 1609459200L,
				"dateModified" to 1609459200L,
				"bucket" to "Camera"
			),
			mapOf(
				"id" to 2L,
				"name" to "image2.png",
				"size" to 2048L,
				"dateTaken" to 1609545600000L,
				"dateAdded" to 1609545600L,
				"dateModified" to 1609545600L,
				"bucket" to "Screenshots"
			)
		))

		every {
			mockContentResolver.query(
				any<Uri>(),
				any(),
				any(),
				any(),
				any()
			)
		} returns mockCursor

		// Act
		val result = androidMediaDataSource.fetchImages()

		// Assert
		assertTrue(result is Result.Success<*>)
		val mediaList = (result as Result.Success).data
		assertEquals(2, mediaList.size)
	}

	@Test
	fun `fetchVideos_returnsSuccess_whenCursorHasData`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)
		every { ContentUris.withAppendedId(any(), any()) } returns mockUri

		setupCursorWithData(mockCursor, listOf(
			mapOf(
				"id" to 2L,
				"name" to "video.mp4",
				"size" to 5120L,
				"dateTaken" to 1609459200000L,
				"dateAdded" to 1609459200L,
				"dateModified" to 1609459200L,
				"bucket" to "Videos"
			)
		))

		every {
			mockContentResolver.query(
				any<Uri>(),
				any(),
				any(),
				any(),
				any()
			)
		} returns mockCursor

		// Act
		val result = androidMediaDataSource.fetchVideos()

		// Assert
		assertTrue(result is Result.Success<*>)
		val mediaList = (result as Result.Success).data
		assertEquals(1, mediaList.size)
		assertEquals("video.mp4", mediaList[0].fileName)
		assertEquals("video", mediaList[0].mediaType)
	}

	@Test
	fun `fetchVideos_returnsNoMediaFoundError_whenCursorIsEmpty`() = runTest {
		// Arrange
		setupCursorWithData(mockCursor, emptyList())
		every {
			mockContentResolver.query(
				any<Uri>(),
				any(),
				any(),
				any(),
				any()
			)
		} returns mockCursor

		// Act
		val result = androidMediaDataSource.fetchVideos()

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.NoMediaFoundError)
	}

	@Test
	fun `fetchVideos_returnsPermissionError_whenSecurityExceptionOccurs`() = runTest {
		// Arrange
		every {
			mockContentResolver.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				any(),
				null,
				null,
				any()
			)
		} throws SecurityException("Permission denied")

		// Act
		val result = androidMediaDataSource.fetchVideos()

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.PermissionError)
	}

	@Test
	fun `fetchVideos_returnsMediaLoadError_whenGeneralExceptionOccurs`() = runTest {
		// Arrange
		every {
			mockContentResolver.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				any(),
				null,
				null,
				any()
			)
		} throws RuntimeException("Database error")

		// Act
		val result = androidMediaDataSource.fetchVideos()

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.MediaLoadError)
		assertTrue((result.error as AppError.MediaLoadError).details?.contains("Failed to load videos") == true)
	}

	@Test
	fun `fetchMediaByUri_returnsUnknownError_asNotImplemented`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)

		// Act
		val result = androidMediaDataSource.fetchMediaByUri(mockUri)

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.UnknownError)
		assertEquals("Not implemented", (result.error as AppError.UnknownError).message)
	}

	// deleteMedia() Tests

	@Test
	fun `deleteMedia_returnsSuccess_whenFileIsDeleted`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)
		every { mockContentResolver.delete(mockUri, null, null) } returns 1

		// Act
		val result = androidMediaDataSource.deleteMedia(mockUri)

		// Assert
		assertTrue(result is Result.Success<*>)
		assertTrue((result as Result.Success).data)
	}

	@Test
	fun `deleteMedia_returnsError_whenNoRowsDeleted`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)
		every { mockContentResolver.delete(mockUri, null, null) } returns 0

		// Act
		val result = androidMediaDataSource.deleteMedia(mockUri)

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.UnknownError)
		assertEquals("Failed to delete file", (result.error as AppError.UnknownError).message)
	}

	@Test
	fun `deleteMedia_returnsPermissionError_whenSecurityExceptionOccurs`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)
		every { mockContentResolver.delete(mockUri, null, null) } throws SecurityException("Permission denied")

		// Act
		val result = androidMediaDataSource.deleteMedia(mockUri)

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.PermissionError)
	}

	@Test
	fun `deleteMedia_returnsUnknownError_whenGeneralExceptionOccurs`() = runTest {
		// Arrange
		val mockUri = mockk<Uri>(relaxed = true)
		every { mockContentResolver.delete(mockUri, null, null) } throws RuntimeException("IO error")

		// Act
		val result = androidMediaDataSource.deleteMedia(mockUri)

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.UnknownError)
		assertTrue((result.error as AppError.UnknownError).message.contains("Error deleting file"))
	}

	@Test
	fun `deleteMultipleMedia_returnsSuccessCount_whenAllFilesDeleted`() = runTest {
		// Arrange
		val uris = listOf(
			mockk<Uri>(relaxed = true),
			mockk<Uri>(relaxed = true),
			mockk<Uri>(relaxed = true)
		)
		every { mockContentResolver.delete(any(), null, null) } returns 1

		// Act
		val result = androidMediaDataSource.deleteMultipleMedia(uris)

		// Assert
		assertTrue(result is Result.Success<*>)
		assertEquals(3, (result as Result.Success).data)
	}

	@Test
	fun `deleteMultipleMedia_returnsPartialSuccess_whenSomeFilesDeleted`() = runTest {
		// Arrange
		val uri1 = mockk<Uri>(relaxed = true)
		val uri2 = mockk<Uri>(relaxed = true)
		val uri3 = mockk<Uri>(relaxed = true)
		val uris = listOf(uri1, uri2, uri3)

		every { mockContentResolver.delete(uri1, null, null) } returns 1
		every { mockContentResolver.delete(uri2, null, null) } returns 0
		every { mockContentResolver.delete(uri3, null, null) } returns 1

		// Act
		val result = androidMediaDataSource.deleteMultipleMedia(uris)

		// Assert
		assertTrue(result is Result.Success<*>)
		assertEquals(2, (result as Result.Success).data)
	}

	@Test
	fun `deleteMultipleMedia_returnsPermissionError_whenAllNeedPermission`() = runTest {
		// Arrange
		val uris = listOf(
			mockk<Uri>(relaxed = true),
			mockk<Uri>(relaxed = true)
		)
		every { mockContentResolver.delete(any(), null, null) } throws
				RecoverableSecurityException(SecurityException("Permission needed"), "Permission needed", mockk<RemoteAction>(relaxed = true))

		// Act
		val result = androidMediaDataSource.deleteMultipleMedia(uris)

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.PermissionError)
	}

	@Test
	fun `deleteMultipleMedia_handlesMixedExceptions_correctly`() = runTest {
		// Arrange
		val uri1 = mockk<Uri>(relaxed = true)
		val uri2 = mockk<Uri>(relaxed = true)
		val uri3 = mockk<Uri>(relaxed = true)
		val uris = listOf(uri1, uri2, uri3)

		every { mockContentResolver.delete(uri1, null, null) } returns 1
		every { mockContentResolver.delete(uri2, null, null) } throws SecurityException("Denied")
		every { mockContentResolver.delete(uri3, null, null) } throws RuntimeException("Error")

		// Act
		val result = androidMediaDataSource.deleteMultipleMedia(uris)

		// Assert
		assertTrue(result is Result.Success<*>)
		assertEquals(1, (result as Result.Success).data)
	}

	@Test
	fun `deleteMultipleMedia_returnsError_whenOuterSecurityExceptionThrown`() = runTest {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		every { mockContentResolver.delete(any(), null, null) } throws SecurityException("Permission denied")

		// Act
		val result = androidMediaDataSource.deleteMultipleMedia(uris)

		// Assert
		assertTrue(result is Result.Error)
		assertTrue((result as Result.Error).error is AppError.PermissionError)
	}

	@Test
	fun `deleteMultipleMedia_handlesEmptyList_correctly`() = runTest {
		// Arrange
		val uris = emptyList<Uri>()

		// Act
		val result = androidMediaDataSource.deleteMultipleMedia(uris)

		// Assert
		assertTrue(result is Result.Success<*>)
		assertEquals(0, (result as Result.Success).data)
	}

	@Test
	fun `createDeleteRequest_returnsPendingIntent_onAndroid11Plus`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.R
		every { MediaStore.createDeleteRequest(any(), any()) } returns mockPendingIntent

		// Act
		val result = androidMediaDataSource.createDeleteRequest(uris)

		// Assert
		assertNotNull(result)
		assertEquals(mockPendingIntent, result)
	}

	@Test
	fun `createDeleteRequest_returnsPendingIntent_onAndroid10`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.Q
		every { MediaStore.createDeleteRequest(any(), any()) } returns mockPendingIntent

		// Act
		val result = androidMediaDataSource.createDeleteRequest(uris)

		// Assert
		assertNotNull(result)
	}

	@Test
	fun `createDeleteRequest_returnsNull_onAndroidBelow10`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.P

		// Act
		val result = androidMediaDataSource.createDeleteRequest(uris)

		// Assert
		assertNull(result)
	}

	@Test
	fun `createTrashRequest_returnsPendingIntent_onAndroid11Plus`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.R
		every { MediaStore.createTrashRequest(any(), any(), any()) } returns mockPendingIntent

		// Act
		val result = androidMediaDataSource.createTrashRequest(uris)

		// Assert
		assertNotNull(result)
		assertEquals(mockPendingIntent, result)
	}

	@Test
	fun `createTrashRequest_returnsNull_whenExceptionOccurs`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.R
		every { MediaStore.createTrashRequest(any(), any(), any()) } throws RuntimeException("Error")

		// Act
		val result = androidMediaDataSource.createTrashRequest(uris)

		// Assert
		assertNull(result)
	}

	@Test
	fun `createTrashRequest_returnsNull_onAndroidBelow11`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.Q

		// Act
		val result = androidMediaDataSource.createTrashRequest(uris)

		// Assert
		assertNull(result)
	}

	@Test
	fun `createDeletionRequest_usesTrash_onAndroid11PlusWhenUseTrashTrue`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.R
		every { MediaStore.createTrashRequest(any(), any(), any()) } returns mockPendingIntent

		// Act
		val result = androidMediaDataSource.createDeletionRequest(uris, useTrash = true)

		// Assert
		assertNotNull(result)
		assertEquals(mockPendingIntent, result)
	}

	@Test
	fun `createDeletionRequest_fallsBackToDelete_whenTrashFails`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.R
		every { MediaStore.createTrashRequest(any(), any(), any()) } throws RuntimeException("Trash failed")
		every { MediaStore.createDeleteRequest(any(), any()) } returns mockPendingIntent

		// Act
		val result = androidMediaDataSource.createDeletionRequest(uris, useTrash = true)

		// Assert
		assertNotNull(result)
		assertEquals(mockPendingIntent, result)
	}

	@Test
	fun `createDeletionRequest_usesDelete_whenUseTrashFalse`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.R
		every { MediaStore.createDeleteRequest(any(), any()) } returns mockPendingIntent

		// Act
		val result = androidMediaDataSource.createDeletionRequest(uris, useTrash = false)

		// Assert
		assertNotNull(result)
	}

	@Test
	fun `createDeletionRequest_returnsNull_whenBothTrashAndDeleteFail`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		// Set SDK to P (Android 9), which is too old for both trash and delete requests
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.P

		// Act
		val result = androidMediaDataSource.createDeletionRequest(uris, useTrash = true)

		// Assert
		assertNull(result)
	}

	@Test
	fun `createDeletionRequest_onOlderAndroid_usesDeleteRequest`() {
		// Arrange
		val uris = listOf(mockk<Uri>(relaxed = true))
		val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
		every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.Q
		every { MediaStore.createDeleteRequest(any(), any()) } returns mockPendingIntent

		// Act
		val result = androidMediaDataSource.createDeletionRequest(uris, useTrash = true)

		// Assert
		assertNotNull(result)
	}

	// Helper function to setup cursor with data
	private fun setupCursorWithData(cursor: Cursor, dataList: List<Map<String, Any>>) {
		var currentIndex = -1

		every { cursor.moveToNext() } answers {
			currentIndex++
			currentIndex < dataList.size
		}

		// Map column names to indices
		every { cursor.getColumnIndexOrThrow(any()) } answers {
			when (firstArg<String>()) {
				MediaStore.Images.Media._ID, MediaStore.Video.Media._ID -> 0
				MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Video.Media.DISPLAY_NAME -> 1
				MediaStore.Images.Media.SIZE, MediaStore.Video.Media.SIZE -> 2
				MediaStore.Images.Media.DATE_TAKEN, MediaStore.Video.Media.DATE_TAKEN -> 3
				MediaStore.Images.Media.DATE_ADDED, MediaStore.Video.Media.DATE_ADDED -> 4
				MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Video.Media.DATE_MODIFIED -> 5
				MediaStore.Images.Media.DATA, MediaStore.Video.Media.DATA -> 6
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_DISPLAY_NAME -> 7
				MediaStore.Images.Media.WIDTH, MediaStore.Video.Media.WIDTH -> 8
				MediaStore.Images.Media.HEIGHT, MediaStore.Video.Media.HEIGHT -> 9
				else -> -1
			}
		}
		
		every { cursor.getLong(any()) } answers {
			if (currentIndex >= 0 && currentIndex < dataList.size) {
				when (firstArg<Int>()) {
					0 -> dataList[currentIndex]["id"] as? Long ?: 0L
					2 -> dataList[currentIndex]["size"] as? Long ?: 0L
					3 -> dataList[currentIndex]["dateTaken"] as? Long ?: 0L
					4 -> dataList[currentIndex]["dateAdded"] as? Long ?: 0L
					5 -> dataList[currentIndex]["dateModified"] as? Long ?: 0L
					else -> 0L
				}
			} else 0L
		}
		
		every { cursor.getString(any()) } answers {
			if (currentIndex >= 0 && currentIndex < dataList.size) {
				when (firstArg<Int>()) {
					1 -> dataList[currentIndex]["name"] as? String
					6 -> dataList[currentIndex]["path"] as? String
					7 -> dataList[currentIndex]["bucket"] as? String
					else -> null
				}
			} else null
		}
		
		every { cursor.getInt(any()) } answers {
			if (currentIndex >= 0 && currentIndex < dataList.size) {
				when (firstArg<Int>()) {
					8 -> dataList[currentIndex]["width"] as? Int ?: 0
					9 -> dataList[currentIndex]["height"] as? Int ?: 0
					else -> 0
				}
			} else 0
		}

		every { cursor.close() } returns Unit
	}
}
