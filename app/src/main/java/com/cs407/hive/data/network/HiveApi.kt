package com.cs407.hive.data.network

import com.cs407.hive.data.model.CheckLoginResponse
import com.cs407.hive.data.model.GroupRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface HiveApi {
    @POST("api/groups")
    suspend fun createGroup(@Body group: GroupRequest)

    @POST("/api/checkLogin")
    suspend fun checkLogin(@Body body: Map<String, String>): CheckLoginResponse


}