package com.example.openwebuieink.network

import retrofit2.http.Body
import retrofit2.http.POST

@kotlinx.serialization.InternalSerializationApi
interface OpenAiApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(@Body request: ChatRequest): ChatResponse
}