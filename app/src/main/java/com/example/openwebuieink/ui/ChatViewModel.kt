package com.example.openwebuieink.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.network.Chat
import com.example.openwebuieink.network.ChatRepository
import com.example.openwebuieink.network.ChatService
import com.example.openwebuieink.network.Message
import com.example.openwebuieink.network.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatViewModel(private val mainViewModel: MainViewModel) :
    ViewModel() {

    private val repository = ChatRepository()
    private val chatService = ChatService(repository)

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat

    private val _chatHistory = MutableStateFlow<List<Message>>(emptyList())
    val chatHistory: StateFlow<List<Message>> = _chatHistory

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _restoreMessage = MutableStateFlow<String?>(null)
    val restoreMessage: StateFlow<String?> = _restoreMessage

    @kotlinx.serialization.InternalSerializationApi
    fun sendMessage(message: String, model: Model?) {
        viewModelScope.launch {
            val profile = mainViewModel.selectedConnectionProfile.first()
                ?: // Handle error: no profile selected
                return@launch
            val modelId = model?.id ?: return@launch // or a default model

            // Store original state for rollback on error
            val originalChat = _chat.value
            val originalHistory = _chatHistory.value

            chatService.sendMessage(
                profile,
                _chat.value,
                message,
                modelId
            )
                .catch { e ->
                    Log.e("ChatViewModel", "Error sending message: ${e.message}")
                    Log.e("ChatViewModel", e.stackTraceToString())

                    // Rollback to original state
                    _chat.value = originalChat
                    _chatHistory.value = originalHistory

                    // Restore the user's message to the input field
                    _restoreMessage.value = message

                    _errorMessage.value = "Error: ${e.message ?: "Unknown error occurred"}"
                }.onEach { updatedChat ->
                    _chat.value = updatedChat
                    _chatHistory.value = updatedChat.messages
                }.collect { updatedChat ->
                    _chat.value = updatedChat
                    _chatHistory.value = updatedChat.messages
                }
        }
    }

    fun loadChat(chatToLoad: Chat) {
        _chat.value = chatToLoad
        _chatHistory.value = chatToLoad.messages
}

    fun clearChat() {
        _chat.value = null
        _chatHistory.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearRestoreMessage() {
        _restoreMessage.value = null
    }
}
