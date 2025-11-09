package com.example.openwebuieink.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.data.ConnectionProfile
import com.example.openwebuieink.network.Chat
import com.example.openwebuieink.network.ChatRepository
import com.example.openwebuieink.network.Model
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val settingsViewModel: SettingsViewModel) : AndroidViewModel(application) {

    private val chatRepository = ChatRepository()

    val selectedConnectionProfile = settingsViewModel.selectedConnectionProfile

    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()

    private val _selectedModel = MutableStateFlow<Model?>(null)
    val selectedModel: StateFlow<Model?> = _selectedModel.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _clearChatEvent = MutableSharedFlow<Unit>()
    val clearChatEvent = _clearChatEvent.asSharedFlow()

    init {
        settingsViewModel.selectedConnectionProfile.onEach { profile ->
            if (profile != null) {
                getModels(profile)
                getChats(profile)
            }
        }.launchIn(viewModelScope)
    }

    fun getModels(profile: ConnectionProfile) {
        viewModelScope.launch {
            try {
                val response = chatRepository.getModels(profile.baseUrl, profile.apiKey)
                _models.value = response.data
                if (_selectedModel.value == null && response.data.isNotEmpty()) {
                    _selectedModel.value = response.data.first()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to get models", e)
            }
        }
    }

    fun getChats(profile: ConnectionProfile) {
        viewModelScope.launch {
            try {
                _chats.value = chatRepository.getChats(profile.baseUrl, profile.apiKey)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to get chats", e)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            _clearChatEvent.emit(Unit)
        }
    }

    fun selectModel(model: Model) {
        _selectedModel.value = model
    }
}