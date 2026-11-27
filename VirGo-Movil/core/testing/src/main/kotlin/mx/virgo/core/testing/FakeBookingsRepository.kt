package mx.virgo.core.testing

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.BookingRequest
import mx.virgo.core.domain.BookingStatus
import mx.virgo.core.domain.BookingsRepository
import java.math.BigDecimal
import java.time.LocalDate

/** Igual que el servidor: la misma clave devuelve la misma reservación. */
class FakeBookingsRepository : BookingsRepository {
    val bookings = mutableListOf<Booking>()
    val keys = mutableListOf<String>()
    val requests = mutableListOf<BookingRequest>()
    private val byKey = mutableMapOf<String, Booking>()
    var createFailure: AppResult.Failure? = null
    var listFailure: AppResult.Failure? = null
    var folioFailure: AppResult.Failure? = null
    val folioQueries = mutableListOf<String>()

    override suspend fun create(
        request: BookingRequest,
        idempotencyKey: String,
    ): AppResult<Booking> {
        keys += idempotencyKey
        requests += request
        createFailure?.let { return it }
        val booking =
            byKey.getOrPut(idempotencyKey) {
                sampleBooking()
                    .copy(
                        id = "booking-${byKey.size + 1}",
                        folio = "VG-2027-%04d".format(byKey.size + 1),
                        venueId = request.venueId,
                        date = request.date,
                        hours = request.hours ?: 0,
                    ).also { bookings += it }
            }
        return AppResult.Success(booking)
    }

    override suspend fun list(): AppResult<List<Booking>> = listFailure ?: AppResult.Success(bookings.toList())

    override suspend fun get(id: String): AppResult<Booking> =
        bookings.firstOrNull { it.id == id }?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)

    override suspend fun byFolio(folio: String): AppResult<Booking> {
        folioQueries += folio
        folioFailure?.let { return it }
        return bookings.firstOrNull { it.folio == folio }?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.NotFound)
    }
}

fun sampleBooking(): Booking =
    Booking(
        id = "8a1d2c3b-0000-4000-8000-000000000001",
        folio = "VG-2027-0001",
        venueId = "132a1f58-ebf9-4082-acdf-7241975f1811",
        venueName = "Salón Las Palmas",
        date = LocalDate.parse("2027-05-15"),
        hours = 5,
        total = BigDecimal("7500"),
        deposit = BigDecimal("2250"),
        status = BookingStatus.PENDING_PAYMENT,
        createdAt = "2026-11-25T18:00:00Z",
    )
