package com.cs407.hive.data.network

import com.cs407.hive.data.model.CheckLoginResponse
import com.cs407.hive.data.model.GroupRequest
import com.cs407.hive.data.model.GroupResponse
import com.cs407.hive.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface HiveApi {
    @POST("api/groups")
    suspend fun createGroup(@Body group: GroupRequest)

    @POST("/api/checkLogin")
    suspend fun checkLogin(@Body body: Map<String, String>): CheckLoginResponse

    @POST("/api/user/get")
    suspend fun getUser(@Body body: Map<String, String>): UserResponse

    @POST("/api/group/get")
    suspend fun getGroup(@Body body: Map<String, String>): GroupResponse

}