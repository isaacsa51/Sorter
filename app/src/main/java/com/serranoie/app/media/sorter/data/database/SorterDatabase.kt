package com.serranoie.app.media.sorter.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
	entities = [ViewedMedia::class],
	version = 1,
	exportSchema = false
)
abstract class SorterDatabase : RoomDatabase() {
	
	abstract fun viewedMediaDao(): ViewedMediaDao
	
	companion object {
		const val DATABASE_NAME = "sorter_database"
	}
}
