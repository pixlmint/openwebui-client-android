package com.example.openwebuieink.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Settings(
    @PrimaryKey val id: Int = 1,
    val apiEndpoint: String,
    val apiKey: String,
    val defaultModel: String
)