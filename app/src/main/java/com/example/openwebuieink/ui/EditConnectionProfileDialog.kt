package com.example.openwebuieink.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.openwebuieink.data.ConnectionProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditConnectionProfileDialog(
    profile: ConnectionProfile?,
    onDismiss: () -> Unit,
    onSave: (name: String, baseUrl: String, apiKey: String?) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var baseUrl by remember(profile) { mutableStateOf(profile?.baseUrl ?: "") }
    var apiKey by remember(profile) { mutableStateOf(profile?.apiKey ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (profile == null) "Add Connection" else "Edit Connection") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                TextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") }
                )
                TextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (Optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, baseUrl, apiKey) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
