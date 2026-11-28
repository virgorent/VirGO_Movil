package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

/** Día no libre; `id` solo llega en el `201` del bloqueo (y en la lista cuando el backend lo agregue). */
@Serializable
data class VenueDayDto(
    val date: String,
    val kind: String? = null,
    val reason: String? = null,
    val note: String? = null,
    val id: String? = null,
)

@Serializable
data class AvailabilityResponseDto(
    val data: List<VenueDayDto> = emptyList(),
)

@Serializable
data class BlockRequestDto(
    val date: String,
    val reason: String,
    val note: String? = null,
)
