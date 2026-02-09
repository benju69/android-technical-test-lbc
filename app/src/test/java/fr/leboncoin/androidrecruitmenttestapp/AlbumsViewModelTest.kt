package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.data.local.dao.AlbumDao
import fr.leboncoin.data.local.entity.AlbumEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    val fakeDao = object : AlbumDao {
        override fun getAllAlbums(): Flow<List<AlbumEntity>> = flowOf(emptyList())
        override suspend fun getAlbumById(id: Int): AlbumEntity? = null
        override fun observeAlbumById(id: Int): Flow<AlbumEntity?> {
            return flowOf(null)
        }

        override suspend fun insertAlbums(albums: List<AlbumEntity>) {}
        override suspend fun clearAllAlbums() {}
        override suspend fun getAlbumsCount(): Int = 0
        override suspend fun getCacheTimestamp(): Long? = null
        override suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {}

        override fun getFavoriteAlbums(): Flow<List<AlbumEntity>> {
            return flowOf(emptyList())
        }

        override suspend fun isFavorite(id: Int): Boolean {
            return false
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = emptyList()
        }
        val repository = AlbumRepository(fakeService, fakeDao)

        val viewModel = AlbumsViewModel(repository)

        assertTrue("Initial state should be Loading", viewModel.uiState.value is AlbumsUiState.Loading)
    }

    @Test
    fun `loadAlbums success emits Success state with albums`() = runTest {
        val expectedAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1"),
            AlbumDto(id = 2, albumId = 2, title = "Album 2", url = "url2", thumbnailUrl = "thumb2")
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = expectedAlbums
        }
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success", state is AlbumsUiState.Success)
        assertEquals("Albums should match", expectedAlbums, (state as AlbumsUiState.Success).albums)
    }

    @Test
    fun `loadAlbums failure emits Error state with message`() = runTest {
        val errorMessage = "Network error"
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> {
                throw Exception(errorMessage)
            }
        }
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Error", state is AlbumsUiState.Error)
        assertTrue(
            "Error message should contain exception message",
            (state as AlbumsUiState.Error).message.contains(errorMessage)
        )
    }

    @Test
    fun `loadAlbums can be retried after error`() = runTest {
        var shouldFail = true
        val successAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb")
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> {
                if (shouldFail) {
                    throw Exception("First attempt fails")
                }
                return successAlbums
            }
        }
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()
        assertTrue("First attempt should fail", viewModel.uiState.value is AlbumsUiState.Error)

        // Retry
        shouldFail = false
        viewModel.loadAlbums()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Retry should succeed", state is AlbumsUiState.Success)
        assertEquals("Should have albums after retry", successAlbums, (state as AlbumsUiState.Success).albums)
    }

    @Test
    fun `init loads albums automatically`() = runTest {
        val expectedAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb")
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = expectedAlbums
        }
        val repository = AlbumRepository(fakeService, fakeDao)

        val viewModel = AlbumsViewModel(repository)

        // Initially Loading
        assertTrue("State should initially be Loading", viewModel.uiState.value is AlbumsUiState.Loading)

        // After completing coroutines, should be Success
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success after init", state is AlbumsUiState.Success)
        assertEquals("Albums should be loaded", expectedAlbums, (state as AlbumsUiState.Success).albums)
    }
}

