package com.example.openwebuieink.network

import android.util.Log
import com.example.openwebuieink.data.ConnectionProfile
import kotlinx.coroutines.delay
import java.util.UUID

class ChatService(private val repository: ChatRepository) {

    private companion object {
        private const val MAX_REFRESHES = 30
        private const val REFRESH_INTERVAL = 2000L
    }

    @kotlinx.serialization.InternalSerializationApi
    suspend fun sendMessage(
        profile: ConnectionProfile,
        chat: Chat?,
        message: String,
        modelId: String
    ): Pair<Chat, String?> {
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

        val assistantMessageId = "assistant-msg-${UUID.randomUUID()}"
        val currentSession = "session-${UUID.randomUUID()}"

        val task = triggerAssistantCompletion(
            profile,
            currentChat,
            assistantMessageId,
            currentSession,
            modelId,
            message
        )

        if (task.status) {
            val updatedChat = pollForChatCompletion(profile, currentChat)
            completeChat(profile, updatedChat, assistantMessageId, currentSession, modelId)
            return Pair(updatedChat, task.taskId)
        }
        return Pair(currentChat, null)
    }

    @kotlinx.serialization.InternalSerializationApi
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

    @kotlinx.serialization.InternalSerializationApi
    private suspend fun triggerAssistantCompletion(
        profile: ConnectionProfile,
        chat: Chat,
        assistantMessageId: String,
        sessionId: String,
        modelId: String,
        userMessage: String,
    ): ChatCompletionsResponse {
        val completionsMessages =
            chat.messages.map { ChatCompletionMessage(role = it.role, content = it.content) } +
                    ChatCompletionMessage(role = "user", content = userMessage)

        val chatCompletionsRequest = ChatCompletionsRequest(
            chatId = chat.id!!,
            id = assistantMessageId,
            messages = completionsMessages,
            model = modelId,
            sessionId = sessionId,
            stream = false,
        )
        return repository.getChatCompletions(
            profile.baseUrl,
            profile.apiKey,
            chatCompletionsRequest
        )
    }

    private suspend fun pollForChatCompletion(
        profile: ConnectionProfile,
        chat: Chat
    ): Chat {
        var updatedChat = chat
        for (i in 0 until MAX_REFRESHES) {
            Log.d("ChatService", "Fetching chat completion (${i + 1} / $MAX_REFRESHES)")
            val remoteChatUpdate = repository.getUpdateChat(profile.baseUrl, profile.apiKey, chat.id!!)
            if (remoteChatUpdate.chat.history.messages.size > updatedChat.history.messages.size) {
                val assistantMsgId = remoteChatUpdate.chat.history.currentId
                val assistantMsg = remoteChatUpdate.chat.history.messages[assistantMsgId]!!
                val userMsg = remoteChatUpdate.chat.messages.last()
                userMsg.childrenIds += assistantMsgId

                val newMsg = Message(
                    id = assistantMsgId,
                    role = "assistant",
                    content = assistantMsg.content,
                    timestamp = System.currentTimeMillis() / 1000,
                    model = assistantMsg.model,
                    parentId = userMsg.id,
                )
                val newMessages = updatedChat.messages + newMsg
                val newHistoryMessages = updatedChat.history.messages.toMutableMap()
                newHistoryMessages[assistantMsgId] = newMsg
                updatedChat = updatedChat.copy(
                    messages = newMessages,
                    history = updatedChat.history.copy(
                        currentId = assistantMsgId,
                        messages = newHistoryMessages
                    )
                )
                val updateRequest = ChatUpdateRequest(chat = updatedChat)
                repository.updateChat(profile.baseUrl, profile.apiKey, updatedChat.id!!, updateRequest)
                return updatedChat
            }
            delay(REFRESH_INTERVAL)
        }
        return updatedChat
    }

    @kotlinx.serialization.InternalSerializationApi
    private suspend fun completeChat(
        profile: ConnectionProfile,
        chat: Chat,
        assistantMsgId: String,
        sessionId: String,
        modelId: String
    ) {
        val assistantMsg = chat.history.messages[assistantMsgId]!!
        val chatCompletedRequest = ChatCompletedRequest(
            chatId = chat.id!!,
            id = assistantMsgId,
            model = modelId,
            sessionId = sessionId
        )
        repository.completeChat(profile.baseUrl, profile.apiKey, chatCompletedRequest)
    }
}
