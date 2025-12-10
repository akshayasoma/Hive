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

data class CompletedChore(
    val name: String,
    val description: String,
    val points: Int,
    val completedAt: Long
)

data class CompleteChoreRequest(
    val groupId: String,
    val deviceId: String,
    val choreName: String,
    val description: String,
    val points: Int
)

data class UserDetail(
    val userId: String,
    val name: String,
    val preferences: Map<String, Any>?,
    val points: Int,
    val profilePic: String,
    val choreRegister: List<CompletedChore>,
)

data class UserNamesResponse(
    val names: List<String>
)


data class LeaderboardEntry(
    val name: String,
    val points: Int
)

data class LeaderboardResponse(
    val leaderboard: List<LeaderboardEntry>
)

data class GetUserNamesRequest(
    val groupId: String,
    val deviceId: String
)


data class GroupResponse(
    val group: GroupDetail
)

data class ChoreItem(
    val name: String,
    val description: String,
    val points: Int,
    val status: Int,
    val assignee: String
)

data class GroupDetail(
    val groupName: String,
    val creatorId: String,
    val groupId: String,
    val peopleList: List<String>,
    val chores: List<ChoreItem>? = emptyList(),
    val groceries: List<GroceryItem>? = emptyList()
)

data class AddChoreRequest(
    val groupId: String,
    val deviceId: String,
    val name: String,
    val description: String,
    val points: Int,
    val assignee: String = "",
    val status: Int = 0
)

data class DeleteChoreRequest(
    val groupId: String,
    val deviceId: String,
    val choreName: String,
    val description: String,
    val points: Int,
    val status: Int,
    val assignee: String
)

data class DeleteGroupRequest(
    val groupId: String,
    val deviceId: String
)

data class UpdateUserNameRequest(
    val userId: String,
    val newName: String
)

data class UpdateGroupNameRequest(
    val groupId: String,
    val deviceId: String,
    val newName: String
)

data class JoinGroupRequest(
    val groupId: String,
    val deviceId: String,
    val userName: String
)

data class LeaveGroupRequest(
    val groupId: String,
    val deviceId: String
)

data class UiChore(
    val name: String,
    val description: String,
    val points: Int,
    val status: Int,
    val assignee: String
)

data class UiGrocery(
    val name: String,
    val description: String,
    val completed: Boolean
)

data class GroceryItem(
    val name: String,
    val description: String,
    val completed: Boolean
)

data class AddGroceryRequest(
    val groupId: String,
    val deviceId: String,
    val name: String,
    val description: String,
    val completed: Boolean = false
)

data class DeleteGroceryRequest(
    val groupId: String,
    val deviceId: String,
    val name: String,
    val description: String,
    val completed: Boolean
)

data class UpdateProfilePicRequest(
    val deviceId: String,
    val profilePic: String
)

data class UpdateProfilePicResponse(
    val message: String,
    val profilePic: String
)
