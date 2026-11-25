package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

/** Estado de la reservación; un valor nuevo del servidor no rompe la app (009 RF-10). */
enum class BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    UNKNOWN,
    ;

    companion object {
        fun fromWire(value: String?): BookingStatus =
            when (value) {
                "pending_payment" -> PENDING_PAYMENT
                "confirmed" -> CONFIRMED
                "cancelled" -> CANCELLED
                else -> UNKNOWN
            }
    }
}

/** Reservación tal como la devuelve el servidor; la app nunca calcula total ni anticipo. */
data class Booking(
    val id: String,
    val folio: String,
    val venueId: String?,
    val venueName: String,
    val date: LocalDate?,
    val hours: Int,
    val total: BigDecimal?,
    val deposit: BigDecimal?,
    val status: BookingStatus,
    val createdAt: String?,
)

data class BookingRequest(
    val venueId: String,
    val date: LocalDate?,
    val hours: Int?,
    val quoteId: String? = null,
)

enum class BookingField {
    DATE,
    HOURS,
}

enum class BookingProblem {
    REQUIRED,
    PAST_DATE,
    BELOW_MINIMUM,
}

/** Fecha no pasada y horas ≥ max(1, horas mínimas del salón) (009 RF-02). */
object BookingValidator {
    fun validate(
        request: BookingRequest,
        today: LocalDate,
        minimumHours: Int?,
    ): Map<BookingField, BookingProblem> =
        buildMap {
            when {
                request.date == null -> put(BookingField.DATE, BookingProblem.REQUIRED)
                request.date.isBefore(today) -> put(BookingField.DATE, BookingProblem.PAST_DATE)
            }
            val hours = request.hours
            when {
                hours == null || hours < 1 -> put(BookingField.HOURS, BookingProblem.REQUIRED)
                hours < (minimumHours ?: 1) -> put(BookingField.HOURS, BookingProblem.BELOW_MINIMUM)
            }
        }
}

interface BookingsRepository {
    /** `idempotencyKey` se reutiliza en cada reintento del mismo intento (009 RNF-03). */
    suspend fun create(
        request: BookingRequest,
        idempotencyKey: String,
    ): AppResult<Booking>

    suspend fun list(): AppResult<List<Booking>>

    suspend fun get(id: String): AppResult<Booking>
}

class CreateBookingUseCase
    @Inject
    constructor(
        private val repository: BookingsRepository,
    ) {
        suspend operator fun invoke(
            request: BookingRequest,
            idempotencyKey: String,
        ): AppResult<Booking> = repository.create(request, idempotencyKey)
    }

/** Lista más reciente primero (009 RF-08). */
class GetBookingsUseCase
    @Inject
    constructor(
        private val repository: BookingsRepository,
    ) {
        suspend operator fun invoke(): AppResult<List<Booking>> =
            when (val result = repository.list()) {
                is AppResult.Failure -> result
                is AppResult.Success -> AppResult.Success(result.value.sortedByDescending { it.createdAt.orEmpty() })
            }
    }

class GetBookingUseCase
    @Inject
    constructor(
        private val repository: BookingsRepository,
    ) {
        suspend operator fun invoke(id: String): AppResult<Booking> = repository.get(id)
    }
