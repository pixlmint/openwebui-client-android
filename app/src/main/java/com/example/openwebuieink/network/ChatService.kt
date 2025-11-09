package com.example.openwebuieink.network

import android.util.Log
import com.example.openwebuieink.data.ConnectionProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import java.util.UUID

class ChatService(private val repository: ChatRepository) {

    fun sendMessage(
        profile: ConnectionProfile,
        chat: Chat?,
        message: String,
        modelId: String
    ): Flow<Chat> = flow {
        val userMessageId = "user-msg-${UUID.randomUUID()}"
        val userMessage = Message(
            id = userMessageId,
            role = "user",
            content = message,
            timestamp = System.currentTimeMillis() / 1000,
            models = listOf(modelId)
        )

        val currentChat = if (chat == null) {
            createNewChat(profile, userMessage, modelId)
        } else {
            updateChatWithMessage(profile, chat, userMessage)
        }
        emit(currentChat)

        val assistantMessageId = "assistant-msg-${UUID.randomUUID()}"
        val currentSession = "session-${UUID.randomUUID()}"
        var assistantMessage: Message? = null
        var updatedChat: Chat? = null

        streamChatCompletion(profile, currentChat, assistantMessageId, currentSession, modelId)
            .onEach {
                emit(it)
            }
            .collect {
                assistantMessage = it.messages.last()
                updatedChat = it
                emit(updatedChat)
            }

        updateChatWithMessage(profile, currentChat, assistantMessage!!);

        completeChat(profile, updatedChat!!, assistantMessageId, currentSession, modelId)
    }

    private suspend fun createNewChat(
        profile: ConnectionProfile,
        userMessage: Message,
        modelId: String
    ): Chat {
        val newChat = Chat(
            title = "New Chat",
            models = listOf(modelId),
            messages = listOf(userMessage),
            history = History(
                currentId = userMessage.id!!,
                messages = mapOf(userMessage.id to userMessage)
            ),
            timestamp = System.currentTimeMillis() / 1000
        )
        val createChatRequest = CreateChatRequest(chat = newChat)
        val createChatResponse =
            repository.createChat(profile.baseUrl, profile.apiKey, createChatRequest)
        val createdChat = createChatResponse.chat
        if (createdChat.id == null) {
            createdChat.id = createChatResponse.id
        }
        return createdChat
    }

    private suspend fun updateChatWithMessage(profile: ConnectionProfile, chat: Chat, userMessage: Message): Chat {
        val parentMsgId = chat.history.currentId
        userMessage.parentId = parentMsgId
        chat.history.messages[parentMsgId]!!.childrenIds += userMessage.id!!
        val updatedMessages = chat.messages + userMessage
        val newHistoryMessages = chat.history.messages.toMutableMap()
        newHistoryMessages[userMessage.id] = userMessage
        val updatedHistory = chat.history.copy(
            currentId = userMessage.id,
            messages = newHistoryMessages
        )
        val updatedChat = chat.copy(
            messages = updatedMessages,
            history = updatedHistory
        )
        val req = ChatUpdateRequest(chat = updatedChat)
        repository.updateChat(profile.baseUrl, profile.apiKey, chat.id!!, req)
        return updatedChat
    }

    private fun streamChatCompletion(
        profile: ConnectionProfile,
        chat: Chat,
        assistantMessageId: String,
        sessionId: String,
        modelId: String,
    ): Flow<Chat> = flow {
        val completionsMessages =
            chat.messages.map { ChatCompletionMessage(role = it.role, content = it.content) }

        val chatCompletionsRequest = ChatCompletionsRequest(
            chatId = chat.id!!,
            id = assistantMessageId,
            messages = completionsMessages,
            model = modelId,
            sessionId = sessionId,
            stream = true,
        )

        val responseBody = repository.streamChatCompletions(
            profile.baseUrl,
            profile.apiKey,
            chatCompletionsRequest
        )

        val source = responseBody.source()
        var assistantMessage = ""
        var lastEmitTime = 0L
        val emitInterval = 2000L // 2 seconds

        try {
            while (!source.exhausted()) {
                val line = source.readUtf8Line()
                Log.d("ChatService", "Received line: $line")
                if (line != null && line.startsWith("data:") && !line.endsWith("[DONE]")) {
                    val json = line.removePrefix("data:").trim()
                    if (json.isNotEmpty()) {
                        try {
                            val chunk = Json.decodeFromString<StreamingChatCompletionChunk>(json)
                            chunk.choices.firstOrNull()?.delta?.content?.let {
                                assistantMessage += it
                            }

                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastEmitTime >= emitInterval) {
                                val updatedChat = handleUpdateChat(assistantMessageId, assistantMessage, modelId, chat)

                                emit(updatedChat)
                                lastEmitTime = currentTime
                                delay(100)
                            }
                        } catch (e: Exception) {
                            Log.e("ChatService", "Error parsing chunk: $json", e)
                        }
                    }
                }
            }
            emit(handleUpdateChat(assistantMessageId, assistantMessage, modelId, chat))
        } catch (e: Exception) {
            Log.e("ChatService", "Error streaming chat completion", e)
            throw e
        } finally {
            Log.d("ChatService", "Closing response body")
            responseBody.close()
        }
    }

    @InternalSerializationApi
    private fun handleUpdateChat(
        assistantMessageId: String,
        assistantMessage: String,
        modelId: String,
        chat: Chat
    ): Chat {
        val newMsg = Message(
            id = assistantMessageId,
            role = "assistant",
            content = assistantMessage, // Updated content
            timestamp = System.currentTimeMillis() / 1000,
            model = modelId,
            parentId = chat.history.currentId,
        )

        val newMessages =
            chat.messages.filter { it.id != assistantMessageId } + newMsg
        val newHistoryMessages = chat.history.messages.toMutableMap()
        newHistoryMessages[assistantMessageId] = newMsg

        val updatedChat = chat.copy(
            messages = newMessages,
            history = chat.history.copy(
                currentId = assistantMessageId,
                messages = newHistoryMessages
            )
        )
        return updatedChat
    }

    private suspend fun completeChat(
        profile: ConnectionProfile,
        chat: Chat,
        assistantMsgId: String,
        sessionId: String,
        modelId: String
    ) {
        val chatCompletedRequest = ChatCompletedRequest(
            chatId = chat.id!!,
            id = assistantMsgId,
            model = modelId,
            sessionId = sessionId
        )
        repository.completeChat(profile.baseUrl, profile.apiKey, chatCompletedRequest)
    }
}
