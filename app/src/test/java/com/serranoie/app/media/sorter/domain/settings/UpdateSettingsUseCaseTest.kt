package com.serranoie.app.media.sorter.domain.settings

import com.serranoie.app.media.sorter.data.settings.ThemeMode
import com.serranoie.app.media.sorter.domain.repository.SettingsRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UpdateSettingsUseCaseTest {
	private val mockRepository: SettingsRepository = mockk(relaxed = true)
	private lateinit var useCase: UpdateSettingsUseCase

	@Before
	fun setUp() {
		useCase = UpdateSettingsUseCase(mockRepository)
	}

	@After
	fun tearDown() = clearAllMocks()

	@Test
	fun setThemeMode_delegates() = runTest {
		coEvery { mockRepository.setThemeMode(any()) } just Runs
		useCase.setThemeMode(ThemeMode.DARK)
		coVerify { mockRepository.setThemeMode(ThemeMode.DARK) }
	}

	@Test
	fun setUseDynamicColors_delegates() = runTest {
		coEvery { mockRepository.setUseDynamicColors(any()) } just Runs
		useCase.setUseDynamicColors(true)
		coVerify { mockRepository.setUseDynamicColors(true) }
	}

	@Test
	fun setUseBlurredBackground_delegates() = runTest {
		coEvery { mockRepository.setUseBlurredBackground(any()) } just Runs
		useCase.setUseBlurredBackground(false)
		coVerify { mockRepository.setUseBlurredBackground(false) }
	}

	@Test
	fun setTutorialCompleted_delegates() = runTest {
		coEvery { mockRepository.setTutorialCompleted(any()) } just Runs
		useCase.setTutorialCompleted(true)
		coVerify { mockRepository.setTutorialCompleted(true) }
	}

	@Test
	fun resetTutorial_delegates() = runTest {
		coEvery { mockRepository.resetTutorial() } just Runs
		useCase.resetTutorial()
		coVerify { mockRepository.resetTutorial() }
	}

	@Test
	fun setAutoPlayVideos_delegates() = runTest {
		coEvery { mockRepository.setAutoPlayVideos(any()) } just Runs
		useCase.setAutoPlayVideos(true)
		coVerify { mockRepository.setAutoPlayVideos(true) }
	}

	@Test
	fun setUseAureaPadding_delegates() = runTest {
		coEvery { mockRepository.setUseAureaPadding(any()) } just Runs
		useCase.setUseAureaPadding(true)
		coVerify { mockRepository.setUseAureaPadding(true) }
	}

	@Test
	fun resetSettings_delegates() = runTest {
		coEvery { mockRepository.resetSettings() } just Runs
		useCase.resetSettings()
		coVerify { mockRepository.resetSettings() }
	}
}
