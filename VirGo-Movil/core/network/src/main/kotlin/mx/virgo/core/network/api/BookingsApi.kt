package mx.virgo.core.network.api

import mx.virgo.core.network.dto.BookingDto
import mx.virgo.core.network.dto.BookingsResponseDto
import mx.virgo.core.network.dto.CreateBookingRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/** Reservaciones (backend `009`, `066`, `067`). Crear exige rol `client` e `Idempotency-Key`. */
interface BookingsApi {
    @POST("api/v1/bookings")
    suspend fun create(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateBookingRequestDto,
    ): BookingDto

    @GET("api/v1/bookings")
    suspend fun list(): BookingsResponseDto

    @GET("api/v1/bookings/{id}")
    suspend fun get(
        @Path("id") id: String,
    ): BookingDto

    /** Backend `068`: el folio va en la ruta, codificado por Retrofit (`010` RF-03). */
    @GET("api/v1/bookings/by-folio/{folio}")
    suspend fun byFolio(
        @Path("folio") folio: String,
    ): BookingDto
}
