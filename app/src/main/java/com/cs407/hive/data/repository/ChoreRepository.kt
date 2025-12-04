package com.cs407.hive.data.repository

import com.cs407.hive.data.local.chore.ChoreDao
import com.cs407.hive.data.local.chore.ChoreEntity
import com.cs407.hive.data.model.AddChoreRequest
import com.cs407.hive.data.network.HiveApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class ChoreRepository(
    private val api: HiveApi,
    private val choreDao: ChoreDao
) {

    fun observeChores(): Flow<List<ChoreEntity>> = choreDao.observeChores()

    suspend fun syncChores(groupId: String): List<ChoreEntity> {
        val response = api.getGroup(mapOf("groupId" to groupId))
        val serverChores = response.group.chores.orEmpty()
        val entities = serverChores.map { chore ->
            ChoreEntity(
                id = choreId(chore.name, chore.description),
                name = chore.name,
                description = chore.description,
                points = chore.points,
                createdAt = System.currentTimeMillis()
            )
        }
        choreDao.clear()
        choreDao.upsertAll(entities)
        return entities
    }

    suspend fun addChore(request: AddChoreRequest) {
        api.addChore(request)
    }

    suspend fun newestChoreId(): String? = choreDao.observeChores().first().firstOrNull()?.id

    suspend fun currentChoreIds(): Set<String> = choreDao.getAll().map { it.id }.toSet()

    private fun choreId(name: String, description: String) = UUID.nameUUIDFromBytes("$name|$description".toByteArray()).toString()
}
