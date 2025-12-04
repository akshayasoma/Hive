package com.cs407.hive.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Test utilities for the ChoreCheckWorker.
 * Use these functions during development to test the worker without waiting 15 minutes.
 */
object WorkerTestUtils {
    private const val TAG = "WorkerTestUtils"

    /**
     * Runs the chore check worker immediately (one-time).
     * Useful for testing without waiting for the periodic interval.
     */
    fun runChoreCheckNow(context: Context, groupId: String, deviceId: String) {
        Log.d(TAG, "Running one-time chore check immediately")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            ChoreCheckWorker.KEY_GROUP_ID to groupId,
            ChoreCheckWorker.KEY_DEVICE_ID to deviceId
        )

        val workRequest = OneTimeWorkRequestBuilder<ChoreCheckWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d(TAG, "One-time chore check enqueued")
    }

    /**
     * Runs the chore check worker after a short delay (for testing).
     * @param delayMinutes Delay in minutes before running (minimum 1 minute)
     */
    fun runChoreCheckWithDelay(context: Context, groupId: String, deviceId: String, delayMinutes: Long = 1) {
        Log.d(TAG, "Scheduling chore check in $delayMinutes minute(s)")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            ChoreCheckWorker.KEY_GROUP_ID to groupId,
            ChoreCheckWorker.KEY_DEVICE_ID to deviceId
        )

        val workRequest = OneTimeWorkRequestBuilder<ChoreCheckWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d(TAG, "Delayed chore check enqueued")
    }

    /**
     * Resets the stored chore count to force a notification on next run.
     * Useful for testing notifications.
     */
    fun resetChoreCount(context: Context) {
        val prefs = context.getSharedPreferences("chore_check_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("last_chore_count").apply()
        Log.d(TAG, "Chore count reset - next run will initialize count")
    }
}

