package com.example.openwebuieink.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@kotlinx.serialization.InternalSerializationApi
interface OpenAiApi {
    @POST("api/v1/chat/completions")
    suspend fun getChatCompletion(@Body request: ChatRequest): ChatResponse

    @GET("api/v1/models/list")
    suspend fun getModels(): ModelsResponse
}