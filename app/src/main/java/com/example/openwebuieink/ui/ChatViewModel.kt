package com.example.openwebuieink.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.network.ChatMessage
import com.example.openwebuieink.network.ChatRequest
import com.example.openwebuieink.network.ChatRepository
import com.example.openwebuieink.network.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application, private val mainViewModel: MainViewModel) : ViewModel() {

    private val repository = ChatRepository()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory

    fun sendMessage(message: String, model: Model?) {
        val userMessage = ChatMessage(role = "user", content = message)
        _chatHistory.value = _chatHistory.value + userMessage

        viewModelScope.launch {
            val profile = mainViewModel.selectedConnectionProfile.first()
            if (profile == null) {
                val errorMessage = ChatMessage(role = "assistant", content = "Error: Please select a connection profile.")
                _chatHistory.value = _chatHistory.value + errorMessage
                return@launch
            }

            val request = ChatRequest(
                model = model?.id ?: "",
                messages = _chatHistory.value
            )

            try {
                val response = repository.getChatCompletion(profile.baseUrl, profile.apiKey, request)
                val assistantMessage = response.choices.first().message
                _chatHistory.value = _chatHistory.value + assistantMessage
            } catch (e: Exception) {
                // Handle error
                val errorMessage = ChatMessage(role = "assistant", content = "Error: ${e.message}")
                _chatHistory.value = _chatHistory.value + errorMessage
            }
        }
    }

    fun clearChat() {
        _chatHistory.value = emptyList()
    }
}