package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

/** Sin `deposit`: el servidor aplica el anticipo del 30 % (009 ADR-03). `quoteId` se omite si es nulo. */
@Serializable
data class CreateBookingRequestDto(
    val venueId: String,
    val date: String,
    val hours: Int,
    val quoteId: String? = null,
)

@Serializable
data class BookingsResponseDto(
    val data: List<BookingDto> = emptyList(),
)

/** Solo los campos que muestra la app; el resto (cliente, pagos, clave) se ignora. */
@Serializable
data class BookingDto(
    val id: String,
    val folio: String? = null,
    val venueId: String? = null,
    val venueName: String? = null,
    val date: String? = null,
    val hours: Int? = null,
    val total: Double? = null,
    val deposit: Double? = null,
    val status: String? = null,
    val createdAt: String? = null,
)
