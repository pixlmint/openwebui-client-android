package com.example.openwebuieink.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.openwebuieink.ui.MainViewModel

class ChatViewModelFactory(private val application: Application, private val mainViewModel: MainViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(application, mainViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}