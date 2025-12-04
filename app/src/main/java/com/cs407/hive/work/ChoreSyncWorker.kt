package com.cs407.hive.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.cs407.hive.HiveApp
import com.cs407.hive.notifications.ChoreNotifier

class ChoreSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? HiveApp ?: return Result.failure()
        val groupId = inputData.getString(KEY_GROUP_ID) ?: return Result.success()

        return try {
            val beforeIds = app.choreRepository.currentChoreIds()
            val synced = app.choreRepository.syncChores(groupId)
            val newChores = synced.filterNot { beforeIds.contains(it.id) }
            newChores.forEach { ChoreNotifier.notifyNewChore(applicationContext, it.name) }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_GROUP_ID = "group_id"
    }
}
