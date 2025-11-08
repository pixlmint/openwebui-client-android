package com.example.openwebuieink.network

import kotlinx.serialization.Serializable

@Serializable
data class ModelsResponse(
    val models: List<Model>
)
