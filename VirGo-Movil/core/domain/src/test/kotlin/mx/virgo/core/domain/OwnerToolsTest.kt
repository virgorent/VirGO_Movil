package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class OwnerToolsTest {
    private val pricing =
        VenuePricing(BigDecimal("3500"), "hour", 5, weekendPct = 15, highSeasonPct = 20, highSeasonMonths = setOf(11))

    private fun booking(
        id: String,
        status: BookingStatus,
        deposit: String,
        created: String,
    ) = Booking(
        id,
        "VG-$id",
        "a",
        "Salón",
        LocalDate.parse("2027-05-15"),
        4,
        null,
        BigDecimal(deposit),
        status,
        created,
    )

    @Test
    fun pricePreviewAddsWeekendAndSeason() {
        assertEquals(BigDecimal("4025"), pricing.priceOn(LocalDate.parse("2027-05-15")))
        assertEquals(BigDecimal("4200"), pricing.priceOn(LocalDate.parse("2026-11-24")))
        assertEquals(BigDecimal("4725"), pricing.priceOn(LocalDate.parse("2026-11-28")))
        assertEquals(BigDecimal("3500"), pricing.priceOn(LocalDate.parse("2027-05-11")))
        assertTrue(pricing.valid)
        assertFalse(pricing.copy(weekendPct = 101).valid)
    }

    @Test
    fun receivedPaymentsSumConfirmedAndPendingDeposits() {
        val payments =
            OwnerPayments.from(
                setOf("a"),
                listOf(
                    booking("1", BookingStatus.CONFIRMED, "15000", "2026-11-01T00:00:00Z"),
                    booking("2", BookingStatus.PENDING_PAYMENT, "3000", "2026-11-02T00:00:00Z"),
                    booking("3", BookingStatus.CANCELLED, "9000", "2026-11-03T00:00:00Z"),
                ),
            )
        assertEquals(BigDecimal("15000"), payments.received)
        assertEquals(BigDecimal("3000"), payments.pending)
        assertEquals(2, payments.bookings.size)
    }

    @Test
    fun monthlyStatsCountRequestsBookingsAndConversion() {
        val quote =
            Quote("q", "a", "Salón", null, null, 10, null, QuoteStatus.SENT, null, null, null, "2026-11-05T00:00:00Z")
        val stats =
            OwnerStats.monthly(
                setOf("a"),
                List(4) { quote.copy(id = "q$it") },
                listOf(booking("1", BookingStatus.CONFIRMED, "1", "2026-11-10T00:00:00Z")),
                months = 3,
                today = LocalDate.parse("2026-11-27"),
            )
        assertEquals(
            listOf(YearMonth.of(2026, 9), YearMonth.of(2026, 10), YearMonth.of(2026, 11)),
            stats.map { it.month },
        )
        assertEquals(25, stats.last().conversion)
        assertEquals(0, stats.first().conversion)
    }

    @Test
    fun reviewNeedsStarsAndText() {
        assertTrue(ReviewRules.valid(4, "Muy bien"))
        assertFalse(ReviewRules.valid(0, "Muy bien"))
        assertFalse(ReviewRules.valid(5, "  "))
    }
}
