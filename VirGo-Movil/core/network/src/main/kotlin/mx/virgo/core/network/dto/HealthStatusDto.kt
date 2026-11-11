package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthStatusDto(
    val status: String,
)
