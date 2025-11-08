package com.example.openwebuieink.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class ChatRepository {

    private fun getClient(apiKey: String?): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            if (apiKey != null) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            chain.proceed(requestBuilder.build())
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build()
    }

    @kotlinx.serialization.InternalSerializationApi
    private fun getApi(baseUrl: String, apiKey: String?): OpenWebuiApi {
        val contentType = "application/json".toMediaType()
        val json = Json { ignoreUnknownKeys = true }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getClient(apiKey))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(OpenWebuiApi::class.java)
    }

    @kotlinx.serialization.InternalSerializationApi
    suspend fun getChatCompletion(baseUrl: String, apiKey: String?, request: ChatRequest): ChatResponse {
        return getApi(baseUrl, apiKey).getChatCompletion(request)
    }

    @kotlinx.serialization.InternalSerializationApi
    suspend fun getModels(baseUrl: String, apiKey: String?): ModelsResponse {
        return getApi(baseUrl, apiKey).getModels()
    }

    @kotlinx.serialization.InternalSerializationApi
    suspend fun getChats(baseUrl: String, apiKey: String?): List<Chat> {
        return getApi(baseUrl, apiKey).getChats()
    }
}