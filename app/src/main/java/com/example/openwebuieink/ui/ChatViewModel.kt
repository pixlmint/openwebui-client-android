package com.example.openwebuieink.ui

import android.app.Application
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, private val mainViewModel: MainViewModel) :
    ViewModel() {

    private val repository = ChatRepository()
    private val chatService = ChatService(repository)

    private val _chat = MutableStateFlow<Chat?>(null)
    val chat: StateFlow<Chat?> = _chat

    private val _chatHistory = MutableStateFlow<List<Message>>(emptyList())
    val chatHistory: StateFlow<List<Message>> = _chatHistory

    @kotlinx.serialization.InternalSerializationApi
    fun sendMessage(message: String, model: Model?) {
        viewModelScope.launch {
            val profile = mainViewModel.selectedConnectionProfile.first()
                ?: // Handle error: no profile selected
                return@launch
            val modelId = model?.id ?: return@launch // or a default model

            try {
                val (updatedChat, taskId) = chatService.sendMessage(
                    profile,
                    _chat.value,
                    message,
                    modelId
                )
                _chat.value = updatedChat
                _chatHistory.value = updatedChat.messages
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
                Log.e("ChatViewModel", e.stackTraceToString())
                // Handle error
            }
        }
    }

    fun clearChat() {
        _chat.value = null
        _chatHistory.value = emptyList()
    }
}
