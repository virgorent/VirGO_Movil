package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class OwnerCalendarTest {
    private val today = LocalDate.parse("2027-05-10")

    private fun booking(
        id: String,
        venue: String,
        date: String,
        status: BookingStatus = BookingStatus.PENDING_PAYMENT,
    ) = Booking(id, "VG-$id", venue, "Salón $venue", LocalDate.parse(date), 4, null, null, status, null)

    private fun venue(
        id: String,
        status: VenueStatus,
    ) = OwnerVenue(id, VenueDraft(name = "Salón $id"), status)

    private fun quote(
        venue: String,
        status: QuoteStatus,
    ) = Quote(
        id = "q-$venue-$status",
        venueId = venue,
        venueName = "Salón",
        eventType = null,
        eventDate = null,
        guests = 10,
        message = null,
        status = status,
        total = null,
        deposit = null,
        responseMessage = null,
        createdAt = null,
    )

    @Test
    fun dashboardCountsOnlyOwnVenuesAndUpcomingBookings() {
        val dashboard =
            OwnerDashboard.from(
                venues = listOf(venue("a", VenueStatus.PUBLISHED), venue("b", VenueStatus.DRAFT)),
                quotes =
                    listOf(
                        quote("a", QuoteStatus.SENT),
                        quote("a", QuoteStatus.ANSWERED),
                        quote("x", QuoteStatus.SENT),
                    ),
                bookings =
                    listOf(
                        booking("1", "a", "2027-05-20"),
                        booking("2", "a", "2027-05-10"),
                        booking("3", "a", "2027-05-01"),
                        booking("4", "a", "2027-06-01", BookingStatus.CANCELLED),
                        booking("5", "x", "2027-06-02"),
                        booking("6", "a", "2027-07-01"),
                        booking("7", "a", "2027-08-01"),
                    ),
                today = today,
            )
        assertEquals(1, dashboard.publishedVenues)
        assertEquals(2, dashboard.registeredVenues)
        assertEquals(1, dashboard.pendingRequests)
        assertEquals(4, dashboard.upcomingBookings)
        assertEquals(listOf("2", "1", "6"), dashboard.nextBookings.map { it.id })
    }

    @Test
    fun monthMarksBookedBlockedAndOverlappingDays() {
        val days =
            OwnerCalendar.month(
                YearMonth.of(2027, 5),
                "a",
                listOf(
                    booking("1", "a", "2027-05-20"),
                    booking("2", "a", "2027-05-20"),
                    booking("3", "b", "2027-05-21"),
                    booking("4", "a", "2027-05-22", BookingStatus.CANCELLED),
                ),
                listOf(VenueDay(LocalDate.parse("2027-05-25"), blocked = true, reason = BlockReason.MAINTENANCE)),
            )
        assertEquals(31, days.size)
        val day20 = days.first { it.date.dayOfMonth == 20 }
        assertTrue(day20.booked)
        assertTrue(day20.overlapping)
        assertTrue(days.first { it.date.dayOfMonth == 21 }.free)
        assertTrue(days.first { it.date.dayOfMonth == 22 }.free)
        val day25 = days.first { it.date.dayOfMonth == 25 }
        assertFalse(day25.free)
        assertEquals(BlockReason.MAINTENANCE, day25.block?.reason)
    }

    @Test
    fun blockReasonsUseTheServerWords() {
        assertEquals(listOf("maintenance", "own_event", "other"), BlockReason.entries.map { it.wire })
        assertEquals(BlockReason.OWN_EVENT, BlockReason.fromWire("own_event"))
        assertNull(BlockReason.fromWire("vacaciones"))
    }
}
