package com.example.openwebuieink.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.openwebuieink.db.AppDatabase
import com.example.openwebuieink.db.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDao = AppDatabase.getDatabase(application).settingsDao()

    val settings = settingsDao.getSettings().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    fun saveSettings(apiEndpoint: String, apiKey: String, defaultModel: String) {
        viewModelScope.launch {
            val newSettings = Settings(
                apiEndpoint = apiEndpoint,
                apiKey = apiKey,
                defaultModel = defaultModel
            )
            settingsDao.saveSettings(newSettings)
        }
    }
}