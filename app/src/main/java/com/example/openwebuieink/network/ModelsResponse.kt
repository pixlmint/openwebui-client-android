package com.example.openwebuieink.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class ModelsResponse(
    val data: List<Model>
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class Model(
    val id: String,
    val name: String,
    val `object`: String, // Escaped because 'object' is a keyword
    val created: Long? = null,
    @SerialName("owned_by")
    val ownedBy: String,
    // The following fields are based on your sample response and might need adjustment
    // if their structure is more complex than shown.
    val pipe: Pipe? = null, 
    @SerialName("has_user_valves")
    val hasUserValves: Boolean? = null,
    val actions: List<String>? = null, // Assuming actions is a list of strings
    val filters: List<String>? = null, // Assuming filters is a list of strings
    val tags: List<Map<String, String>>? = null // Assuming tags is a list of strings
)

@kotlinx.serialization.InternalSerializationApi
@Serializable
data class Pipe(
    val type: String
)
