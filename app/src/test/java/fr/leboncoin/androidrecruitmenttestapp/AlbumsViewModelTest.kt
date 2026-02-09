package fr.leboncoin.androidrecruitmenttestapp

import fr.leboncoin.data.local.dao.AlbumDao
import fr.leboncoin.data.local.entity.AlbumEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import fr.leboncoin.data.repository.AlbumRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    // Mutable state for fake DAO
    private val albumsData = mutableListOf<AlbumEntity>()
    private val albumsFlow = MutableStateFlow<List<AlbumEntity>>(emptyList())
    private val favoriteAlbumsFlow = MutableStateFlow<List<AlbumEntity>>(emptyList())

    private fun createFakeDao(): AlbumDao = object : AlbumDao {
        override fun getAllAlbums(): Flow<List<AlbumEntity>> = albumsFlow
        override suspend fun getAlbumById(id: Int): AlbumEntity? = albumsData.find { it.id == id }
        override fun observeAlbumById(id: Int): Flow<AlbumEntity?> {
            return albumsFlow.map { albums -> albums.find { it.id == id } }
        }

        override suspend fun insertAlbums(albums: List<AlbumEntity>) {
            albumsData.clear()
            albumsData.addAll(albums)
            updateFlows()
        }
        override suspend fun clearAllAlbums() {
            albumsData.clear()
            updateFlows()
        }
        override suspend fun getAlbumsCount(): Int = albumsData.size
        override suspend fun getCacheTimestamp(): Long? = null
        override suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
            val index = albumsData.indexOfFirst { it.id == id }
            if (index != -1) {
                albumsData[index] = albumsData[index].copy(isFavorite = isFavorite)
                updateFlows()
            }
        }

        override fun getFavoriteAlbums(): Flow<List<AlbumEntity>> {
            return favoriteAlbumsFlow
        }

        override suspend fun isFavorite(id: Int): Boolean {
            return albumsData.find { it.id == id }?.isFavorite ?: false
        }

        private fun updateFlows() {
            albumsFlow.value = albumsData.toList()
            favoriteAlbumsFlow.value = albumsData.filter { it.isFavorite }
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        albumsData.clear()
        albumsFlow.value = emptyList()
        favoriteAlbumsFlow.value = emptyList()
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
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)

        val viewModel = AlbumsViewModel(repository)

        assertTrue("Initial state should be Loading", viewModel.uiState.value is AlbumsUiState.Loading)
    }

    @Test
    fun `loadAlbums success emits Success state with albums`() = runTest {
        val expectedAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false),
            AlbumDto(id = 2, albumId = 2, title = "Album 2", url = "url2", thumbnailUrl = "thumb2", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = expectedAlbums
        }
        val fakeDao = createFakeDao()
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
        val fakeDao = createFakeDao()
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
            AlbumDto(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> {
                if (shouldFail) {
                    throw Exception("First attempt fails")
                }
                return successAlbums
            }
        }
        val fakeDao = createFakeDao()
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
            AlbumDto(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = expectedAlbums
        }
        val fakeDao = createFakeDao()
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

    // ========== Tests for Favorite Feature ==========

    @Test
    fun `toggleFavorite marks album as favorite`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        // Toggle favorite
        viewModel.toggleFavorite(1)
        advanceUntilIdle()

        // Verify the album is now favorite
        val isFavorite = viewModel.isFavorite(1)
        assertTrue("Album should be marked as favorite", isFavorite)
    }

    @Test
    fun `toggleFavorite unmarks album as favorite when called twice`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        // Toggle favorite twice
        viewModel.toggleFavorite(1)
        advanceUntilIdle()
        viewModel.toggleFavorite(1)
        advanceUntilIdle()

        // Verify the album is not favorite anymore
        val isFavorite = viewModel.isFavorite(1)
        assertTrue("Album should not be favorite after toggling twice", !isFavorite)
    }

    @Test
    fun `setFavorite marks album as favorite when true`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        // Set favorite
        viewModel.setFavorite(1, true)
        advanceUntilIdle()

        // Verify
        val isFavorite = viewModel.isFavorite(1)
        assertTrue("Album should be marked as favorite", isFavorite)
    }

    @Test
    fun `setFavorite unmarks album as favorite when false`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = true)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        // Set favorite to false
        viewModel.setFavorite(1, false)
        advanceUntilIdle()

        // Verify
        val isFavorite = viewModel.isFavorite(1)
        assertTrue("Album should not be favorite", !isFavorite)
    }

    @Test
    fun `favoriteAlbums flow emits only favorite albums`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false),
            AlbumDto(id = 2, albumId = 2, title = "Album 2", url = "url2", thumbnailUrl = "thumb2", isFavorite = false),
            AlbumDto(id = 3, albumId = 3, title = "Album 3", url = "url3", thumbnailUrl = "thumb3", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        // Create an active collector for the favoriteAlbums flow
        val job = launch {
            viewModel.favoriteAlbums.collect { }
        }

        advanceUntilIdle()

        // Initially, no favorites
        assertEquals("Should have no favorites initially", 0, viewModel.favoriteAlbums.value.size)

        // Mark albums 1 and 3 as favorites
        viewModel.setFavorite(1, true)
        advanceUntilIdle()
        viewModel.setFavorite(3, true)
        advanceUntilIdle()

        // Verify favoriteAlbums contains only albums 1 and 3
        val favorites = viewModel.favoriteAlbums.value
        assertEquals("Should have 2 favorites", 2, favorites.size)
        assertTrue("Should contain album 1", favorites.any { it.id == 1 })
        assertTrue("Should contain album 3", favorites.any { it.id == 3 })
        assertTrue("Should not contain album 2", favorites.none { it.id == 2 })

        job.cancel()
    }

    @Test
    fun `favoriteAlbums flow updates when favorite is removed`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        // Create an active collector for the favoriteAlbums flow
        val job = launch {
            viewModel.favoriteAlbums.collect { }
        }

        advanceUntilIdle()

        // Mark as favorite
        viewModel.setFavorite(1, true)
        advanceUntilIdle()

        assertEquals("Should have 1 favorite", 1, viewModel.favoriteAlbums.value.size)

        // Remove from favorites
        viewModel.setFavorite(1, false)
        advanceUntilIdle()

        assertEquals("Should have no favorites after removing", 0, viewModel.favoriteAlbums.value.size)

        job.cancel()
    }

    @Test
    fun `isFavorite returns false for non-favorite album`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        val isFavorite = viewModel.isFavorite(1)
        assertTrue("Album should not be favorite", !isFavorite)
    }

    @Test
    fun `isFavorite returns true for favorite album`() = runTest {
        val albums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1", isFavorite = false)
        )
        val fakeService = object : AlbumApiService {
            override suspend fun getAlbums(): List<AlbumDto> = albums
        }
        val fakeDao = createFakeDao()
        val repository = AlbumRepository(fakeService, fakeDao)
        val viewModel = AlbumsViewModel(repository)

        advanceUntilIdle()

        // Mark as favorite
        viewModel.setFavorite(1, true)
        advanceUntilIdle()

        val isFavorite = viewModel.isFavorite(1)
        assertTrue("Album should be favorite", isFavorite)
    }
}

