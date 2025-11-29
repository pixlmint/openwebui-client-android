package com.example.openwebuieink.network

import android.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class ChatRepository {

    private fun getClient(apiKey: String?, fullLogging: Boolean = true): OkHttpClient {
        val httpLogLevel = if (fullLogging) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.HEADERS
        }
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = httpLogLevel
        }

        val errorJsonInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code >= 400) {
                val body = response.peekBody(Long.MAX_VALUE)
                val bodyString = body.string()
                if (bodyString.isNotBlank()) {
                    try {
                        val jsonObject = JSONObject(bodyString)
                        Log.d("ChatRepository", "Error JSON: ${jsonObject.toString(4)}")
                    } catch (e: JSONException) {
                        try {
                            val jsonArray = JSONArray(bodyString)
                            Log.d("ChatRepository", "Error JSON: ${jsonArray.toString(4)}")
                        } catch (e2: JSONException) {
                            // Not a JSON response
                        }
                    }
                }
            }
            response
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
            .addInterceptor(errorJsonInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build()
    }

    private fun getApi(baseUrl: String, apiKey: String?, fullLogging: Boolean = true): OpenWebuiApi {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getClient(apiKey, fullLogging))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(OpenWebuiApi::class.java)
    }

    suspend fun getModels(baseUrl: String, apiKey: String?): ModelsResponse {
        return getApi(baseUrl, apiKey).getModels()
    }

    suspend fun getChats(baseUrl: String, apiKey: String?): List<ListChat> {
        return getApi(baseUrl, apiKey).getChats()
    }

    @kotlinx.serialization.InternalSerializationApi
    suspend fun getChat(baseUrl: String, apiKey: String?, chatId: String): Chat {
        val response = getApi(baseUrl, apiKey).getUpdateChat(chatId)
        return response.chat.toChat()
    }

    suspend fun createChat(baseUrl: String, apiKey: String?, chat: CreateChatRequest): CreateChatResponse {
        return getApi(baseUrl, apiKey).createChat(chat)
    }

    suspend fun updateChat(baseUrl: String, apiKey: String?, chatId: String, req: ChatUpdateRequest): ChatUpdateResponse {
        return getApi(baseUrl, apiKey).updateChat(chatId, req)
    }

    suspend fun getChatCompletions(baseUrl: String, apiKey: String?, request: ChatCompletionsRequest): ChatCompletionsResponse {
        return getApi(baseUrl, apiKey).getChatCompletions(request)
    }

    suspend fun generateTitle(baseUrl: String, apiKey: String?, request: ChatCompletionsRequest): TaskResponse {
        return getApi(baseUrl, apiKey).generateTitle(request)
    }

    suspend fun streamChatCompletions(
        baseUrl: String,
        apiKey: String?,
        request: ChatCompletionsRequest
    ): ResponseBody {
        return getApi(baseUrl, apiKey, false).streamChatCompletions(request)
    }

    suspend fun getUpdateChat(baseUrl: String, apiKey: String?, chatId: String): ChatUpdateResponse {
        return getApi(baseUrl, apiKey).getUpdateChat(chatId)
    }

    suspend fun completeChat(baseUrl: String, apiKey: String?, request: ChatCompletedRequest) {
        getApi(baseUrl, apiKey).completeChat(request)
    }
}