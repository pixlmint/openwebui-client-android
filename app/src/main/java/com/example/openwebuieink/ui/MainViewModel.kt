package com.example.openwebuieink.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.db.AppDatabase
import com.example.openwebuieink.db.Settings
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

    fun getModels(settings: Settings) {
        viewModelScope.launch {
            try {
                val response = chatRepository.getModels(settings)
                _models.value = response.models
                if (_selectedModel.value == null && settings.defaultModel.isNotEmpty()) {
                    _selectedModel.value = response.models.find { it.id == settings.defaultModel }
                }
                if (_selectedModel.value == null && response.models.isNotEmpty()) {
                    _selectedModel.value = response.models.first()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectModel(model: Model) {
        _selectedModel.value = model
    }
}