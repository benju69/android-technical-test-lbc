package fr.leboncoin.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.data.local.dao.AlbumDao
import fr.leboncoin.data.network.api.AlbumApiService
import fr.leboncoin.data.repository.AlbumRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAlbumRepository(
        albumApiService: AlbumApiService,
        albumDao: AlbumDao
    ): AlbumRepository {
        return AlbumRepository(albumApiService, albumDao)
    }
}

