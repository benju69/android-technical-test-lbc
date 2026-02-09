package fr.leboncoin.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.leboncoin.data.local.dao.AlbumDao
import fr.leboncoin.data.local.entity.AlbumEntity

@Database(
    entities = [AlbumEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao

    companion object {
        const val DATABASE_NAME = "leboncoin_albums.db"
    }
}

