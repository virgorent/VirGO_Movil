package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Favorite
import mx.virgo.core.domain.FavoritesRepository
import mx.virgo.core.network.api.FavoritesApi
import mx.virgo.core.network.dto.FavoriteDto
import mx.virgo.core.network.dto.FavoriteRequestDto
import javax.inject.Inject

class FavoritesRepositoryImpl
    @Inject
    constructor(
        private val favoritesApi: FavoritesApi,
    ) : FavoritesRepository {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun getFavorites(): AppResult<List<Favorite>> =
            try {
                val response = favoritesApi.favorites()
                AppResult.Success(response.data.map { it.toFavorite() })
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }

        @Suppress("TooGenericExceptionCaught")
        override suspend fun addFavorite(
            targetType: String,
            targetId: String,
            name: String?,
            photoUrl: String?,
            rating: Double?,
        ): AppResult<Unit> =
            try {
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
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }

        @Suppress("TooGenericExceptionCaught")
        override suspend fun removeFavorite(
            targetType: String,
            targetId: String,
        ): AppResult<Unit> =
            try {
                favoritesApi.removeFavorite(targetType, targetId)
                AppResult.Success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
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
