package com.example.openwebuieink.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class Message(
    val id: String? = null,
    val role: String,
    var content: String,
    val timestamp: Long? = null,
    val models: List<String>? = null,
    val model: String? = null,
    @SerialName("parentId") var parentId: String? = null,
    @SerialName("modelName") val modelName: String? = null,
    @SerialName("modelIdx") val modelIdx: Int? = null,
    @SerialName("childrenIds") var childrenIds: List<String> = emptyList(),
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class History(
    val currentId: String,
    val messages: Map<String, Message>
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class Tag(
    val id: String,
    val name: String,
    val color: String
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class Chat(
    var id: String? = null,
    var title: String,
    var models: List<String>,
    val files: List<String> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val params: Map<String, String> = emptyMap(),
    val timestamp: Long,
    var messages: List<Message>,
    var history: History
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ChatCompletionMessage(
    val role: String,
    val content: String
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class BackgroundTasks(
    @SerialName("title_generation") val titleGeneration: Boolean = false,
    @SerialName("tags_generation") val tagsGeneration: Boolean = false,
    @SerialName("follow_up_generation") val followUpGeneration: Boolean = false
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class Features(
    @SerialName("code_interpreter") val codeInterpreter: Boolean = false,
    @SerialName("web_search") val webSearch: Boolean = false,
    @SerialName("image_generation") val imageGeneration: Boolean = false,
    val memory: Boolean = false
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class KnowledgeFile(
    val id: String,
    val type: String,
    val status: String
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class UsageStats(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int,
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ListChat(
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("updated_at")
    val updatedAt: Long,
    @SerialName("created_at")
    val createdAt: Long
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class TaskChoice(
    val index: Int,
    val message: Message
)

