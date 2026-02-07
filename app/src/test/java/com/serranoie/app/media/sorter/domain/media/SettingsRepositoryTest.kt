package com.serranoie.app.media.sorter.data.settings

import com.serranoie.app.media.sorter.domain.repository.SettingsRepository
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {

	private val mockDataStore: SettingsDataStore = mockk(relaxed = true)
	private lateinit var settingsRepository: SettingsRepositoryImpl

	@Before
	fun setUp() {
		settingsRepository = SettingsRepositoryImpl(mockDataStore)
	}

	@After
	fun tearDown() = clearAllMocks()

	@Test
	fun setThemeMode_callsDataStore() = runTest {
		coEvery { mockDataStore.setThemeMode(any()) } just Runs
		settingsRepository.setThemeMode(ThemeMode.DARK)
		coVerify { mockDataStore.setThemeMode(ThemeMode.DARK) }
	}

	@Test
	fun setUseDynamicColors_callsDataStore() = runTest {
		coEvery { mockDataStore.setUseDynamicColors(any()) } just Runs
		settingsRepository.setUseDynamicColors(true)
		coVerify { mockDataStore.setUseDynamicColors(true) }
	}

	@Test
	fun setUseBlurredBackground_callsDataStore() = runTest {
		coEvery { mockDataStore.setUseBlurredBackground(any()) } just Runs
		settingsRepository.setUseBlurredBackground(false)
		coVerify { mockDataStore.setUseBlurredBackground(false) }
	}

	@Test
	fun setTutorialCompleted_callsDataStore() = runTest {
		coEvery { mockDataStore.setTutorialCompleted(any()) } just Runs
		settingsRepository.setTutorialCompleted(true)
		coVerify { mockDataStore.setTutorialCompleted(true) }
	}

	@Test
	fun resetTutorial_callsDataStore() = runTest {
		coEvery { mockDataStore.resetTutorial() } just Runs
		settingsRepository.resetTutorial()
		coVerify { mockDataStore.resetTutorial() }
	}

	@Test
	fun setAutoPlayVideos_callsDataStore() = runTest {
		coEvery { mockDataStore.setAutoPlayVideos(any()) } just Runs
		settingsRepository.setAutoPlayVideos(true)
		coVerify { mockDataStore.setAutoPlayVideos(true) }
	}

	@Test
	fun setUseAureaPadding_callsDataStore() = runTest {
		coEvery { mockDataStore.setUseAureaPadding(any()) } just Runs
		settingsRepository.setUseAureaPadding(true)
		coVerify { mockDataStore.setUseAureaPadding(true) }
	}

	@Test
	fun resetSettings_callsClearSettings() = runTest {
		coEvery { mockDataStore.clearSettings() } just Runs
		settingsRepository.resetSettings()
		coVerify { mockDataStore.clearSettings() }
	}
}
