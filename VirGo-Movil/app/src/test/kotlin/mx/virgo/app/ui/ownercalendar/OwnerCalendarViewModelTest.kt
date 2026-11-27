package mx.virgo.app.ui.ownercalendar

import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.ui.ownerhome.OwnerHomeViewModel
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.BlockDayUseCase
import mx.virgo.core.domain.BlockReason
import mx.virgo.core.domain.GetBookingsUseCase
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.GetVenueDaysUseCase
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.UnblockDayUseCase
import mx.virgo.core.domain.VenueDay
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.testing.FakeAvailabilityRepository
import mx.virgo.core.testing.FakeBookingsRepository
import mx.virgo.core.testing.FakeOwnerVenuesRepository
import mx.virgo.core.testing.FakeQuotesRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleBooking
import mx.virgo.core.testing.sampleQuote
import mx.virgo.core.testing.sampleVenueDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

class OwnerCalendarViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val venues = FakeOwnerVenuesRepository()
    private val bookings = FakeBookingsRepository()
    private val quotes = FakeQuotesRepository()
    private val availability = FakeAvailabilityRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))
    private val today = LocalDate.parse("2027-05-10")

    private fun publishedVenue() = OwnerVenue(VENUE, sampleVenueDraft(), VenueStatus.PUBLISHED)

    private fun calendar(): OwnerCalendarViewModel =
        OwnerCalendarViewModel(
            GetMyVenuesUseCase(venues),
            GetBookingsUseCase(bookings),
            GetVenueDaysUseCase(availability),
            BlockDayUseCase(availability),
            UnblockDayUseCase(availability),
            session,
        ).also {
            it.today = { today }
            it.refresh()
        }

    private fun home() =
        OwnerHomeViewModel(GetMyVenuesUseCase(venues), GetQuotesUseCase(quotes), GetBookingsUseCase(bookings), session)

    @Test
    fun loadsPublishedVenuesBookingsAndBlocksOfTheMonth() {
        venues.venues += publishedVenue()
        venues.venues += OwnerVenue("draft", sampleVenueDraft(), VenueStatus.DRAFT)
        bookings.bookings += sampleBooking()
        availability.days[VENUE] = mutableListOf(VenueDay(LocalDate.parse("2027-05-25"), blocked = true))
        val vm = calendar()
        val state = vm.state.value
        assertEquals(listOf(VENUE), state.venues.map { it.id })
        assertEquals(YearMonth.of(2027, 5), state.month)
        assertTrue(state.days.first { it.date.dayOfMonth == 15 }.booked)
        assertFalse(state.days.first { it.date.dayOfMonth == 25 }.free)
        assertEquals(LocalDate.parse("2027-05-31"), availability.queries.last().third)
    }

    @Test
    fun changingMonthReloadsTheDays() {
        venues.venues += publishedVenue()
        val vm = calendar()
        vm.changeMonth(1)
        assertEquals(YearMonth.of(2027, 6), vm.state.value.month)
        assertEquals(LocalDate.parse("2027-06-01"), availability.queries.last().second)
    }

    @Test
    fun blockingAFreeDayMarksItAndConflictsAreReported() {
        venues.venues += publishedVenue()
        val vm = calendar()
        vm.selectDay(LocalDate.parse("2027-05-20"))
        vm.openBlock()
        vm.updateBlock(BlockForm(BlockReason.MAINTENANCE, "Pintura"))
        vm.confirmBlock()
        assertNull(vm.state.value.blockForm)
        assertEquals(
            BlockReason.MAINTENANCE,
            vm.state.value.selectedDay
                ?.block
                ?.reason,
        )
        availability.blockFailure = AppResult.Failure(AppError.Conflict)
        vm.selectDay(LocalDate.parse("2027-05-21"))
        vm.openBlock()
        vm.updateBlock(BlockForm(BlockReason.OTHER))
        vm.confirmBlock()
        assertEquals(BlockError.NOT_FREE, vm.state.value.blockError)
        assertTrue(
            vm.state.value.selectedDay
                ?.free == true,
        )
    }

    @Test
    fun pastOrBookedDaysCannotBeBlocked() {
        venues.venues += publishedVenue()
        bookings.bookings += sampleBooking()
        val vm = calendar()
        vm.selectDay(LocalDate.parse("2027-05-02"))
        vm.openBlock()
        assertNull(vm.state.value.blockForm)
        vm.selectDay(LocalDate.parse("2027-05-15"))
        vm.openBlock()
        assertNull(vm.state.value.blockForm)
        assertEquals(0, availability.blockCalls)
    }

    @Test
    fun unblockUsesTheBlockId() {
        venues.venues += publishedVenue()
        availability.days[VENUE] =
            mutableListOf(VenueDay(LocalDate.parse("2027-05-25"), blocked = true, blockId = "b-9"))
        val vm = calendar()
        vm.selectDay(LocalDate.parse("2027-05-25"))
        vm.unblock()
        assertTrue(
            vm.state.value.selectedDay
                ?.free == true,
        )
    }

    @Test
    fun unauthorizedEndsTheSession() =
        runTest {
            session.establish(sampleAuthSession())
            venues.mineFailure = AppResult.Failure(AppError.Unauthorized)
            calendar()
            assertEquals(SessionEndReason.REVOKED, session.state.value.endReason)
        }

    @Test
    fun homeDashboardUsesRealData() {
        venues.venues += publishedVenue()
        bookings.bookings += sampleBooking()
        quotes.quotes += sampleQuote()
        val vm =
            home().also {
                it.today = { today }
                it.refresh()
            }
        val dashboard = vm.state.value.dashboard
        assertEquals(1, dashboard?.publishedVenues)
        assertEquals(1, dashboard?.pendingRequests)
        assertEquals(1, dashboard?.upcomingBookings)
    }

    @Test
    fun homeNetworkFailureCanBeRetried() {
        venues.mineFailure = AppResult.Failure(AppError.Network)
        val vm =
            home()
        assertTrue(vm.state.value.failed)
        venues.mineFailure = null
        vm.refresh()
        assertFalse(vm.state.value.failed)
        assertEquals(
            0,
            vm.state.value.dashboard
                ?.registeredVenues,
        )
    }
}

private const val VENUE = "132a1f58-ebf9-4082-acdf-7241975f1811"
