package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FavoritesResponseDto(
    val data: List<FavoriteDto> = emptyList(),
)

@Serializable
data class FavoriteDto(
    val targetType: String,
    val targetId: String,
    val name: String? = null,
    val photoUrl: String? = null,
    val rating: Double? = null,
)

@Serializable
data class FavoriteRequestDto(
    val targetType: String,
    val targetId: String,
    val name: String? = null,
    val photoUrl: String? = null,
    val rating: Double? = null,
)
