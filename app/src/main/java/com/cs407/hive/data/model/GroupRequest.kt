package com.cs407.hive.data.model

data class GroupRequest(
    val groupName: String,
    val creatorName: String,
    val groupId: String,
    val peopleList: List<String>
)

data class CheckLoginResponse(
    val message: String,
    val existingGroup: GroupRequest? = null
)