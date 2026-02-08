package com.serranoie.app.media.sorter.domain.media

import com.serranoie.app.media.sorter.data.MediaFile
import com.serranoie.app.media.sorter.domain.Result
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SorterMediaUseCaseTest {
	private val mockRepository: MediaRepository = mockk()
	private lateinit var useCase: SorterMediaUseCase

	@Before
	fun setUp() {
		useCase = SorterMediaUseCase(mockRepository)
	}

	@After
	fun tearDown() = clearAllMocks()

	@Test
	fun invoke_success_filtersAndSorts() = runTest {
		val valid =
			MediaFile(mockk(), "image", "jpg", "file1", "dir", 256L, LocalDate.now(), mockk(), 100L)
		val invalid =
			MediaFile(mockk(), "image", "jpg", "", "dir", 0L, LocalDate.now(), mockk(), 50L)
		val data = listOf(valid, invalid)
		coEvery { mockRepository.fetchMediaFiles() } returns Result.Success(data)

		val result = useCase.invoke()
		assert(result is Result.Success)
		val sorted = (result as Result.Success).data
		assertEquals(listOf(valid), sorted)
		coVerify { mockRepository.fetchMediaFiles() }
	}

	@Test
	fun invoke_error_returnsResultError() = runTest {
		coEvery { mockRepository.fetchMediaFiles() } returns Result.Error(mockk())
		val result = useCase.invoke()
		assert(result is Result.Error)
		coVerify { mockRepository.fetchMediaFiles() }
	}

	@Test
	fun invoke_loading_returnsLoading() = runTest {
		coEvery { mockRepository.fetchMediaFiles() } returns Result.Loading
		val result = useCase.invoke()
		assertEquals(Result.Loading, result)
		coVerify { mockRepository.fetchMediaFiles() }
	}

	@Test
	fun getMediaByFolder_returnsFolderMap() = runTest {
		val expected = Result.Success(mapOf("dir" to emptyList<MediaFile>()))
		coEvery { mockRepository.getMediaByFolder() } returns expected
		val actual = useCase.getMediaByFolder()
		assertEquals(expected, actual)
		coVerify { mockRepository.getMediaByFolder() }
	}
}
