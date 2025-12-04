package com.cs407.hive.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cs407.hive.MainActivity
import com.cs407.hive.R
import com.cs407.hive.data.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChoreCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "ChoreCheckWorker"
        const val KEY_GROUP_ID = "group_id"
        const val KEY_DEVICE_ID = "device_id"
        const val CHANNEL_ID = "chore_updates"
        const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "chore_check_prefs"
        private const val KEY_LAST_CHORE_COUNT = "last_chore_count"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val groupId = inputData.getString(KEY_GROUP_ID)
            val deviceId = inputData.getString(KEY_DEVICE_ID)

            if (groupId.isNullOrEmpty() || deviceId.isNullOrEmpty()) {
                Log.w(TAG, "Missing groupId or deviceId, skipping check")
                return@withContext Result.success()
            }

            Log.d(TAG, "Checking for new chores for group: $groupId")

            // Fetch latest chore data from the server
            val api = ApiClient.instance
            val response = api.getGroup(mapOf("groupId" to groupId))
            val currentChores = response.group.chores ?: emptyList()
            val currentChoreCount = currentChores.size

            // Get the last known chore count
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastChoreCount = prefs.getInt(KEY_LAST_CHORE_COUNT, -1)

            Log.d(TAG, "Current chore count: $currentChoreCount, Last chore count: $lastChoreCount")

            // Check if there are new chores
            if (lastChoreCount != -1 && currentChoreCount > lastChoreCount) {
                val newChoresCount = currentChoreCount - lastChoreCount
                Log.d(TAG, "Found $newChoresCount new chore(s)")
                sendNotification(newChoresCount)
            }

            // Update the last known chore count
            prefs.edit().putInt(KEY_LAST_CHORE_COUNT, currentChoreCount).apply()

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for new chores", e)
            Result.retry()
        }
    }

    private fun sendNotification(newChoresCount: Int) {
        createNotificationChannel()

        // Create intent to open the app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = if (newChoresCount == 1) {
            "You have 1 new chore to do!"
        } else {
            "You have $newChoresCount new chores to do!"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_app) // Using the app icon
            .setContentTitle("New Things to Do! 🐝")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Check for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Notification permission not granted")
                return
            }
        }

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "Notification sent: $notificationText")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chore Updates"
            val descriptionText = "Notifications for new chores added to your group"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

