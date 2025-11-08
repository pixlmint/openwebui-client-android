package com.example.openwebuieink.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connection_profiles")
data class ConnectionProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val baseUrl: String,
    val apiKey: String?,
    val defaultModel: String? = null
)
