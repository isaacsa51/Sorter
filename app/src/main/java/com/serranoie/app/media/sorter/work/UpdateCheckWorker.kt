package com.serranoie.app.media.sorter.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.serranoie.app.media.sorter.MainActivity
import com.serranoie.app.media.sorter.R
import com.serranoie.app.media.sorter.update.UpdateManager
import com.serranoie.app.media.sorter.update.UpdateCheckResponse
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
	@Assisted private val context: Context,
	@Assisted private val params: WorkerParameters,
	private val updateManager: UpdateManager
) : CoroutineWorker(context, params) {

	override suspend fun doWork(): Result {
		return withContext(Dispatchers.IO) {
			try {
				val response = updateManager.checkForUpdates(forceCheck = true)

				if (response.hasUpdate) {
					val shouldShowNotification = shouldShowNotification(response)
					if (shouldShowNotification) {
						showUpdateNotification(response)
					}
				}

				Result.success()
			} catch (e: Exception) {
				Result.retry()
			}
		}
	}

	private suspend fun shouldShowNotification(response: UpdateCheckResponse): Boolean {
		val dismissedVersion = updateManager.getDismissedUpdateVersion()
		val currentVersion = response.updateInfo?.versionName
		return dismissedVersion != currentVersion
	}

	private fun showUpdateNotification(response: UpdateCheckResponse) {
		val notificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

		createNotificationChannel()

		val intent = Intent(context, MainActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
			putExtra("show_update_dialog", true)
		}

		val pendingIntent = PendingIntent.getActivity(
			context,
			UPDATE_NOTIFICATION_ID,
			intent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)

		val contentText = response.updateInfo?.versionName?.let { 
			context.getString(R.string.notification_update_text, it)
		} ?: context.getString(R.string.notification_update_text_generic)

		val notification =
			NotificationCompat.Builder(context, CHANNEL_ID)
				.setContentTitle(context.getString(R.string.notification_update_title))
				.setContentText(contentText)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.build()

		notificationManager.notify(UPDATE_NOTIFICATION_ID, notification)
	}

	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				CHANNEL_ID,
				context.getString(R.string.notification_channel_updates_name),
				NotificationManager.IMPORTANCE_HIGH
			).apply {
				description = context.getString(R.string.notification_channel_updates_desc)
			}

			val notificationManager =
				context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}

	companion object {
		const val WORK_NAME = "UpdateCheckWorker"
		const val CHANNEL_ID = "app_update_channel"
		const val UPDATE_NOTIFICATION_ID = 1001
	}
}

object UpdateWorkerHelper {
	const val WORK_TAG = "periodic_update_check"
}