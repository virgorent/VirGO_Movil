package mx.virgo.core.network.api

import mx.virgo.core.network.dto.CreateVenueRequestDto
import mx.virgo.core.network.dto.OwnerVenueDto
import mx.virgo.core.network.dto.OwnerVenuesResponseDto
import mx.virgo.core.network.dto.PatchVenueRequestDto
import mx.virgo.core.network.dto.PublishVenueResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Salones del propietario (backend `025`, `026`, `027`, `029`). Todos exigen rol `owner`. */
interface OwnerVenuesApi {
    @GET("api/v1/venues/mine")
    suspend fun mine(): OwnerVenuesResponseDto

    @POST("api/v1/venues")
    suspend fun create(
        @Body request: CreateVenueRequestDto,
    ): OwnerVenueDto

    @PATCH("api/v1/venues/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body request: PatchVenueRequestDto,
    ): OwnerVenueDto

    @POST("api/v1/venues/{id}/publish")
    suspend fun publish(
        @Path("id") id: String,
    ): PublishVenueResponseDto
}
