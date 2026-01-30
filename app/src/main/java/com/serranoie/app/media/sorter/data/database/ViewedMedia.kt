package com.serranoie.app.media.sorter.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "viewed_media")
data class ViewedMedia(
	@PrimaryKey
	val mediaId: Long,
	val viewedTimestamp: Long = System.currentTimeMillis()
)
