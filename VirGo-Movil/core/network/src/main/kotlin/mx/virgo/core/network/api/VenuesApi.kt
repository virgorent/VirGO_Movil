package mx.virgo.core.network.api

import mx.virgo.core.network.dto.VenueDetailDto
import mx.virgo.core.network.dto.VenuesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VenuesApi {
    @GET("api/v1/venues")
    suspend fun venues(
        @Query("zone") zone: String? = null,
        @Query("category") category: String? = null,
    ): VenuesResponseDto

    /** Backend `024`: público; solo salones publicados (borrador → 404). `price` solo con sesión. */
    @GET("api/v1/venues/{id}")
    suspend fun venue(
        @Path("id") id: String,
    ): VenueDetailDto
}
