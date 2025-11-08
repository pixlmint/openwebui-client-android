package com.example.openwebuieink.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.data.AppDatabase
import com.example.openwebuieink.data.ConnectionProfile
import com.example.openwebuieink.data.ConnectionProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val connectionProfileDao = AppDatabase.getDatabase(application).connectionProfileDao()
    private val repository = ConnectionProfileRepository(connectionProfileDao)

    val profiles: StateFlow<List<ConnectionProfile>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedConnectionProfile = MutableStateFlow<ConnectionProfile?>(null)
    val selectedConnectionProfile: StateFlow<ConnectionProfile?> = _selectedConnectionProfile.asStateFlow()

    fun selectConnectionProfile(profile: ConnectionProfile) {
        _selectedConnectionProfile.value = profile
    }

    fun addConnectionProfile(name: String, baseUrl: String, apiKey: String?, defaultModel: String?) {
        viewModelScope.launch {
            repository.insert(ConnectionProfile(name = name, baseUrl = baseUrl, apiKey = apiKey, defaultModel = defaultModel))
        }
    }

    fun updateConnectionProfile(profile: ConnectionProfile) {
        viewModelScope.launch {
            repository.update(profile)
        }
    }

    fun deleteConnectionProfile(profile: ConnectionProfile) {
        viewModelScope.launch {
            repository.delete(profile)
        }
    }
}
