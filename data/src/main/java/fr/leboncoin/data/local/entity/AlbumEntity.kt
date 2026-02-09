package fr.leboncoin.data.local.entity

import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey
    val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val cachedAt: Long = System.currentTimeMillis(),
)
