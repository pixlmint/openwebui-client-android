package com.example.openwebuieink.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.openwebuieink.data.ConnectionProfile

@Composable
fun ConnectionProfileSwitcher(
    settingsViewModel: SettingsViewModel
) {
    val profiles by settingsViewModel.profiles.collectAsState()
    val selectedProfile by settingsViewModel.selectedConnectionProfile.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var profileToEdit by remember { mutableStateOf<ConnectionProfile?>(null) }

    if (showEditDialog) {
        EditConnectionProfileDialog(
            profile = profileToEdit,
            onDismiss = { showEditDialog = false },
            onSave = { name, baseUrl, apiKey, defaultModel ->
                if (profileToEdit == null) {
                    settingsViewModel.addConnectionProfile(name, baseUrl, apiKey, defaultModel)
                } else {
                    settingsViewModel.updateConnectionProfile(profileToEdit!!.copy(name = name, baseUrl = baseUrl, apiKey = apiKey, defaultModel = defaultModel))
                }
                showEditDialog = false
            }
        )
    }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Open user menu",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            profiles.forEach { profile ->
                DropdownMenuItem(onClick = {
                    settingsViewModel.selectConnectionProfile(profile)
                    showMenu = false
                }, text = { Text(profile.name) })
            }
            DropdownMenuItem(onClick = {
                profileToEdit = null
                showEditDialog = true
                showMenu = false
            }, text = { Text("Add Profile") }, leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) })
            selectedProfile?.let {
                DropdownMenuItem(onClick = {
                    profileToEdit = it
                    showEditDialog = true
                    showMenu = false
                }, text = { Text("Edit Profile") }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) })
            }
        }
    }
}

@Composable
fun EditConnectionProfileDialog(
    profile: ConnectionProfile?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var baseUrl by remember { mutableStateOf(profile?.baseUrl ?: "") }
    var apiKey by remember { mutableStateOf(profile?.apiKey ?: "") }
    var defaultModel by remember { mutableStateOf(profile?.defaultModel ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (profile == null) "Add Profile" else "Edit Profile", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(value = name, onValueChange = { name = it }, label = { Text("Profile Name") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = baseUrl, onValueChange = { baseUrl = it }, label = { Text("Base URL") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key (Optional)") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = defaultModel, onValueChange = { defaultModel = it }, label = { Text("Default Model (Optional)") })
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }
//                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(name, baseUrl, apiKey, defaultModel) }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
