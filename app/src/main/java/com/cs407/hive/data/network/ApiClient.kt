package com.cs407.hive.data.network

import com.cs407.hive.data.model.GroupRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

object ApiClient {
    val instance: HiveApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // change to your server base URL. orig 10.0.2.2
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HiveApi::class.java)
    }


}

