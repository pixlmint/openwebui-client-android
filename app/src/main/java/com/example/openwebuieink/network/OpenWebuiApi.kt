package com.example.openwebuieink.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

@kotlinx.serialization.InternalSerializationApi
interface OpenWebuiApi {
    @POST("api/v1/chat/completions")
    suspend fun getChatCompletion(@Body request: ChatRequest): ChatResponse

    @GET("api/v1/models")
    suspend fun getModels(@Query("refresh") refresh: Boolean = false): ModelsResponse

    @GET("api/v1/chats/list")
    suspend fun getChats(): List<Chat>
}