package com.example.openwebuieink.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.data.AppDatabase
import com.example.openwebuieink.data.ConnectionProfile
import com.example.openwebuieink.data.ConnectionProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val connectionProfileDao = AppDatabase.getDatabase(application).connectionProfileDao()
    private val repository = ConnectionProfileRepository(connectionProfileDao)
    private val sharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val profiles: StateFlow<List<ConnectionProfile>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedConnectionProfile = MutableStateFlow<ConnectionProfile?>(null)
    val selectedConnectionProfile: StateFlow<ConnectionProfile?> = _selectedConnectionProfile.asStateFlow()

    init {
        // Auto-select last used connection profile when profiles are loaded
        profiles.onEach { profileList ->
            // Only auto-select if no profile is currently selected and we have profiles
            if (_selectedConnectionProfile.value == null && profileList.isNotEmpty()) {
                val lastUsedId = sharedPreferences.getInt(PREF_LAST_CONNECTION_ID, -1)
                val profileToSelect = if (lastUsedId != -1) {
                    // Try to find the last used profile
                    profileList.find { it.id == lastUsedId }
                } else {
                    null
                }
                // If last used profile exists, use it; otherwise use first profile
                _selectedConnectionProfile.value = profileToSelect ?: profileList.firstOrNull()
            }
        }.launchIn(viewModelScope)
    }

    fun selectConnectionProfile(profile: ConnectionProfile) {
        _selectedConnectionProfile.value = profile
        // Save the selected profile ID to SharedPreferences
        sharedPreferences.edit().putInt(PREF_LAST_CONNECTION_ID, profile.id).apply()
    }

    companion object {
        private const val PREFS_NAME = "connection_prefs"
        private const val PREF_LAST_CONNECTION_ID = "last_connection_id"
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
