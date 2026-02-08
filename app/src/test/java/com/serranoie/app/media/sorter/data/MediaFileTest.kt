package com.serranoie.app.media.sorter.data

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate

class MediaFileTest {

	@Test
	fun `mediaFile_createsInstance_withAllProperties`() {
		// Arrange
		val uri = mockk<Uri>()
		val previewUri = mockk<Uri>()
		val fileDate = LocalDate.of(2024, 1, 15)
		val dateTaken = 1705276800000L

		// Act
		val mediaFile = MediaFile(
			uri = uri,
			mediaType = "image",
			extension = "jpg",
			fileName = "test.jpg",
			folderName = "Camera",
			fileSize = 1024000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = dateTaken
		)

		// Assert
		assertEquals(uri, mediaFile.uri)
		assertEquals("image", mediaFile.mediaType)
		assertEquals("jpg", mediaFile.extension)
		assertEquals("test.jpg", mediaFile.fileName)
		assertEquals("Camera", mediaFile.folderName)
		assertEquals(1024000L, mediaFile.fileSize)
		assertEquals(fileDate, mediaFile.fileDate)
		assertEquals(previewUri, mediaFile.previewUri)
		assertEquals(dateTaken, mediaFile.dateTaken)
	}

	@Test
	fun `mediaFile_videoType_hasCorrectExtension`() {
		// Arrange
		val uri = mockk<Uri>()
		val previewUri = mockk<Uri>()

		// Act
		val mediaFile = MediaFile(
			uri = uri,
			mediaType = "video",
			extension = "mp4",
			fileName = "video.mp4",
			folderName = "Videos",
			fileSize = 5120000L,
			fileDate = LocalDate.now(),
			previewUri = previewUri,
			dateTaken = System.currentTimeMillis()
		)

		// Assert
		assertEquals("video", mediaFile.mediaType)
		assertEquals("mp4", mediaFile.extension)
	}

	@Test
	fun `mediaFile_equals_returnsTrueForSameData`() {
		// Arrange
		val uri = mockk<Uri>()
		val previewUri = mockk<Uri>()
		val fileDate = LocalDate.of(2024, 1, 15)
		val dateTaken = 1705276800000L

		val mediaFile1 = MediaFile(
			uri = uri,
			mediaType = "image",
			extension = "jpg",
			fileName = "test.jpg",
			folderName = "Camera",
			fileSize = 1024000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = dateTaken
		)

		val mediaFile2 = MediaFile(
			uri = uri,
			mediaType = "image",
			extension = "jpg",
			fileName = "test.jpg",
			folderName = "Camera",
			fileSize = 1024000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = dateTaken
		)

		// Act & Assert
		assertEquals(mediaFile1, mediaFile2)
	}

	@Test
	fun `mediaFile_equals_returnsFalseForDifferentData`() {
		// Arrange
		val uri1 = mockk<Uri>()
		val uri2 = mockk<Uri>()
		every { uri1.toString() } returns "content://media/1"
		every { uri2.toString() } returns "content://media/2"

		val previewUri = mockk<Uri>()
		val fileDate = LocalDate.of(2024, 1, 15)

		val mediaFile1 = MediaFile(
			uri = uri1,
			mediaType = "image",
			extension = "jpg",
			fileName = "test1.jpg",
			folderName = "Camera",
			fileSize = 1024000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = 1705276800000L
		)

		val mediaFile2 = MediaFile(
			uri = uri2,
			mediaType = "image",
			extension = "jpg",
			fileName = "test2.jpg",
			folderName = "Camera",
			fileSize = 2048000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = 1705363200000L
		)

		// Act & Assert
		assertNotEquals(mediaFile1, mediaFile2)
	}

	@Test
	fun `mediaFile_copy_createsNewInstanceWithChangedProperty`() {
		// Arrange
		val uri = mockk<Uri>()
		val previewUri = mockk<Uri>()
		val fileDate = LocalDate.of(2024, 1, 15)

		val original = MediaFile(
			uri = uri,
			mediaType = "image",
			extension = "jpg",
			fileName = "test.jpg",
			folderName = "Camera",
			fileSize = 1024000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = 1705276800000L
		)

		// Act
		val copied = original.copy(fileName = "renamed.jpg")

		// Assert
		assertEquals("renamed.jpg", copied.fileName)
		assertEquals(original.uri, copied.uri)
		assertEquals(original.mediaType, copied.mediaType)
		assertNotEquals(original, copied)
	}

	@Test
	fun `mediaFile_hashCode_sameForEqualObjects`() {
		// Arrange
		val uri = mockk<Uri>()
		val previewUri = mockk<Uri>()
		val fileDate = LocalDate.of(2024, 1, 15)
		val dateTaken = 1705276800000L

		val mediaFile1 = MediaFile(
			uri = uri,
			mediaType = "image",
			extension = "jpg",
			fileName = "test.jpg",
			folderName = "Camera",
			fileSize = 1024000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = dateTaken
		)

		val mediaFile2 = MediaFile(
			uri = uri,
			mediaType = "image",
			extension = "jpg",
			fileName = "test.jpg",
			folderName = "Camera",
			fileSize = 1024000L,
			fileDate = fileDate,
			previewUri = previewUri,
			dateTaken = dateTaken
		)

		// Act & Assert
		assertEquals(mediaFile1.hashCode(), mediaFile2.hashCode())
	}

	@Test
	fun `mediaFile_withLargeFileSize_handlesCorrectly`() {
		// Arrange
		val uri = mockk<Uri>()
		val previewUri = mockk<Uri>()
		val largeSize = Long.MAX_VALUE

		// Act
		val mediaFile = MediaFile(
			uri = uri,
			mediaType = "video",
			extension = "mp4",
			fileName = "large.mp4",
			folderName = "Videos",
			fileSize = largeSize,
			fileDate = LocalDate.now(),
			previewUri = previewUri,
			dateTaken = System.currentTimeMillis()
		)

		// Assert
		assertEquals(largeSize, mediaFile.fileSize)
	}

	@Test
	fun `mediaFile_withEmptyFolderName_createsSuccessfully`() {
		// Arrange
		val uri = mockk<Uri>()
		val previewUri = mockk<Uri>()

		// Act
		val mediaFile = MediaFile(
			uri = uri,
			mediaType = "image",
			extension = "jpg",
			fileName = "test.jpg",
			folderName = "",
			fileSize = 1024L,
			fileDate = LocalDate.now(),
			previewUri = previewUri,
			dateTaken = System.currentTimeMillis()
		)

		// Assert
		assertEquals("", mediaFile.folderName)
	}
}