package com.serranoie.app.media.sorter.di

import android.content.Context
import androidx.room.Room
import com.serranoie.app.media.sorter.data.database.SorterDatabase
import com.serranoie.app.media.sorter.data.database.ViewedMediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	
	@Provides
	@Singleton
	fun provideSorterDatabase(
		@ApplicationContext context: Context
	): SorterDatabase {
		return Room.databaseBuilder(
			context,
			SorterDatabase::class.java,
			SorterDatabase.DATABASE_NAME
		)
			.fallbackToDestructiveMigration(false)
			.build()
	}
	
	@Provides
	@Singleton
	fun provideViewedMediaDao(database: SorterDatabase): ViewedMediaDao {
		return database.viewedMediaDao()
	}
}
