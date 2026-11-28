package mx.virgo.core.network.api

import mx.virgo.core.network.dto.CreateReviewDto
import mx.virgo.core.network.dto.ReplyReviewDto
import mx.virgo.core.network.dto.ReviewDto
import mx.virgo.core.network.dto.VenueReviewsDto
import mx.virgo.core.network.dto.VenueToolsPatchDto
import mx.virgo.core.network.dto.VenueToolsResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Precios, montajes y reseñas del salón (`019`, `025`; contrato §5 y §6). */
interface OwnerToolsApi {
    @GET("api/v1/venues/mine")
    suspend fun mine(): VenueToolsResponseDto

    @PATCH("api/v1/venues/{id}")
    suspend fun patch(
        @Path("id") id: String,
        @Body body: VenueToolsPatchDto,
    ): Response<Unit>

    @GET("api/v1/venues/{id}/reviews")
    suspend fun reviews(
        @Path("id") venueId: String,
    ): VenueReviewsDto

    @POST("api/v1/reviews")
    suspend fun createReview(
        @Body body: CreateReviewDto,
    ): ReviewDto

    @POST("api/v1/reviews/{id}/reply")
    suspend fun reply(
        @Path("id") reviewId: String,
        @Body body: ReplyReviewDto,
    ): ReviewDto
}
