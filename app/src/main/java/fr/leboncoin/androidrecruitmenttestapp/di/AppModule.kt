package fr.leboncoin.androidrecruitmenttestapp.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.leboncoin.androidrecruitmenttestapp.utils.AnalyticsHelper
import okio.Path.Companion.toOkioPath
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnalyticsHelper(
        @ApplicationContext context: Context
    ): AnalyticsHelper {
        return AnalyticsHelper().apply {
            initialize(context)
        }
    }

    /**
     * Provides an ImageLoader optimized for scrolling performance
     *
     * Optimizations:
     * - MemoryCache: 25% of RAM to retain recently scrolled images
     * - DiskCache: 100MB for persistent caching
     * - Crossfade: Smooth transitions by default
     */
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(100 * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
