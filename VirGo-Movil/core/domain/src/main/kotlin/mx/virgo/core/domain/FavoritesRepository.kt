package mx.virgo.core.domain

import mx.virgo.core.common.AppResult

interface FavoritesRepository {
    suspend fun getFavorites(): AppResult<List<Favorite>>

    suspend fun addFavorite(
        targetType: String,
        targetId: String,
        name: String?,
        photoUrl: String?,
        rating: Double?,
    ): AppResult<Unit>

    suspend fun removeFavorite(
        targetType: String,
        targetId: String,
    ): AppResult<Unit>
}
