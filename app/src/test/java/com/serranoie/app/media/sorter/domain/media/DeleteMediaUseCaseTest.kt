package com.serranoie.app.media.sorter.domain.media

import android.net.Uri
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeleteMediaUseCaseTest {

	private val mockRepository: MediaRepository = mockk()
	private lateinit var useCase: DeleteMediaUseCase

	@Before
	fun setUp() {
		useCase = DeleteMediaUseCase(mockRepository)
	}

	@After
	fun tearDown() = clearAllMocks()

	@Test
	fun invoke_success_returnsResult() = runTest {
		val uri = mockk<Uri>()
		val expected = Result.Success(true)
		coEvery { mockRepository.deleteMedia(uri) } returns expected

		val result = useCase.invoke(uri)
		assertEquals(expected, result)
		coVerify { mockRepository.deleteMedia(uri) }
	}

	@Test
	fun deleteMultiple_success_returnsResult() = runTest {
		val uris = listOf(mockk<Uri>(), mockk<Uri>())
		val expected = Result.Success(2)
		coEvery { mockRepository.deleteMultipleMedia(uris) } returns expected

		val result = useCase.deleteMultiple(uris)
		assertEquals(expected, result)
		coVerify { mockRepository.deleteMultipleMedia(uris) }
	}

	@Test
	fun invoke_error_returnsResultError() = runTest {
		val uri = mockk<Uri>()
		val expected = Result.Error(mockk())
		coEvery { mockRepository.deleteMedia(uri) } returns expected

		val result = useCase.invoke(uri)
		assertEquals(expected, result)
		coVerify { mockRepository.deleteMedia(uri) }
	}
}
