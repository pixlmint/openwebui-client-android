package com.example.openwebuieink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.openwebuieink.ui.ChatScreen
import com.example.openwebuieink.ui.theme.OpenwebuiEinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenwebuiEinkTheme {
                ChatScreen()
            }
        }
    }
}