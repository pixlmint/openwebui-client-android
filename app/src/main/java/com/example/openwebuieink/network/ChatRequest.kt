package com.example.openwebuieink.network

import kotlinx.serialization.Serializable

@Serializable
@kotlinx.serialization.InternalSerializationApi
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>
)

@Serializable
@kotlinx.serialization.InternalSerializationApi
data class ChatMessage(
    val role: String,
    val content: String
)