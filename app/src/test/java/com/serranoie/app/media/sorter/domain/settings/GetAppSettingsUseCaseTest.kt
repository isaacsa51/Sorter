package com.serranoie.app.media.sorter.domain.settings

import com.serranoie.app.media.sorter.data.settings.AppSettings
import com.serranoie.app.media.sorter.domain.repository.SettingsRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class GetAppSettingsUseCaseTest {
	private val mockRepository: SettingsRepository = mockk()
	private lateinit var useCase: GetAppSettingsUseCase

	@Before
	fun setUp() {
		useCase = GetAppSettingsUseCase(mockRepository)
	}

	@After
	fun tearDown() = clearAllMocks()

	@Test
	fun invoke_returns_appSettings_flow_from_repository() = runTest {
		val expected = AppSettings(tutorialCompleted = true)
		every { mockRepository.appSettings } returns flowOf(expected)

		val actual = useCase.invoke().first()

		assertEquals(expected, actual)
		verify { mockRepository.appSettings }
	}
}
