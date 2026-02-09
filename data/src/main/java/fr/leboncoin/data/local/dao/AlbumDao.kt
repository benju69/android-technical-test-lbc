package fr.leboncoin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.leboncoin.data.local.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Query("SELECT * FROM albums ORDER BY albumId ASC, id ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Int): AlbumEntity?

    @Query("SELECT * FROM albums WHERE id = :id")
    fun observeAlbumById(id: Int): Flow<AlbumEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    suspend fun clearAllAlbums()

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumsCount(): Int

    @Query("SELECT cachedAt FROM albums LIMIT 1")
    suspend fun getCacheTimestamp(): Long?

    @Query("UPDATE albums SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("SELECT * FROM albums WHERE isFavorite = 1 ORDER BY albumId ASC, id ASC")
    fun getFavoriteAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT isFavorite FROM albums WHERE id = :id")
    suspend fun isFavorite(id: Int): Boolean?
}

