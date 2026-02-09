package fr.leboncoin.data.repository

import fr.leboncoin.data.local.dao.AlbumDao
import fr.leboncoin.data.mapper.toDto
import fr.leboncoin.data.mapper.toDtoList
import fr.leboncoin.data.mapper.toEntityList
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.network.model.AlbumDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class AlbumRepository(
    private val albumApiService: AlbumApiService,
    private val albumDao: AlbumDao
) {
    
    companion object {
        // Cache duration: 1 hour
        private val CACHE_DURATION_MS = TimeUnit.HOURS.toMillis(1)
    }

    /**
     * Offline-first strategy:
     * 1. Returns cached data if available
     * 2. Refreshes from network in background
     * 3. In case of network error, uses cache even if expired
     */
    fun getAlbumsWithCache(): Flow<Result<List<AlbumDto>>> = flow {
        try {
            // First emit cached data if available
            val cachedAlbums = albumDao.getAllAlbums().first()
            if (cachedAlbums.isNotEmpty()) {
                emit(Result.success(cachedAlbums.toDtoList()))
            }

            // Check if cache is still valid
            val cacheTimestamp = albumDao.getCacheTimestamp()
            val isCacheValid = cacheTimestamp?.let {
                System.currentTimeMillis() - it < CACHE_DURATION_MS
            } ?: false

            // If cache is not valid, refresh from network
            if (!isCacheValid) {
                try {
                    val networkAlbums = albumApiService.getAlbums()

                    // Update cache
                    albumDao.clearAllAlbums()
                    albumDao.insertAlbums(networkAlbums.toEntityList())

                    // Emit new data
                    emit(Result.success(networkAlbums))
                } catch (networkException: Exception) {
                    // In case of network error, use cache if available
                    if (cachedAlbums.isEmpty()) {
                        emit(Result.failure(networkException))
                    }
                    // Otherwise, keep already emitted cached data
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Retrieves albums from cache only
     */
    fun getCachedAlbums(): Flow<List<AlbumDto>> {
        return albumDao.getAllAlbums().map { it.toDtoList() }
    }

    /**
     * Forces a refresh from network
     */
    suspend fun refreshAlbums(): Result<List<AlbumDto>> {
        return try {
            val albums = albumApiService.getAlbums()
            albumDao.clearAllAlbums()
            albumDao.insertAlbums(albums.toEntityList())
            Result.success(albums)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Legacy method for compatibility
     */
    suspend fun getAllAlbums() = albumApiService.getAlbums()

    /**
     * Retrieves an album by its ID from cache
     */
    suspend fun getAlbumById(id: Int): AlbumDto? {
        return albumDao.getAlbumById(id)?.toDto()
    }

    /**
     * Observes an album by its ID from cache
     */
    fun observeAlbumById(id: Int): Flow<AlbumDto?> {
        return albumDao.observeAlbumById(id).map { it?.toDto() }
    }

    /**
     * Checks if data is cached
     */
    suspend fun hasCachedData(): Boolean {
        return albumDao.getAlbumsCount() > 0
    }

    /**
     * Toggles favorite status for an album
     */
    suspend fun toggleFavorite(id: Int): Result<Boolean> {
        return try {
            val currentStatus = albumDao.isFavorite(id) ?: false
            val newStatus = !currentStatus
            albumDao.updateFavoriteStatus(id, newStatus)
            Result.success(newStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sets favorite status for an album
     */
    suspend fun setFavorite(id: Int, isFavorite: Boolean): Result<Unit> {
        return try {
            albumDao.updateFavoriteStatus(id, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets all favorite albums
     */
    fun getFavoriteAlbums(): Flow<List<AlbumDto>> {
        return albumDao.getFavoriteAlbums().map { it.toDtoList() }
    }

    /**
     * Checks if an album is favorite
     */
    suspend fun isFavorite(id: Int): Boolean {
        return albumDao.isFavorite(id) ?: false
    }
}

