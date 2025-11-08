package com.example.openwebuieink.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@kotlinx.serialization.InternalSerializationApi
class ChatRepository {
    private val openAiApi: OpenAiApi

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/") // Replace with your base URL
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        openAiApi = retrofit.create(OpenAiApi::class.java)
    }

    suspend fun getChatCompletion(request: ChatRequest): ChatResponse {
        return openAiApi.getChatCompletion(request)
    }
}