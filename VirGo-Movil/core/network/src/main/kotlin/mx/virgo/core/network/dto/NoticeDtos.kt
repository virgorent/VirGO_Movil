package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class NoticesResponseDto(
    val data: List<NoticeDto> = emptyList(),
)

@Serializable
data class NoticeDto(
    val id: String,
    val type: String? = null,
    val title: String,
    val body: String? = null,
    val readAt: String? = null,
    val createdAt: String? = null,
)
