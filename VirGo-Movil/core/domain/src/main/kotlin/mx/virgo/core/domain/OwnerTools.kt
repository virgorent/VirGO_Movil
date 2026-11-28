package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Precio del salón con aumentos (`025` RF-01, esquema `venues.price`). Los aumentos se suman: un sábado de
 * temporada alta paga base × (1 + fin de semana % + temporada %).
 */
data class VenuePricing(
    val base: BigDecimal,
    val unit: String,
    val minHours: Int,
    val weekendPct: Int = 0,
    val highSeasonPct: Int = 0,
    val highSeasonMonths: Set<Int> = emptySet(),
) {
    fun priceOn(date: LocalDate): BigDecimal {
        val weekend = if (date.dayOfWeek in WEEKEND) weekendPct else 0
        val season = if (date.monthValue in highSeasonMonths) highSeasonPct else 0
        return base
            .multiply(
                BigDecimal(HUNDRED + weekend + season),
            ).divide(BigDecimal(HUNDRED), 0, RoundingMode.HALF_UP)
    }

    val valid: Boolean
        get() = weekendPct in 0..HUNDRED && highSeasonPct in 0..HUNDRED && highSeasonMonths.all { it in 1..MONTHS }

    companion object {
        const val HUNDRED = 100
        const val MONTHS = 12
        val WEEKEND = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    }
}

/** Capacidad por montaje (`025` RF-02, esquema `venues.setups`). */
data class VenueSetups(
    val banquet: Int = 0,
    val auditorium: Int = 0,
    val cocktail: Int = 0,
)

data class OwnerVenueTools(
    val id: String,
    val name: String,
    val published: Boolean,
    val pricing: VenuePricing,
    val setups: VenueSetups?,
)

/** Anticipos de las reservaciones de sus salones (`025` RF-03); la app no cobra. */
data class OwnerPayments(
    val bookings: List<Booking>,
    val received: BigDecimal,
    val pending: BigDecimal,
) {
    companion object {
        fun from(
            venues: Set<String>,
            bookings: List<Booking>,
        ): OwnerPayments {
            val own = bookings.filter { it.venueId in venues && it.status != BookingStatus.CANCELLED }

            fun sum(status: BookingStatus) =
                own.filter { it.status == status }.fold(BigDecimal.ZERO) { acc, b ->
                    acc +
                        (b.deposit ?: BigDecimal.ZERO)
                }
            return OwnerPayments(
                own.sortedByDescending {
                    it.date
                },
                sum(BookingStatus.CONFIRMED),
                sum(BookingStatus.PENDING_PAYMENT),
            )
        }
    }
}

data class MonthStats(
    val month: YearMonth,
    val requests: Int,
    val bookings: Int,
) {
    /** Reservaciones ÷ solicitudes, en porcentaje entero; 0 sin solicitudes. */
    val conversion: Int get() = if (requests == 0) 0 else bookings * VenuePricing.HUNDRED / requests
}

object OwnerStats {
    /** Últimos [months] meses (incluido el actual), contados por fecha de creación (`025` RF-04). */
    fun monthly(
        venues: Set<String>,
        quotes: List<Quote>,
        bookings: List<Booking>,
        months: Int,
        today: LocalDate,
    ): List<MonthStats> {
        val current = YearMonth.from(today)

        fun monthOf(createdAt: String?) =
            createdAt?.take(MONTH_LENGTH)?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
        val requests = quotes.filter { it.venueId in venues }.groupingBy { monthOf(it.createdAt) }.eachCount()
        val booked =
            bookings
                .filter { it.venueId in venues && it.status != BookingStatus.CANCELLED }
                .groupingBy { monthOf(it.createdAt) }
                .eachCount()
        return (months - 1 downTo 0).map { back ->
            val month = current.minusMonths(back.toLong())
            MonthStats(month, requests[month] ?: 0, booked[month] ?: 0)
        }
    }

    private const val MONTH_LENGTH = 7
}

interface OwnerToolsRepository {
    suspend fun venues(): AppResult<List<OwnerVenueTools>>

    suspend fun savePricing(
        venueId: String,
        pricing: VenuePricing,
    ): AppResult<Unit>

    suspend fun saveSetups(
        venueId: String,
        setups: VenueSetups,
    ): AppResult<Unit>
}

data class ReviewReply(
    val text: String,
    val at: String?,
)

/** Reseña de un salón (esquema `reviews`, contrato §5). */
data class Review(
    val id: String,
    val authorName: String,
    val stars: Int,
    val text: String,
    val createdAt: String?,
    val reply: ReviewReply?,
)

data class VenueReviews(
    val rating: Double,
    val count: Int,
    val reviews: List<Review>,
)

object ReviewRules {
    const val MAX_TEXT = 1000
    val STARS = 1..5

    fun valid(
        stars: Int,
        text: String,
    ): Boolean = stars in STARS && text.isNotBlank() && text.trim().length <= MAX_TEXT
}

interface ReviewsRepository {
    suspend fun venueReviews(venueId: String): AppResult<VenueReviews>

    suspend fun create(
        venueId: String,
        bookingId: String,
        stars: Int,
        text: String,
    ): AppResult<Review>

    suspend fun reply(
        reviewId: String,
        text: String,
    ): AppResult<Review>
}
