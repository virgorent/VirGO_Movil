package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.BookingRequest
import mx.virgo.core.domain.BookingStatus
import mx.virgo.core.domain.BookingsRepository
import mx.virgo.core.network.api.BookingsApi
import mx.virgo.core.network.dto.BookingDto
import mx.virgo.core.network.dto.CreateBookingRequestDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

class BookingsRepositoryImpl
    @Inject
    constructor(
        private val api: BookingsApi,
    ) : BookingsRepository {
        override suspend fun create(
            request: BookingRequest,
            idempotencyKey: String,
        ): AppResult<Booking> =
            safeCall {
                val body =
                    CreateBookingRequestDto(
                        venueId = request.venueId,
                        date = requireNotNull(request.date).toString(),
                        hours = requireNotNull(request.hours),
                        quoteId = request.quoteId,
                    )
                AppResult.Success(api.create(idempotencyKey, body).toBooking())
            }

        override suspend fun list(): AppResult<List<Booking>> =
            safeCall { AppResult.Success(api.list().data.map { it.toBooking() }) }

        override suspend fun get(id: String): AppResult<Booking> =
            safeCall { AppResult.Success(api.get(id).toBooking()) }

        override suspend fun byFolio(folio: String): AppResult<Booking> =
            safeCall { AppResult.Success(api.byFolio(folio).toBooking()) }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun <T> safeCall(block: suspend () -> AppResult<T>): AppResult<T> =
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun BookingDto.toBooking(): Booking =
    Booking(
        id = id,
        folio = folio.orEmpty(),
        venueId = venueId,
        venueName = venueName.orEmpty(),
        date = date?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        hours = hours ?: 0,
        total = total?.let(BigDecimal::valueOf),
        deposit = deposit?.let(BigDecimal::valueOf),
        status = BookingStatus.fromWire(status),
        createdAt = createdAt,
    )
