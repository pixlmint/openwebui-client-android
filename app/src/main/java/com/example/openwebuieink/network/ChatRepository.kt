package com.example.openwebuieink.network

import com.example.openwebuieink.db.Settings
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class ChatRepository {

    private fun getClient(settings: Settings): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${settings.apiKey}")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build()
    }

    private fun getApi(settings: Settings): OpenAiApi {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }

        val retrofit = Retrofit.Builder()
            .baseUrl(settings.apiEndpoint)
            .client(getClient(settings))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(OpenAiApi::class.java)
    }

    suspend fun getChatCompletion(settings: Settings, request: ChatRequest): ChatResponse {
        return getApi(settings).getChatCompletion(request)
    }

    suspend fun getModels(settings: Settings): ModelsResponse {
        return getApi(settings).getModels()
    }
}