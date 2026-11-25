package mx.virgo.core.network.api

import mx.virgo.core.network.dto.CreateQuoteRequestDto
import mx.virgo.core.network.dto.QuoteAnswerRequestDto
import mx.virgo.core.network.dto.QuoteDto
import mx.virgo.core.network.dto.QuotesResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Cotizaciones a salones (backend `008`, `060`–`062`). Crear exige rol `client`; responder, `owner`. */
interface QuotesApi {
    @POST("api/v1/quotes")
    suspend fun create(
        @Body request: CreateQuoteRequestDto,
    ): QuoteDto

    @GET("api/v1/quotes")
    suspend fun list(): QuotesResponseDto

    @GET("api/v1/quotes/{id}")
    suspend fun get(
        @Path("id") id: String,
    ): QuoteDto

    @POST("api/v1/quotes/{id}/response")
    suspend fun respond(
        @Path("id") id: String,
        @Body request: QuoteAnswerRequestDto,
    ): QuoteDto
}
