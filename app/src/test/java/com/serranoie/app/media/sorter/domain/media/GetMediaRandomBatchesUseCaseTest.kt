package com.serranoie.app.media.sorter.domain.media

import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.AppError
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetMediaRandomBatchesUseCaseTest {

	private val mockRepository: MediaRepository = mockk()
	private lateinit var useCase: GetMediaRandomBatchesUseCase

	@Before
	fun setUp() {
		useCase = GetMediaRandomBatchesUseCase(mockRepository)
	}

	@After
	fun tearDown() = clearAllMocks()

	@Test
	fun invoke_empty_returnsNoMediaFoundError() = runTest {
		coEvery { mockRepository.getMediaGroupedByDateFiltered() } returns Result.Success(emptyMap())

		val result = useCase.invoke()
		assert(result is Result.Error)
		assert((result as Result.Error).error is AppError.NoMediaFoundError)
		coVerify { mockRepository.getMediaGroupedByDateFiltered() }
	}

	@Test
	fun invoke_loading_returnsLoading() = runTest {
		coEvery { mockRepository.getMediaGroupedByDateFiltered() } returns Result.Loading

		val result = useCase.invoke()
		assertEquals(Result.Loading, result)
		coVerify { mockRepository.getMediaGroupedByDateFiltered() }
	}

	@Test
	fun invoke_error_returnsError() = runTest {
		val appError = AppError.MediaLoadError("fail")
		coEvery { mockRepository.getMediaGroupedByDateFiltered() } returns Result.Error(appError)

		val result = useCase.invoke()
		assertEquals(Result.Error(appError), result)
		coVerify { mockRepository.getMediaGroupedByDateFiltered() }
	}

	@Test
	fun invoke_success_filtersBatches() = runTest {
		val date1 = LocalDate.of(2024, 1, 1)
		val date2 = LocalDate.of(2024, 1, 2)
		val valid = MediaFile(mockk(), "image", "jpg", "file1", "dir", 512L, date1, mockk(), 123456789L)
		val invalid = MediaFile(mockk(), "video", "mp4", "", "dir", 0L, date2, mockk(), 987654321L)
		val data = mapOf(
			date1 to listOf(valid),
			date2 to listOf(invalid)
		)
		coEvery { mockRepository.getMediaGroupedByDateFiltered() } returns Result.Success(data)

		val result = useCase.invoke()
		assert(result is Result.Success)
		val list = (result as Result.Success).data
		assertEquals(1, list.size)
		assertEquals(date1, list[0].first)
		assertEquals(listOf(valid), list[0].second)
		coVerify { mockRepository.getMediaGroupedByDateFiltered() }
	}
}
