package mx.virgo.core.network.api

import mx.virgo.core.network.dto.VenuesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface VenuesApi {
    @GET("api/v1/venues")
    suspend fun venues(
        @Query("zone") zone: String? = null,
        @Query("category") category: String? = null,
    ): VenuesResponseDto
}
