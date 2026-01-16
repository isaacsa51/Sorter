package com.serranoie.app.media.sorter.di

import android.content.Context
import com.serranoie.app.media.sorter.data.settings.SettingsDataStore
import com.serranoie.app.media.sorter.data.settings.SettingsRepositoryImpl
import com.serranoie.app.media.sorter.domain.UndoManager
import com.serranoie.app.media.sorter.domain.repository.SettingsRepository
import com.serranoie.app.media.sorter.domain.settings.GetAppSettingsUseCase
import com.serranoie.app.media.sorter.domain.settings.UpdateSettingsUseCase
import com.serranoie.app.media.sorter.presentation.mapper.MediaFileMapper
import com.serranoie.app.media.sorter.presentation.model.MediaFileUi
import com.serranoie.app.media.sorter.util.MediaFileFormatter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }
    
    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsDataStore: SettingsDataStore
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideGetAppSettingsUseCase(
        repository: SettingsRepository
    ): GetAppSettingsUseCase {
        return GetAppSettingsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateSettingsUseCase(
        repository: SettingsRepository
    ): UpdateSettingsUseCase {
        return UpdateSettingsUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideMediaFileFormatter(): MediaFileFormatter {
        return MediaFileFormatter()
    }
    
    @Provides
    @Singleton
    fun provideMediaFileMapper(
        formatter: MediaFileFormatter
    ): MediaFileMapper {
        return MediaFileMapper(formatter)
    }
    
    @Provides
    fun provideUndoManager(): UndoManager<MediaFileUi> {
        return UndoManager()
    }
}
