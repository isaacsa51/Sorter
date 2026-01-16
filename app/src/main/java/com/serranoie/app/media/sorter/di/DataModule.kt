package com.serranoie.app.media.sorter.di

import android.content.Context
import com.serranoie.app.media.sorter.data.SorterMediaRepositoryImpl
import com.serranoie.app.media.sorter.data.datasource.AndroidMediaDataSource
import com.serranoie.app.media.sorter.data.datasource.MediaDataSource
import com.serranoie.app.media.sorter.domain.repository.MediaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideMediaDataSource(
        @ApplicationContext context: Context
    ): MediaDataSource {
        return AndroidMediaDataSource(context)
    }
    
    @Provides
    @Singleton
    fun provideMediaRepository(
        mediaDataSource: MediaDataSource
    ): MediaRepository {
        return SorterMediaRepositoryImpl(mediaDataSource)
    }
}
