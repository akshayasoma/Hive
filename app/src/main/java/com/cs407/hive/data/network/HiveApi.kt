package com.cs407.hive.data.network

import com.cs407.hive.data.model.AddChoreRequest
import com.cs407.hive.data.model.CheckLoginResponse
import com.cs407.hive.data.model.DeleteChoreRequest
import com.cs407.hive.data.model.GroupRequest
import com.cs407.hive.data.model.GroupResponse
import com.cs407.hive.data.model.JoinGroupRequest
import com.cs407.hive.data.model.LeaveGroupRequest
import com.cs407.hive.data.model.UpdateGroupNameRequest
import com.cs407.hive.data.model.UpdateUserNameRequest
import com.cs407.hive.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface HiveApi {
    @POST("api/groups")
    suspend fun createGroup(@Body group: GroupRequest)

    @POST("/api/group/join")
    suspend fun joinGroup(@Body req: JoinGroupRequest): GroupResponse

    @POST("/api/group/leave")
    suspend fun leaveGroup(@Body req: LeaveGroupRequest): GroupResponse

    @POST("/api/checkLogin")
    suspend fun checkLogin(@Body body: Map<String, String>): CheckLoginResponse

    @POST("/api/user/get")
    suspend fun getUser(@Body body: Map<String, String>): UserResponse

    @POST("/api/group/get")
    suspend fun getGroup(@Body body: Map<String, String>): GroupResponse

    @POST("/api/group/addChore")
    suspend fun addChore(@Body body: AddChoreRequest)

    @POST("/api/group/deleteChore")
    suspend fun deleteChore(@Body req: DeleteChoreRequest)


    @POST("/api/group/delete")
    suspend fun deleteGroup(@Body body: Map<String, String>)

    @POST("/api/user/updateName")
    suspend fun updateUserName(@Body req: UpdateUserNameRequest)

    @POST("/api/group/updateName")
    suspend fun updateGroupName(@Body req: UpdateGroupNameRequest)



}