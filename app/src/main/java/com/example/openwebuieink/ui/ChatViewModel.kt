package com.example.openwebuieink.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.db.AppDatabase
import com.example.openwebuieink.network.ChatMessage
import com.example.openwebuieink.network.ChatRequest
import com.example.openwebuieink.network.ChatRepository
import com.example.openwebuieink.network.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository()
    private val settingsDao = AppDatabase.getDatabase(application).settingsDao()

    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory

    fun sendMessage(message: String, model: Model?) {
        val userMessage = ChatMessage(role = "user", content = message)
        _chatHistory.value = _chatHistory.value + userMessage

        viewModelScope.launch {
            val settings = settingsDao.getSettings().first()
            if (settings == null) {
                val errorMessage = ChatMessage(role = "assistant", content = "Error: Please configure the API settings first.")
                _chatHistory.value = _chatHistory.value + errorMessage
                return@launch
            }

            val request = ChatRequest(
                model = model?.id ?: settings.defaultModel,
                messages = _chatHistory.value
            )

            try {
                val response = repository.getChatCompletion(settings, request)
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