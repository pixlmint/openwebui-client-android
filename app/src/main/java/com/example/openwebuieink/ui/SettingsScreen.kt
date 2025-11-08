package com.example.openwebuieink.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = viewModel()) {
    val selectedProfile by viewModel.selectedConnectionProfile.collectAsState()
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    LaunchedEffect(selectedProfile) {
        selectedProfile?.let {
            name = it.name
            baseUrl = it.baseUrl
            apiKey = it.apiKey ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedProfile != null
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedProfile != null
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedProfile != null
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                selectedProfile?.let {
                    viewModel.updateConnectionProfile(it.copy(name = name, baseUrl = baseUrl, apiKey = apiKey))
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedProfile != null
        ) {
            Text("Save")
        }
    }
}