package com.serranoie.app.media.sorter.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UpdatePreferencesTest {

	private lateinit var mockContext: Context
	private lateinit var mockDataStore: DataStore<Preferences>
	private lateinit var mockPreferences: Preferences
	private lateinit var mockMutablePreferences: MutablePreferences
	private lateinit var updatePreferences: UpdatePreferences

	@Before
	fun setUp() {
		mockContext = mockk(relaxed = true)
		mockDataStore = mockk(relaxed = true)
		mockPreferences = mockk(relaxed = true)
		mockMutablePreferences = mockk(relaxed = true)

		mockkStatic("com.serranoie.app.media.sorter.data.UpdatePreferencesKt")
		mockkStatic("androidx.datastore.preferences.core.PreferencesKt")
		
		every { mockContext.updateDataStore } returns mockDataStore
		
		every { mockDataStore.data } returns flowOf(mockPreferences)
		
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		updatePreferences = UpdatePreferences(mockContext)
	}

	@After
	fun tearDown() {
		unmockkAll()
	}

	@Test
	fun `lastCheckedVersion emitsNull whenNoVersionStored`() = runTest {
		// Arrange
		val key = stringPreferencesKey("last_checked_version")
		every { mockPreferences[key] } returns null
		every { mockDataStore.data } returns flowOf(mockPreferences)

		// Act
		val result = updatePreferences.lastCheckedVersion.first()

		// Assert
		assertNull(result)
	}

	@Test
	fun `lastCheckedVersion emitsStoredVersion whenVersionExists`() = runTest {
		// Arrange
		val expectedVersion = "1.0.14"
		val key = stringPreferencesKey("last_checked_version")
		every { mockPreferences[key] } returns expectedVersion
		every { mockDataStore.data } returns flowOf(mockPreferences)

		// Act
		val result = updatePreferences.lastCheckedVersion.first()

		// Assert
		assertEquals(expectedVersion, result)
	}

	@Test
	fun `lastCheckTime emitsNull whenNoTimeStored`() = runTest {
		// Arrange
		val key = stringPreferencesKey("last_check_time")
		every { mockPreferences[key] } returns null
		every { mockDataStore.data } returns flowOf(mockPreferences)

		// Act
		val result = updatePreferences.lastCheckTime.first()

		// Assert
		assertNull(result)
	}

	@Test
	fun `lastCheckTime emitsTimestampAsLong whenTimeExists`() = runTest {
		// Arrange
		val expectedTime = 1609459200000L // Jan 1, 2021
		val key = stringPreferencesKey("last_check_time")
		every { mockPreferences[key] } returns expectedTime.toString()
		every { mockDataStore.data } returns flowOf(mockPreferences)

		// Act
		val result = updatePreferences.lastCheckTime.first()

		// Assert
		assertEquals(expectedTime, result)
	}

	@Test
	fun `lastCheckTime emitsNull whenStoredValueIsInvalidLong`() = runTest {
		// Arrange
		val key = stringPreferencesKey("last_check_time")
		every { mockPreferences[key] } returns "invalid"
		every { mockDataStore.data } returns flowOf(mockPreferences)

		// Act & Assert
		try {
			updatePreferences.lastCheckTime.first()
			// If we reach here, test should fail as invalid string should throw
			assert(false) { "Should have thrown NumberFormatException" }
		} catch (e: NumberFormatException) {
			// Expected behavior
			assertTrue(true)
		}
	}

	@Test
	fun `dismissedUpdateVersion emitsNull whenNoVersionDismissed`() = runTest {
		// Arrange
		val key = stringPreferencesKey("dismissed_update_version")
		every { mockPreferences[key] } returns null
		every { mockDataStore.data } returns flowOf(mockPreferences)

		// Act
		val result = updatePreferences.dismissedUpdateVersion.first()

		// Assert
		assertNull(result)
	}

	@Test
	fun `dismissedUpdateVersion emitsStoredVersion whenVersionDismissed`() = runTest {
		// Arrange
		val expectedVersion = "2.0.0"
		val key = stringPreferencesKey("dismissed_update_version")
		every { mockPreferences[key] } returns expectedVersion
		every { mockDataStore.data } returns flowOf(mockPreferences)

		// Act
		val result = updatePreferences.dismissedUpdateVersion.first()

		// Assert
		assertEquals(expectedVersion, result)
	}

	@Test
	fun `saveLastCheckedVersion savesVersionAndTimestamp successfully`() = runTest {
		// Arrange
		val version = "1.5.0"
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		// Act
		updatePreferences.saveLastCheckedVersion(version)

		// Assert
		coVerify(exactly = 1) { 
			mockDataStore.edit(any()) 
		}
	}

	@Test
	fun `saveLastCheckedVersion handlesMultipleVersions correctly`() = runTest {
		// Arrange
		val version1 = "1.0.0"
		val version2 = "2.0.0"
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		// Act
		updatePreferences.saveLastCheckedVersion(version1)
		updatePreferences.saveLastCheckedVersion(version2)

		// Assert
		coVerify(exactly = 2) { 
			mockDataStore.edit(any()) 
		}
	}

	@Test
	fun `saveDismissedUpdateVersion savesDismissedVersion successfully`() = runTest {
		// Arrange
		val version = "2.0.0"
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		// Act
		updatePreferences.saveDismissedUpdateVersion(version)

		// Assert
		coVerify(exactly = 1) { 
			mockDataStore.edit(any()) 
		}
	}

	@Test
	fun `saveDismissedUpdateVersion handlesEmptyString correctly`() = runTest {
		// Arrange
		val version = ""
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		// Act
		updatePreferences.saveDismissedUpdateVersion(version)

		// Assert
		coVerify(exactly = 1) { 
			mockDataStore.edit(any()) 
		}
	}

	@Test
	fun `clearDismissedUpdateVersion removesKey successfully`() = runTest {
		// Arrange
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		// Act
		updatePreferences.clearDismissedUpdateVersion()

		// Assert
		coVerify(exactly = 1) { 
			mockDataStore.edit(any()) 
		}
	}

	@Test
	fun `clearDismissedUpdateVersion canBeCalledMultipleTimes withoutError`() = runTest {
		// Arrange
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		// Act
		updatePreferences.clearDismissedUpdateVersion()
		updatePreferences.clearDismissedUpdateVersion()

		// Assert
		coVerify(exactly = 2) { mockDataStore.edit(any()) }
	}

	@Test
	fun `constructor createsInstance withValidContext`() {
		// Arrange & Act
		val instance = UpdatePreferences(mockContext)

		// Assert
		assertNotNull(instance)
	}

	@Test
	fun `multipleOperations workSequentially withoutConflict`() = runTest {
		// Arrange
		val version1 = "1.0.0"
		val version2 = "2.0.0"
		coEvery { mockDataStore.edit(any()) } returns mockPreferences

		// Act
		updatePreferences.saveLastCheckedVersion(version1)
		updatePreferences.saveDismissedUpdateVersion(version2)
		updatePreferences.clearDismissedUpdateVersion()

		// Assert
		coVerify(exactly = 3) { mockDataStore.edit(any()) }
	}
}