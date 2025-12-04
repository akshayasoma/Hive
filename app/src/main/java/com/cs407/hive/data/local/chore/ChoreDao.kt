package com.cs407.hive.data.local.chore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoreDao {
    @Query("SELECT * FROM chores ORDER BY createdAt DESC")
    fun observeChores(): Flow<List<ChoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(chores: List<ChoreEntity>)

    @Query("SELECT * FROM chores")
    suspend fun getAll(): List<ChoreEntity>

    @Query("DELETE FROM chores")
    suspend fun clear()
}
