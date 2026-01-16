package com.serranoie.app.media.sorter.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MediaFileFormatter @Inject constructor() {

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    fun formatRelativeDate(date: LocalDate): String {
        val today = LocalDate.now()
        val daysDiff = ChronoUnit.DAYS.between(date, today).toInt()
        
        return when {
            daysDiff == 0 -> "Today"
            daysDiff == 1 -> "Yesterday"
            daysDiff < 7 -> "$daysDiff days ago"
            daysDiff < 30 -> {
                val weeks = daysDiff / 7
                "$weeks week${if (weeks > 1) "s" else ""} ago"
            }
            daysDiff < 365 -> {
                val months = daysDiff / 30
                "$months month${if (months > 1) "s" else ""} ago"
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
}
