package com.serranoie.app.media.sorter.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ViewedMediaDao {

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertViewed(viewedMedia: ViewedMedia)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAllViewed(viewedMediaList: List<ViewedMedia>)

	@Query("SELECT mediaId FROM viewed_media")
	suspend fun getAllViewedIds(): List<Long>

	@Query("SELECT * FROM viewed_media")
	suspend fun getAllViewed(): List<ViewedMedia>

	@Query("SELECT EXISTS(SELECT 1 FROM viewed_media WHERE mediaId = :mediaId)")
	suspend fun isViewed(mediaId: Long): Boolean

	@Query("DELETE FROM viewed_media")
	suspend fun clearAllViewed()

	@Query("SELECT COUNT(*) FROM viewed_media")
	suspend fun getViewedCount(): Int
}
