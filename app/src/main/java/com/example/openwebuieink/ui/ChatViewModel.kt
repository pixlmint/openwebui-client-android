package com.example.openwebuieink.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.network.ChatMessage
import com.example.openwebuieink.network.ChatRequest
import com.example.openwebuieink.network.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory

    fun sendMessage(message: String) {
        val userMessage = ChatMessage(role = "user", content = message)
        _chatHistory.value = _chatHistory.value + userMessage

        viewModelScope.launch {
            val request = ChatRequest(
                model = "gpt-3.5-turbo", // Or your desired model
                messages = _chatHistory.value
            )

            try {
                val response = repository.getChatCompletion(request)
                val assistantMessage = response.choices.first().message
                _chatHistory.value = _chatHistory.value + assistantMessage
            } catch (e: Exception) {
                // Handle error
                val errorMessage = ChatMessage(role = "assistant", content = "Error: ${e.message}")
                _chatHistory.value = _chatHistory.value + errorMessage
            }
        }
    }
}