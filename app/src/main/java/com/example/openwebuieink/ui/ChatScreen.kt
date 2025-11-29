package com.example.openwebuieink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openwebuieink.ModelSelectionButton
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun ChatScreen(mainViewModel: MainViewModel, onMenuClick: () -> Unit) {
    val factory =
        ChatViewModelFactory(mainViewModel)
    val viewModel: ChatViewModel = viewModel(factory = factory)
    val chatHistory by viewModel.chatHistory.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val restoreMessage by viewModel.restoreMessage.collectAsState()
    var message by remember { mutableStateOf("") }
    val selectedModel by mainViewModel.selectedModel.collectAsState()
    val selectedChat by mainViewModel.selectedChat.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedChat) {
        selectedChat?.let {
            viewModel.loadChat(it)
        }
    }

    LaunchedEffect(mainViewModel) {
        mainViewModel.clearChatEvent.onEach {
            viewModel.clearChat()
        }.launchIn(this)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(restoreMessage) {
        restoreMessage?.let {
            message = it
            viewModel.clearRestoreMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                ModelSelectionButton(viewModel = mainViewModel)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(chatHistory) { chatMessage ->
                    val backgroundColor = if (chatMessage.role == "user") {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(backgroundColor)
                    ) {
                        Text(
                            text = "${chatMessage.role}: ${chatMessage.content}",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (message.isNotBlank()) {
                            viewModel.sendMessage(message, selectedModel)
                            message = ""
                        }
                    }
                ) {
                    Text("Send")
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )
    }
}
