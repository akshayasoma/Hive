package com.cs407.hive

import android.app.Application
import com.cs407.hive.data.local.chore.HiveDatabase
import com.cs407.hive.data.network.ApiClient
import com.cs407.hive.data.repository.ChoreRepository
import com.cs407.hive.notifications.ChoreNotifier

class HiveApp : Application() {

    lateinit var database: HiveDatabase
        private set

    lateinit var choreRepository: ChoreRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = HiveDatabase.getInstance(this)
        choreRepository = ChoreRepository(ApiClient.instance, database.choreDao())
        ChoreNotifier.createChannel(this)
    }
}

