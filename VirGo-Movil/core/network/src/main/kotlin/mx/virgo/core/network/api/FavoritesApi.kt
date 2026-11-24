package mx.virgo.core.network.api

import mx.virgo.core.network.dto.FavoriteRequestDto
import mx.virgo.core.network.dto.FavoritesResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritesApi {
    @GET("api/v1/favorites")
    suspend fun favorites(): FavoritesResponseDto

    @POST("api/v1/favorites")
    suspend fun addFavorite(
        @Body body: FavoriteRequestDto,
    )

    @DELETE("api/v1/favorites/{targetType}/{targetId}")
    suspend fun removeFavorite(
        @Path("targetType") targetType: String,
        @Path("targetId") targetId: String,
    )
}
