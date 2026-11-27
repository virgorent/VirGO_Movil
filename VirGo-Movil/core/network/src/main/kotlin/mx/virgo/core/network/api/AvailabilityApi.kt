package mx.virgo.core.network.api

import mx.virgo.core.network.dto.AvailabilityResponseDto
import mx.virgo.core.network.dto.BlockRequestDto
import mx.virgo.core.network.dto.VenueDayDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Disponibilidad y bloqueos de un salón (backend `031`–`033`, `023`). Bloquear exige ser el dueño. */
interface AvailabilityApi {
    @GET("api/v1/venues/{id}/availability")
    suspend fun availability(
        @Path("id") venueId: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): AvailabilityResponseDto

    @POST("api/v1/venues/{id}/blocks")
    suspend fun block(
        @Path("id") venueId: String,
        @Body body: BlockRequestDto,
    ): VenueDayDto

    @DELETE("api/v1/venues/{id}/blocks/{blockId}")
    suspend fun unblock(
        @Path("id") venueId: String,
        @Path("blockId") blockId: String,
    ): Response<Unit>
}
