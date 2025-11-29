package com.example.openwebuieink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.openwebuieink.ui.ChatScreen
import com.example.openwebuieink.ui.ConnectionProfileSwitcher
import com.example.openwebuieink.ui.MainViewModel
import com.example.openwebuieink.ui.SettingsScreen
import com.example.openwebuieink.ui.SettingsViewModel
import com.example.openwebuieink.ui.theme.OpenwebuiEinkTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(application, settingsViewModel) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenwebuiEinkTheme {
                AppNavigation(mainViewModel, settingsViewModel)
            }
        }
    }
}

@kotlinx.serialization.InternalSerializationApi
@Composable
fun AppNavigation(mainViewModel: MainViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val chats by mainViewModel.chats.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                ConnectionProfileSwitcher(settingsViewModel = settingsViewModel)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Chat") },
                    label = { Text(text = "New Chat") },
                    selected = false,
                    onClick = { 
                        scope.launch {
                            mainViewModel.clearChat()
                            drawerState.close()
                        }
                     }
                )
//                NavigationDrawerItem(
//                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
//                    label = { Text(text = "Settings") },
//                    selected = false,
//                    onClick = {
//                        scope.launch { drawerState.close() }
//                        navController.navigate("settings")
//                    }
//                )
                LazyColumn {
                    items(chats) { chat ->
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Face, contentDescription = "Chat") },
                            label = { Text(chat.title) },
                            selected = false,
                            onClick = {
                                mainViewModel.setChat(chat)
                                scope.launch { drawerState.close() }
                                // navController.navigate("chat/${chat.id}")
                            }
                        )
                    }
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "chat") {
            composable("chat") { ChatScreen(mainViewModel) { scope.launch { drawerState.open() } } }
            composable("settings") { SettingsScreen(navController, settingsViewModel) }
        }
    }
}

@Composable
fun ModelSelectionButton(viewModel: MainViewModel) {
    val selectedModel by viewModel.selectedModel.collectAsState()
    val showDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .clickable { showDialog.value = true },
        contentAlignment = Alignment.Center
    ) {
        Text(text = selectedModel?.name ?: "Select Model")
    }

    if (showDialog.value) {
        ModelSelectionDialog(viewModel = viewModel) {
            showDialog.value = false
        }
    }
}

@Composable
fun ModelSelectionDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val models by viewModel.models.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LazyColumn {
                items(models) { model ->
                    Text(
                        text = model.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                viewModel.selectModel(model)
                                onDismiss()
                             }
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
