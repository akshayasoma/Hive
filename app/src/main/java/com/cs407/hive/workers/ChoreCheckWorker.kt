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
        const val CHANNEL_ID = "hive_updates"
        const val NOTIFICATION_ID_CHORES = 1001
        const val NOTIFICATION_ID_GROCERIES = 1002
        const val NOTIFICATION_ID_GROUP_NAME = 1003
        private const val PREFS_NAME = "hive_check_prefs"
        private const val KEY_LAST_CHORE_COUNT = "last_chore_count"
        private const val KEY_LAST_GROCERY_COUNT = "last_grocery_count"
        private const val KEY_LAST_GROUP_NAME = "last_group_name"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val groupId = inputData.getString(KEY_GROUP_ID)
            val deviceId = inputData.getString(KEY_DEVICE_ID)

            if (groupId.isNullOrEmpty() || deviceId.isNullOrEmpty()) {
                Log.w(TAG, "Missing groupId or deviceId, skipping check")
                return@withContext Result.success()
            }

            Log.d(TAG, "Checking for updates for group: $groupId")

            // Fetch latest data from the server
            val api = ApiClient.instance
            val response = api.getGroup(mapOf("groupId" to groupId))
            val groupData = response.group

            val currentChores = groupData.chores ?: emptyList()
            val currentGroceries = groupData.groceries ?: emptyList()
            val currentGroupName = groupData.groupName

            val currentChoreCount = currentChores.size
            val currentGroceryCount = currentGroceries.size

            // Get the last known values
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastChoreCount = prefs.getInt(KEY_LAST_CHORE_COUNT, -1)
            val lastGroceryCount = prefs.getInt(KEY_LAST_GROCERY_COUNT, -1)
            val lastGroupName = prefs.getString(KEY_LAST_GROUP_NAME, null)

            Log.d(TAG, "Chores: $currentChoreCount (was $lastChoreCount)")
            Log.d(TAG, "Groceries: $currentGroceryCount (was $lastGroceryCount)")
            Log.d(TAG, "Group name: $currentGroupName (was $lastGroupName)")

            // Check for new chores
            if (lastChoreCount != -1 && currentChoreCount > lastChoreCount) {
                val newChoresCount = currentChoreCount - lastChoreCount
                Log.d(TAG, "Found $newChoresCount new chore(s)")
                sendChoreNotification(newChoresCount)
            }

            // Check for new groceries
            if (lastGroceryCount != -1 && currentGroceryCount > lastGroceryCount) {
                val newGroceriesCount = currentGroceryCount - lastGroceryCount
                Log.d(TAG, "Found $newGroceriesCount new grocery item(s)")
                sendGroceryNotification(newGroceriesCount)
            }

            // Check for group name change
            if (lastGroupName != null && lastGroupName != currentGroupName) {
                Log.d(TAG, "Group name changed from '$lastGroupName' to '$currentGroupName'")
                sendGroupNameNotification(currentGroupName)
            }

            // Update the last known values
            prefs.edit()
                .putInt(KEY_LAST_CHORE_COUNT, currentChoreCount)
                .putInt(KEY_LAST_GROCERY_COUNT, currentGroceryCount)
                .putString(KEY_LAST_GROUP_NAME, currentGroupName)
                .apply()

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            Result.retry()
        }
    }

    private fun sendChoreNotification(newChoresCount: Int) {
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
            .setSmallIcon(R.mipmap.ic_app)
            .setContentTitle("New Chores! 🐝")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        sendNotificationIfPermitted(NOTIFICATION_ID_CHORES, notification, notificationText)
    }

    private fun sendGroceryNotification(newGroceriesCount: Int) {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = if (newGroceriesCount == 1) {
            "1 new item added to grocery list!"
        } else {
            "$newGroceriesCount new items added to grocery list!"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_app)
            .setContentTitle("New Groceries! 🛒")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        sendNotificationIfPermitted(NOTIFICATION_ID_GROCERIES, notification, notificationText)
    }

    private fun sendGroupNameNotification(newGroupName: String) {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = "Your group is now called \"$newGroupName\""

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_app)
            .setContentTitle("Group Name Changed! 📝")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        sendNotificationIfPermitted(NOTIFICATION_ID_GROUP_NAME, notification, notificationText)
    }

    private fun sendNotificationIfPermitted(notificationId: Int, notification: android.app.Notification, logMessage: String) {
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

        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        Log.d(TAG, "Notification sent: $logMessage")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hive Updates"
            val descriptionText = "Notifications for chores, groceries, and group changes"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

