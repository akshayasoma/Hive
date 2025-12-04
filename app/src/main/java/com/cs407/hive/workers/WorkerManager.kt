package com.cs407.hive.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object WorkerManager {
    private const val TAG = "WorkerManager"
    private const val CHORE_CHECK_WORK_NAME = "chore_check_periodic_work"

    /**
     * Schedules the periodic chore check worker to run every 15 minutes.
     * Note: WorkManager's minimum interval is 15 minutes, not 5 minutes.
     * For testing purposes, you can use a OneTimeWorkRequest with a shorter delay.
     */
    fun scheduleChoreCheckWorker(context: Context, groupId: String, deviceId: String) {
        Log.d(TAG, "Scheduling chore check worker for groupId: $groupId, deviceId: $deviceId")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            ChoreCheckWorker.KEY_GROUP_ID to groupId,
            ChoreCheckWorker.KEY_DEVICE_ID to deviceId
        )

        val workRequest = PeriodicWorkRequestBuilder<ChoreCheckWorker>(
            15, TimeUnit.MINUTES // Minimum interval is 15 minutes
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(CHORE_CHECK_WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CHORE_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        Log.d(TAG, "Chore check worker scheduled successfully")
    }

    /**
     * Cancels the periodic chore check worker.
     */
    fun cancelChoreCheckWorker(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CHORE_CHECK_WORK_NAME)
        Log.d(TAG, "Chore check worker cancelled")
    }
}

