package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/** Motivo de un día bloqueado por el propietario (`023` RF-09); el servidor solo acepta estos tres. */
enum class BlockReason(
    val wire: String,
) {
    MAINTENANCE("maintenance"),
    OWN_EVENT("own_event"),
    OTHER("other"),
    ;

    companion object {
        fun fromWire(value: String?): BlockReason? = entries.firstOrNull { it.wire == value }
    }
}

/**
 * Día no libre según `GET /venues/:id/availability`. Hoy el servidor solo devuelve bloqueos y
 * no manda su `id`; [blockId] llega cuando lo agregue (`023` RF-11).
 */
data class VenueDay(
    val date: LocalDate,
    val blocked: Boolean,
    val reason: BlockReason? = null,
    val note: String? = null,
    val blockId: String? = null,
)

interface AvailabilityRepository {
    suspend fun days(
        venueId: String,
        from: LocalDate,
        to: LocalDate,
    ): AppResult<List<VenueDay>>

    suspend fun block(
        venueId: String,
        date: LocalDate,
        reason: BlockReason,
        note: String?,
    ): AppResult<VenueDay>

    suspend fun unblock(
        venueId: String,
        blockId: String,
    ): AppResult<Unit>
}

class GetVenueDaysUseCase
    @Inject
    constructor(
        private val repository: AvailabilityRepository,
    ) {
        suspend operator fun invoke(
            venueId: String,
            month: YearMonth,
        ): AppResult<List<VenueDay>> = repository.days(venueId, month.atDay(1), month.atEndOfMonth())
    }

class BlockDayUseCase
    @Inject
    constructor(
        private val repository: AvailabilityRepository,
    ) {
        suspend operator fun invoke(
            venueId: String,
            date: LocalDate,
            reason: BlockReason,
            note: String?,
        ): AppResult<VenueDay> = repository.block(venueId, date, reason, note?.trim()?.takeIf { it.isNotEmpty() })
    }

class UnblockDayUseCase
    @Inject
    constructor(
        private val repository: AvailabilityRepository,
    ) {
        suspend operator fun invoke(
            venueId: String,
            blockId: String,
        ): AppResult<Unit> = repository.unblock(venueId, blockId)
    }

/** Un día del calendario del propietario: sus reservaciones y su bloqueo (`023` RF-06–RF-08). */
data class CalendarDay(
    val date: LocalDate,
    val bookings: List<Booking>,
    val block: VenueDay?,
) {
    val booked: Boolean get() = bookings.isNotEmpty()
    val overlapping: Boolean get() = bookings.size > 1
    val free: Boolean get() = !booked && block == null
}

object OwnerCalendar {
    const val NOTE_MAX = 200

    /** Días del mes con las reservaciones no canceladas de [venueId] y sus bloqueos. */
    fun month(
        month: YearMonth,
        venueId: String,
        bookings: List<Booking>,
        days: List<VenueDay>,
    ): List<CalendarDay> {
        val byDate =
            bookings
                .filter { it.venueId == venueId && it.status != BookingStatus.CANCELLED && it.date != null }
                .groupBy { it.date!! }
        val blocks = days.filter { it.blocked }.associateBy { it.date }
        return (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            CalendarDay(date, byDate[date].orEmpty(), blocks[date])
        }
    }
}

/** Números de «Inicio» del propietario (`023` RF-01 y RF-02). */
data class OwnerDashboard(
    val publishedVenues: Int,
    val registeredVenues: Int,
    val pendingRequests: Int,
    val upcomingBookings: Int,
    val nextBookings: List<Booking>,
) {
    companion object {
        const val NEXT_LIMIT = 3

        fun from(
            venues: List<OwnerVenue>,
            quotes: List<Quote>,
            bookings: List<Booking>,
            today: LocalDate,
        ): OwnerDashboard {
            val ownIds = venues.map { it.id }.toSet()
            val upcoming =
                bookings
                    .filter { it.venueId in ownIds && it.status != BookingStatus.CANCELLED }
                    .filter { it.date != null && !it.date.isBefore(today) }
                    .sortedBy { it.date }
            return OwnerDashboard(
                publishedVenues = venues.count { it.status == VenueStatus.PUBLISHED },
                registeredVenues = venues.size,
                pendingRequests = quotes.count { it.venueId in ownIds && it.status == QuoteStatus.SENT },
                upcomingBookings = upcoming.size,
                nextBookings = upcoming.take(NEXT_LIMIT),
            )
        }
    }
}
