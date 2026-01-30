package com.serranoie.app.media.sorter.presentation.mapper

import android.content.Context
import com.serranoie.app.media.sorter.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MediaFileFormatter @Inject constructor(
	@ApplicationContext private val context: Context
) {

	fun formatFileSize(bytes: Long): String {
		return when {
			bytes < 1024 -> "$bytes B"
			bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
			bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
			else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
		}
	}

	fun formatRelativeDate(date: LocalDate): String {
		val today = LocalDate.now()
		val daysDiff = ChronoUnit.DAYS.between(date, today).toInt()

		return when {
			daysDiff == 0 -> context.getString(R.string.date_today)
			daysDiff == 1 -> context.getString(R.string.date_yesterday)
			daysDiff < 7 -> context.resources.getQuantityString(R.plurals.days_ago, daysDiff, daysDiff)
			daysDiff < 30 -> {
				val weeks = daysDiff / 7
				context.resources.getQuantityString(R.plurals.weeks_ago, weeks, weeks)
			}

			daysDiff < 365 -> {
				val months = daysDiff / 30
				context.resources.getQuantityString(R.plurals.months_ago, months, months)
			}

			else -> {
				val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
				date.format(formatter)
			}
		}
	}

	fun formatDateTime(timestamp: Long): String {
		val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
		return sdf.format(Date(timestamp))
	}

	fun formatFileInfo(fileSizeBytes: Long, date: LocalDate): String {
		val sizeStr = formatFileSize(fileSizeBytes)
		val dateStr = formatRelativeDate(date)
		return "$sizeStr • $dateStr"
	}

	fun formatFileInfoWithPath(fileSizeBytes: Long, path: String): String {
		val sizeStr = formatFileSize(fileSizeBytes)
		return "$sizeStr • $path"
	}
}
