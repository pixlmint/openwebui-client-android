package com.example.openwebuieink.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.db.AppDatabase
import com.example.openwebuieink.db.Settings
import com.example.openwebuieink.network.Chat
import com.example.openwebuieink.network.ChatRepository
import com.example.openwebuieink.network.Model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDao = AppDatabase.getDatabase(application).settingsDao()
    private val chatRepository = ChatRepository()

    val settings = settingsDao.getSettings()

    private val _models = MutableStateFlow<List<Model>>(emptyList())
    val models: StateFlow<List<Model>> = _models.asStateFlow()

    private val _selectedModel = MutableStateFlow<Model?>(null)
    val selectedModel: StateFlow<Model?> = _selectedModel.asStateFlow()

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    init {
        viewModelScope.launch {
            settings.collect { settings ->
                if (settings != null) {
                    getModels(settings)
                    getChats(settings)
                }
            }
        }
    }

    fun getModels(settings: Settings) {
        viewModelScope.launch {
            try {
                val response = chatRepository.getModels(settings)
                _models.value = response.data
                if (_selectedModel.value == null && settings.defaultModel.isNotEmpty()) {
                    _selectedModel.value = response.data.find { it.id == settings.defaultModel }
                }
                if (_selectedModel.value == null && response.data.isNotEmpty()) {
                    _selectedModel.value = response.data.first()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to get models", e)
            }
        }
    }

    fun getChats(settings: Settings) {
        viewModelScope.launch {
            try {
                _chats.value = chatRepository.getChats(settings)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to get chats", e)
            }
        }
    }

    fun selectModel(model: Model) {
        _selectedModel.value = model
    }
}