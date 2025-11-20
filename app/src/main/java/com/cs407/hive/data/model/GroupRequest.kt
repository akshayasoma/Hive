package com.cs407.hive.data.model

data class GroupRequest(
    val groupName: String,
    val creatorName: String,
    val groupId: String,
    val creatorId: String,
//    val peopleList: List<String>
)

data class CheckLoginResponse(
    val message: String,
    val existingGroup: GroupRequest? = null
)

data class UserResponse(
    val user: UserDetail
)

data class UserDetail(
    val userId: String,
    val name: String,
    val preferences: Map<String, Any>?
)

data class GroupResponse(
    val group: GroupDetail
)

data class ChoreItem(
    val name: String,
    val description: String,
    val points: Int
)

data class GroupDetail(
    val groupName: String,
    val creatorId: String,
    val groupId: String,
    val peopleList: List<String>,
    val chores: List<ChoreItem>? = emptyList()
)

data class AddChoreRequest(
    val groupId: String,
    val deviceId: String,
    val name: String,
    val description: String,
    val points: Int
)
