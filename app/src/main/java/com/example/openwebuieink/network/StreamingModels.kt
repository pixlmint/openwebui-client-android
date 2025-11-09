package com.example.openwebuieink.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class StreamingChatCompletionChunk(
    val id: String,
    @SerialName("object") val obj: String,
    val created: Long,
    val model: String,
    @SerialName("system_fingerprint") val systemFingerprint: String,
    val choices: List<Choice>
) {
    @Serializable
    data class Choice(
        val index: Int,
        val delta: Delta,
        @SerialName("finish_reason") val finishReason: String?,
        val logprobs: String? = null
    )

    @Serializable
    data class Delta(
        val role: String? = null,
        val content: String? = null,
        val reasoning: String? = null
    )
}
