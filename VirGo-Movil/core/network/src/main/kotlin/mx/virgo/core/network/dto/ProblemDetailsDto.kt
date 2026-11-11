package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProblemDetailsDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val traceId: String? = null,
    val errors: Map<String, List<String>>? = null,
)
