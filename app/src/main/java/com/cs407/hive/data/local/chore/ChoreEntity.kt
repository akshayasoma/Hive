package com.cs407.hive.data.local.chore

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chores")
data class ChoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val points: Int,
    val createdAt: Long
)

