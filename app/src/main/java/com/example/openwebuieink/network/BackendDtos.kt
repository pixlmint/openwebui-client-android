package com.example.openwebuieink.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class CreateChatRequest(
    val chat: Chat
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class CreateChatResponse(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val chat: Chat,
    @SerialName("updated_at") val updatedAt: Long,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("share_id") val shareId: String?,
    val archived: Boolean,
    val pinned: Boolean
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ChatCompletionsRequest(
    @SerialName("chat_id") val chatId: String,
    val id: String,
    val messages: List<ChatCompletionMessage>,
    val model: String,
    val stream: Boolean,
    @SerialName("background_tasks") val backgroundTasks: BackgroundTasks? = null,
    val features: Features? = null,
    val variables: Map<String, String>? = null,
    @SerialName("session_id") val sessionId: String,
    @SerialName("filter_ids") val filterIds: List<String> = emptyList(),
    val files: List<KnowledgeFile> = emptyList()
)


@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ChatCompletionsResponse(
    val status: Boolean,
    @SerialName("task_id") val taskId: String
)


@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ChatCompletedRequest(
    val model: String,
    @SerialName("chat_id") val chatId: String,
    val id: String,
    @SerialName("session_id") val sessionId: String
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ChatUpdateRequest(
    val chat: Chat,
    @SerialName("folder_id") val folderId: String = ""
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ChatUpdateResponse(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val chat: MinimalChat,
    @SerialName("updated_at") val updatedAt: Long,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("share_id") val shareId: String?,
    val archived: Boolean,
    val pinned: Boolean,
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class MinimalChat(
    var id: String? = null,
    var title: String,
    var models: List<String>,
    val files: List<String> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val params: Map<String, String> = emptyMap(),
    val timestamp: Long,
    var messages: List<Message>,
    var history: MinimalHistory
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class MinimalHistory(
    val currentId: String,
    val messages: Map<String, MinimalMessage>
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class MinimalMessage(
    val role: String,
    val content: String,
    val models: List<String>? = null,
    val model: String? = null,
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class TaskResponse(
    val id: String,
    @SerialName("object") val obj: String,
    val created: Long,
    val model: String,
    val choices: List<TaskChoice>,
    val usage: UsageStats
)
