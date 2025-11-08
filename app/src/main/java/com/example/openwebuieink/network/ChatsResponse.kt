package com.example.openwebuieink.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("updated_at")
    val updatedAt: Long,
    @SerialName("created_at")
    val createdAt: Long
)
