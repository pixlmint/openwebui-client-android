package com.example.openwebuieink.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@kotlinx.serialization.InternalSerializationApi
interface OpenWebuiApi {
    @GET("api/v1/models")
    suspend fun getModels(@Query("refresh") refresh: Boolean = false): ModelsResponse

    @GET("api/v1/chats/list")
    suspend fun getChats(): List<ListChat>

    @POST("api/v1/chats/new")
    suspend fun createChat(@Body chat: CreateChatRequest): CreateChatResponse

    @POST("api/v1/chats/{chatId}")
    suspend fun updateChat(@Path("chatId") chatId: String, @Body request: ChatUpdateRequest): ChatUpdateResponse

    @GET("api/v1/chats/{chatId}")
    suspend fun getUpdateChat(@Path("chatId") chatId: String): ChatUpdateResponse

    @POST("api/chat/completions")
    suspend fun getChatCompletions(@Body request: ChatCompletionsRequest): ChatCompletionsResponse

    @POST("api/chat/completed")
    suspend fun completeChat(@Body request: ChatCompletedRequest)
}