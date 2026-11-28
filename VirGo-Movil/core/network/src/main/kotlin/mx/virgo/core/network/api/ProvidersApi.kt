package mx.virgo.core.network.api

import mx.virgo.core.network.dto.GigDto
import mx.virgo.core.network.dto.GigsResponseDto
import mx.virgo.core.network.dto.ProviderDto
import mx.virgo.core.network.dto.ProviderRequestDto
import mx.virgo.core.network.dto.ProvidersResponseDto
import mx.virgo.core.network.dto.VenueReviewsDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Proveedores según los specs del backend `034`–`041` y `073` (especificados, aún sin implementar). */
interface ProvidersApi {
    @GET("api/v1/providers")
    suspend fun catalog(
        @Query("category") category: String? = null,
    ): ProvidersResponseDto

    @GET("api/v1/providers/{id}")
    suspend fun provider(
        @Path("id") id: String,
    ): ProviderDto

    @GET("api/v1/providers/{id}/reviews")
    suspend fun reviews(
        @Path("id") id: String,
    ): VenueReviewsDto

    @GET("api/v1/providers/me")
    suspend fun mine(): ProviderDto

    @POST("api/v1/providers")
    suspend fun create(
        @Body body: ProviderRequestDto,
    ): ProviderDto

    @PATCH("api/v1/providers/me")
    suspend fun update(
        @Body body: ProviderRequestDto,
    ): ProviderDto

    @GET("api/v1/providers/me/gigs")
    suspend fun gigs(): GigsResponseDto

    @POST("api/v1/providers/me/gigs")
    suspend fun createGig(
        @Body body: GigDto,
    ): GigDto

    @PATCH("api/v1/providers/me/gigs/{id}")
    suspend fun updateGig(
        @Path("id") id: String,
        @Body body: GigDto,
    ): GigDto
}
