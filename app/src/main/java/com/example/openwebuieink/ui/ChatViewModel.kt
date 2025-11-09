package com.example.openwebuieink.ui

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.network.Chat
import com.example.openwebuieink.network.ChatCompletedRequest
import com.example.openwebuieink.network.ChatCompletionMessage
import com.example.openwebuieink.network.ChatCompletionsRequest
import com.example.openwebuieink.network.ChatRepository
import com.example.openwebuieink.network.ChatUpdateRequest
import com.example.openwebuieink.network.CreateChatRequest
import com.example.openwebuieink.network.History
import com.example.openwebuieink.network.Message
import com.example.openwebuieink.network.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application, private val mainViewModel: MainViewModel) :
    ViewModel() {

    private val repository = ChatRepository()

    private val _chat = MutableStateFlow<Chat?>(null)
    private var _activeTask: String? = null;
    private var _currentSession: String? = null;

    val chat: StateFlow<Chat?> = _chat

    private val _chatHistory = MutableStateFlow<List<Message>>(emptyList())
    val chatHistory: StateFlow<List<Message>> = _chatHistory

    lateinit var mainHandler: Handler;
    private var _refreshCounter: Long = 0;
    private final val MAX_REFRESHES = 30;


    private val triggerFetchChatCompletion = object : Runnable {
        override fun run() {
            fetchChatCompletion()
            _refreshCounter++
            if (_refreshCounter < MAX_REFRESHES)
                mainHandler.postDelayed(this, 2000)
        }
    }

    fun fetchChatCompletion() {
        Log.d("ChatViewModel", "Fetching chat completion (${_refreshCounter + 1} / ${MAX_REFRESHES})")
        viewModelScope.launch {
            val profile = mainViewModel.selectedConnectionProfile.first()
                ?: // Handle error: no profile selected
                return@launch
            val currentChat = _chat.value

            val updatedChat =
                repository.getUpdateChat(profile.baseUrl, profile.apiKey, currentChat?.id!!)

            if (updatedChat.chat.history.messages.size > currentChat.history.messages.size) {
                mainHandler.removeCallbacks(triggerFetchChatCompletion)

                val assistantMsgId = updatedChat.chat.history.currentId;
                val assistantMsg = updatedChat.chat.history.messages[assistantMsgId]!!
                val userMsg = updatedChat.chat.messages.last()

                userMsg.childrenIds += assistantMsgId

                val newMsg = Message(
                    id = assistantMsgId,
                    role = "assistant",
                    content = assistantMsg.content,
                    timestamp = System.currentTimeMillis() / 1000,
                    model = assistantMsg.model,
                    parentId = userMsg.id,
                )

                // Step 6: Complete Assistant Message
                val chatCompletedRequest = ChatCompletedRequest(
                    chatId = currentChat.id!!,
                    id = assistantMsgId,
                    model = assistantMsg.model!!,
                    sessionId = _currentSession!!
                )
                try {
                    repository.completeChat(profile.baseUrl, profile.apiKey, chatCompletedRequest)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error completing chat: ${e.message}")
                    Log.e("ChatViewModel", e.stackTraceToString())
                    // Handle error
                    return@launch
                }

                currentChat.messages = currentChat.messages + newMsg
                val newHistoryMessages = currentChat.history.messages.toMutableMap()
                newHistoryMessages[assistantMsgId] = newMsg
                currentChat.history = currentChat.history.copy(
                    currentId = assistantMsgId,
                    messages = newHistoryMessages
                )
                try {
                    val updateRequest = ChatUpdateRequest(
                        chat = currentChat
                    )
                    repository.updateChat(
                        profile.baseUrl,
                        profile.apiKey,
                        currentChat.id!!,
                        updateRequest
                    )
                    _chat.value = currentChat
                    _chatHistory.value = currentChat.messages
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error updating chat: ${e.message}")
                    Log.e("ChatViewModel", e.stackTraceToString())
                    // Handle error
                    return@launch
                }
            }
        }
    }

    @kotlinx.serialization.InternalSerializationApi
    fun sendMessage(message: String, model: Model?) {
        viewModelScope.launch {
            val profile = mainViewModel.selectedConnectionProfile.first()
                ?: // Handle error: no profile selected
                return@launch
            val modelId = model?.id ?: return@launch // or a default model

            val userMessageId = "user-msg-${UUID.randomUUID()}"
            val userMessage = Message(
                id = userMessageId,
                role = "user",
                content = message,
                timestamp = System.currentTimeMillis() / 1000,
                models = listOf(modelId)
            )

            var currentChat = _chat.value
            if (currentChat == null) {
                // Step 1: Create Chat with User Message
                val newChat = Chat(
                    title = "New Chat",
                    models = listOf(modelId),
                    messages = listOf(userMessage),
                    history = History(
                        currentId = userMessageId,
                        messages = mapOf(userMessageId to userMessage)
                    ),
                    timestamp = System.currentTimeMillis() / 1000
                )
                try {
                    val createChatRequest = CreateChatRequest(
                        chat = newChat
                    )
                    val createChatResponse =
                        repository.createChat(profile.baseUrl, profile.apiKey, createChatRequest)
                    currentChat = createChatResponse.chat
                    if (currentChat.id == null) {
                        currentChat.id = createChatResponse.id
                    }
                    _chat.value = currentChat
                    _chatHistory.value = currentChat.messages
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error creating chat: ${e.message}")
                    Log.e("ChatViewModel", e.stackTraceToString())
                    // Handle error
                    return@launch
                }
            } else {
                val parentMsg = currentChat.history.currentId
                userMessage.parentId = currentChat.history.currentId
                currentChat.history.messages[parentMsg]!!.childrenIds += userMessageId
                currentChat.messages = currentChat.messages + userMessage
                val newHistoryMessages = currentChat.history.messages.toMutableMap()
                newHistoryMessages[userMessageId] = userMessage
                currentChat.history = currentChat.history.copy(
                    currentId = userMessageId,
                    messages = newHistoryMessages
                )
                val req = ChatUpdateRequest(
                    chat = currentChat
                )
                try {
                    repository.updateChat(
                        profile.baseUrl,
                        profile.apiKey,
                        currentChat.id!!,
                        req
                    )
                    _chat.value = currentChat
                    _chatHistory.value = currentChat.messages
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error updating chat: ${e.message}")
                    Log.e("ChatViewModel", e.stackTraceToString())
                    // Handle error
                    return@launch
                }
            }


            val assistantMessageId = "assistant-msg-${UUID.randomUUID()}"

            _currentSession = "session-${UUID.randomUUID()}"
            // Step 4: Trigger Assistant Completion
            val completionsMessages = currentChat.messages.map {
                ChatCompletionMessage(
                    role = it.role,
                    content = it.content,
                )
            } + ChatCompletionMessage(role = "user", content = message)
            val chatCompletionsRequest = ChatCompletionsRequest(
                chatId = currentChat.id!!,
                id = assistantMessageId,
                messages = completionsMessages,
                model = modelId,
                sessionId = _currentSession!!,
                stream = false,
            )
            try {
                val task = repository.getChatCompletions(
                    profile.baseUrl,
                    profile.apiKey,
                    chatCompletionsRequest
                )

                if (task.status) {
                    _activeTask = task.taskId

                    mainHandler = Handler(Looper.getMainLooper())

                    mainHandler.post(triggerFetchChatCompletion)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error getting chat completions: ${e.message}")
                Log.e("ChatViewModel", e.stackTraceToString())
                // Handle error
                return@launch
            }
        }
    }

    fun clearChat() {
        _chat.value = null
        _chatHistory.value = emptyList()
    }
}
