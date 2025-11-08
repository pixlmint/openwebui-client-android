package com.example.openwebuieink.network

import kotlinx.serialization.Serializable

@Serializable
@kotlinx.serialization.InternalSerializationApi
data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
@kotlinx.serialization.InternalSerializationApi
data class Choice(
    val message: ChatMessage
)