package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class IdeasRequestDto(
    val eventType: String? = null,
    val guests: Int,
    val budget: Double? = null,
)

@Serializable
data class DecorIdeaDto(
    val title: String,
    val description: String? = null,
    val cost: Double = 0.0,
)

@Serializable
data class IdeasResponseDto(
    val data: List<DecorIdeaDto> = emptyList(),
)

@Serializable
data class ChatTurnDto(
    val role: String,
    val text: String,
)

@Serializable
data class AssistantRequestDto(
    val message: String,
    val eventId: String? = null,
    val history: List<ChatTurnDto> = emptyList(),
)

@Serializable
data class AssistantReplyDto(
    val reply: String,
    val uncertain: Boolean = false,
)
