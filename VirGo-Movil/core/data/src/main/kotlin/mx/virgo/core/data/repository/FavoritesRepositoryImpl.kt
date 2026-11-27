package mx.virgo.core.data.repository

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Favorite
import mx.virgo.core.domain.FavoritesRepository
import mx.virgo.core.network.api.FavoritesApi
import mx.virgo.core.network.dto.FavoriteDto
import mx.virgo.core.network.dto.FavoriteRequestDto
import javax.inject.Inject

/** Favoritos (contrato §7): `404`/`501` llegan como [mx.virgo.core.common.AppError.NotFound]. */
class FavoritesRepositoryImpl
    @Inject
    constructor(
        private val favoritesApi: FavoritesApi,
    ) : FavoritesRepository {
        override suspend fun getFavorites(): AppResult<List<Favorite>> =
            contractCall {
                AppResult.Success(favoritesApi.favorites().data.map { it.toFavorite() })
            }

        override suspend fun addFavorite(
            targetType: String,
            targetId: String,
            name: String?,
            photoUrl: String?,
            rating: Double?,
        ): AppResult<Unit> =
            contractCall {
                favoritesApi.addFavorite(
                    FavoriteRequestDto(
                        targetType = targetType,
                        targetId = targetId,
                        name = name,
                        photoUrl = photoUrl,
                        rating = rating,
                    ),
                )
                AppResult.Success(Unit)
            }

        override suspend fun removeFavorite(
            targetType: String,
            targetId: String,
        ): AppResult<Unit> =
            contractCall {
                favoritesApi.removeFavorite(targetType, targetId)
                AppResult.Success(Unit)
            }
    }

internal fun FavoriteDto.toFavorite(): Favorite =
    Favorite(
        targetType = targetType,
        targetId = targetId,
        name = name,
        photoUrl = photoUrl,
        rating = rating,
    )
