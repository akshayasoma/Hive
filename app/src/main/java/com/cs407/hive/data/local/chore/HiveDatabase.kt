package com.cs407.hive.data.local.chore

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChoreEntity::class], version = 1, exportSchema = false)
abstract class HiveDatabase : RoomDatabase() {
    abstract fun choreDao(): ChoreDao

    companion object {
        @Volatile private var INSTANCE: HiveDatabase? = null

        fun getInstance(context: Context): HiveDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                HiveDatabase::class.java,
                "hive.db"
            ).build().also { INSTANCE = it }
        }
    }
}

