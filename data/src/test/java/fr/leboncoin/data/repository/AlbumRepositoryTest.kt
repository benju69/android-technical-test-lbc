package fr.leboncoin.data.repository

import fr.leboncoin.data.local.dao.AlbumDao
import fr.leboncoin.data.local.entity.AlbumEntity
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumRepositoryTest {

    private val albumsData = mutableListOf<AlbumEntity>()
    private val albumsFlow = MutableStateFlow<List<AlbumEntity>>(emptyList())
    private val favoriteAlbumsFlow = MutableStateFlow<List<AlbumEntity>>(emptyList())

    private var apiAlbums = listOf<AlbumDto>()
    private var apiShouldFail = false
    private var apiErrorMessage = "Network error"
    private var cacheTimestamp: Long? = null

    private fun createFakeDao(): AlbumDao = object : AlbumDao {
        override fun getAllAlbums(): Flow<List<AlbumEntity>> = albumsFlow
        override suspend fun getAlbumById(id: Int): AlbumEntity? = albumsData.find { it.id == id }
        override fun observeAlbumById(id: Int): Flow<AlbumEntity?> =
            albumsFlow.map { albums -> albums.find { it.id == id } }

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
        override suspend fun getCacheTimestamp(): Long? = cacheTimestamp

        override suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean) {
            val index = albumsData.indexOfFirst { it.id == id }
            if (index != -1) {
                albumsData[index] = albumsData[index].copy(isFavorite = isFavorite)
                updateFlows()
            }
        }

        override fun getFavoriteAlbums(): Flow<List<AlbumEntity>> = favoriteAlbumsFlow

        override suspend fun isFavorite(id: Int): Boolean? =
            albumsData.find { it.id == id }?.isFavorite

        private fun updateFlows() {
            albumsFlow.value = albumsData.toList()
            favoriteAlbumsFlow.value = albumsData.filter { it.isFavorite }
        }
    }

    private fun createFakeApi(): AlbumApiService = object : AlbumApiService {
        override suspend fun getAlbums(): List<AlbumDto> {
            if (apiShouldFail) throw Exception(apiErrorMessage)
            return apiAlbums
        }
    }

    private lateinit var fakeDao: AlbumDao
    private lateinit var fakeApi: AlbumApiService
    private lateinit var repository: AlbumRepository

    @Before
    fun setup() {
        albumsData.clear()
        albumsFlow.value = emptyList()
        favoriteAlbumsFlow.value = emptyList()
        apiAlbums = emptyList()
        apiShouldFail = false
        cacheTimestamp = null

        fakeDao = createFakeDao()
        fakeApi = createFakeApi()
        repository = AlbumRepository(fakeApi, fakeDao)
    }

    // ========== getAlbumsWithCache ==========

    @Test
    fun `getAlbumsWithCache emits success from network when cache is empty`() = runTest {
        apiAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Album 1", url = "url1", thumbnailUrl = "thumb1")
        )

        val results = mutableListOf<Result<List<AlbumDto>>>()
        val job = launch(UnconfinedTestDispatcher()) {
            repository.getAlbumsWithCache().toList(results)
        }

        assertTrue("Should emit at least one result", results.isNotEmpty())
        val lastResult = results.last()
        assertTrue("Should be success", lastResult.isSuccess)
        assertEquals(1, lastResult.getOrNull()?.size)
        assertEquals("Album 1", lastResult.getOrNull()?.first()?.title)

        job.cancel()
    }

    @Test
    fun `getAlbumsWithCache emits cached data first then refreshes from network`() = runTest {
        // Pre-populate cache
        val cachedEntity = AlbumEntity(
            id = 1, albumId = 1, title = "Cached Album",
            url = "url1", thumbnailUrl = "thumb1", cachedAt = 0 // Expired
        )
        albumsData.add(cachedEntity)
        albumsFlow.value = albumsData.toList()
        cacheTimestamp = 0L // Expired cache

        apiAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Network Album", url = "url1", thumbnailUrl = "thumb1")
        )

        val results = mutableListOf<Result<List<AlbumDto>>>()
        val job = launch(UnconfinedTestDispatcher()) {
            repository.getAlbumsWithCache().toList(results)
        }

        // First emission: cached data, second emission: network data
        assertTrue("Should emit at least 2 results", results.size >= 2)
        assertEquals("Cached Album", results.first().getOrNull()?.first()?.title)
        assertEquals("Network Album", results.last().getOrNull()?.first()?.title)

        job.cancel()
    }

    @Test
    fun `getAlbumsWithCache does not refresh when cache is still valid`() = runTest {
        // Pre-populate cache with a recent timestamp
        val cachedEntity = AlbumEntity(
            id = 1, albumId = 1, title = "Cached Album",
            url = "url1", thumbnailUrl = "thumb1", cachedAt = System.currentTimeMillis()
        )
        albumsData.add(cachedEntity)
        albumsFlow.value = albumsData.toList()
        cacheTimestamp = System.currentTimeMillis() // Valid cache

        apiAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Network Album", url = "url1", thumbnailUrl = "thumb1")
        )

        val results = mutableListOf<Result<List<AlbumDto>>>()
        val job = launch(UnconfinedTestDispatcher()) {
            repository.getAlbumsWithCache().toList(results)
        }

        // Should only emit cached data, not refresh from network
        assertEquals(1, results.size)
        assertEquals("Cached Album", results.first().getOrNull()?.first()?.title)

        job.cancel()
    }

    @Test
    fun `getAlbumsWithCache emits failure when cache is empty and network fails`() = runTest {
        apiShouldFail = true

        val results = mutableListOf<Result<List<AlbumDto>>>()
        val job = launch(UnconfinedTestDispatcher()) {
            repository.getAlbumsWithCache().toList(results)
        }

        assertTrue("Should emit at least one result", results.isNotEmpty())
        assertTrue("Should be failure", results.last().isFailure)

        job.cancel()
    }

    @Test
    fun `getAlbumsWithCache uses expired cache when network fails`() = runTest {
        // Pre-populate with expired cache
        val cachedEntity = AlbumEntity(
            id = 1, albumId = 1, title = "Old Cached Album",
            url = "url1", thumbnailUrl = "thumb1", cachedAt = 0
        )
        albumsData.add(cachedEntity)
        albumsFlow.value = albumsData.toList()
        cacheTimestamp = 0L // Expired

        apiShouldFail = true

        val results = mutableListOf<Result<List<AlbumDto>>>()
        val job = launch(UnconfinedTestDispatcher()) {
            repository.getAlbumsWithCache().toList(results)
        }

        // Should emit cached data, and not emit failure because cache was not empty
        assertTrue("Should emit at least one result", results.isNotEmpty())
        assertTrue("Should be success with cached data", results.first().isSuccess)
        assertEquals("Old Cached Album", results.first().getOrNull()?.first()?.title)

        // Should not have a failure result
        assertTrue("All results should be success", results.all { it.isSuccess })

        job.cancel()
    }

    // ========== refreshAlbums ==========

    @Test
    fun `refreshAlbums returns success with albums from network`() = runTest {
        apiAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "Fresh Album", url = "url1", thumbnailUrl = "thumb1")
        )

        val result = repository.refreshAlbums()

        assertTrue("Should be success", result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Fresh Album", result.getOrNull()?.first()?.title)
    }

    @Test
    fun `refreshAlbums returns failure when network fails`() = runTest {
        apiShouldFail = true

        val result = repository.refreshAlbums()

        assertTrue("Should be failure", result.isFailure)
    }

    @Test
    fun `refreshAlbums clears old cache and inserts new data`() = runTest {
        // Pre-populate cache
        val oldEntity = AlbumEntity(
            id = 99, albumId = 99, title = "Old Album",
            url = "old", thumbnailUrl = "old"
        )
        albumsData.add(oldEntity)
        albumsFlow.value = albumsData.toList()

        apiAlbums = listOf(
            AlbumDto(id = 1, albumId = 1, title = "New Album", url = "new", thumbnailUrl = "new")
        )

        repository.refreshAlbums()

        // Old data should be replaced by new data
        assertEquals(1, albumsData.size)
        assertEquals("New Album", albumsData.first().title)
        assertEquals(1, albumsData.first().id)
    }

    // ========== toggleFavorite ==========

    @Test
    fun `toggleFavorite flips false to true`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = false)
        )

        val result = repository.toggleFavorite(1)

        assertTrue("Should be success", result.isSuccess)
        assertTrue("Should be true after toggle", result.getOrNull() == true)
        assertTrue("Data should be updated", albumsData.first().isFavorite)
    }

    @Test
    fun `toggleFavorite flips true to false`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = true)
        )

        val result = repository.toggleFavorite(1)

        assertTrue("Should be success", result.isSuccess)
        assertTrue("Should be false after toggle", result.getOrNull() == false)
        assertFalse("Data should be updated", albumsData.first().isFavorite)
    }

    // ========== setFavorite ==========

    @Test
    fun `setFavorite sets album as favorite`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = false)
        )

        val result = repository.setFavorite(1, true)

        assertTrue("Should be success", result.isSuccess)
        assertTrue("Album should be favorite", albumsData.first().isFavorite)
    }

    @Test
    fun `setFavorite removes album from favorites`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = true)
        )

        val result = repository.setFavorite(1, false)

        assertTrue("Should be success", result.isSuccess)
        assertFalse("Album should not be favorite", albumsData.first().isFavorite)
    }

    // ========== isFavorite ==========

    @Test
    fun `isFavorite returns true for favorite album`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = true)
        )

        assertTrue(repository.isFavorite(1))
    }

    @Test
    fun `isFavorite returns false for non-favorite album`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb", isFavorite = false)
        )

        assertFalse(repository.isFavorite(1))
    }

    @Test
    fun `isFavorite returns false for non-existing album`() = runTest {
        assertFalse(repository.isFavorite(999))
    }

    // ========== getFavoriteAlbums ==========

    @Test
    fun `getFavoriteAlbums returns only favorite albums`() = runTest {
        albumsData.addAll(
            listOf(
                AlbumEntity(id = 1, albumId = 1, title = "Fav", url = "url", thumbnailUrl = "thumb", isFavorite = true),
                AlbumEntity(id = 2, albumId = 1, title = "Not Fav", url = "url", thumbnailUrl = "thumb", isFavorite = false),
            )
        )
        favoriteAlbumsFlow.value = albumsData.filter { it.isFavorite }

        val favorites = repository.getFavoriteAlbums().first()

        assertEquals(1, favorites.size)
        assertEquals("Fav", favorites.first().title)
    }

    // ========== getAlbumById ==========

    @Test
    fun `getAlbumById returns album when it exists`() = runTest {
        albumsData.add(
            AlbumEntity(id = 42, albumId = 1, title = "My Album", url = "url", thumbnailUrl = "thumb")
        )

        val album = repository.getAlbumById(42)

        assertNotNull(album)
        assertEquals("My Album", album?.title)
        assertEquals(42, album?.id)
    }

    @Test
    fun `getAlbumById returns null when album does not exist`() = runTest {
        val album = repository.getAlbumById(999)
        assertNull(album)
    }

    // ========== observeAlbumById ==========

    @Test
    fun `observeAlbumById emits album when present`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Observed Album", url = "url", thumbnailUrl = "thumb")
        )
        albumsFlow.value = albumsData.toList()

        val album = repository.observeAlbumById(1).first()

        assertNotNull(album)
        assertEquals("Observed Album", album?.title)
    }

    @Test
    fun `observeAlbumById emits null when album is not present`() = runTest {
        albumsFlow.value = emptyList()

        val album = repository.observeAlbumById(999).first()

        assertNull(album)
    }

    // ========== getCachedAlbums ==========

    @Test
    fun `getCachedAlbums returns albums from dao`() = runTest {
        albumsData.addAll(
            listOf(
                AlbumEntity(id = 1, albumId = 1, title = "Album 1", url = "url", thumbnailUrl = "thumb"),
                AlbumEntity(id = 2, albumId = 1, title = "Album 2", url = "url", thumbnailUrl = "thumb"),
            )
        )
        albumsFlow.value = albumsData.toList()

        val albums = repository.getCachedAlbums().first()

        assertEquals(2, albums.size)
    }

    // ========== hasCachedData ==========

    @Test
    fun `hasCachedData returns true when data exists`() = runTest {
        albumsData.add(
            AlbumEntity(id = 1, albumId = 1, title = "Album", url = "url", thumbnailUrl = "thumb")
        )

        assertTrue(repository.hasCachedData())
    }

    @Test
    fun `hasCachedData returns false when no data`() = runTest {
        assertFalse(repository.hasCachedData())
    }
}

