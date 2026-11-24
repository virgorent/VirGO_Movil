package mx.virgo.core.domain

data class Favorite(
    val targetType: String,
    val targetId: String,
    val name: String?,
    val photoUrl: String?,
    val rating: Double?,
)
