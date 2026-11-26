package mx.virgo.app.ui.bookings

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.BookingField
import mx.virgo.core.domain.BookingProblem
import mx.virgo.core.domain.CreateBookingUseCase
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.testing.FakeBookingsRepository
import mx.virgo.core.testing.FakeVenueDetailRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleVenueDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class BookingConfirmViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val venues = FakeVenueDetailRepository().apply { result = AppResult.Success(sampleVenueDetail()) }
    private val bookings = FakeBookingsRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))
    private val saved = SavedStateHandle()
    private val day0 = LocalDate.parse("2026-11-25")

    private fun viewModel(handle: SavedStateHandle = saved) =
        BookingConfirmViewModel(handle, GetVenueDetailUseCase(venues), CreateBookingUseCase(bookings), session)
            .also { it.today = { day0 } }

    private fun BookingConfirmViewModel.ready(): BookingConfirmViewModel {
        start("v1")
        setDate(day0.plusDays(10))
        setHours("5")
        return this
    }

    @Test
    fun confirmCreatesOneBookingWithoutDepositAndShowsTheServerFolio() {
        val vm = viewModel().ready()
        vm.confirm()
        val state = vm.state.value
        assertEquals("VG-2027-0001", state.booking?.folio)
        assertEquals(1, bookings.keys.size)
        assertEquals(5, bookings.requests.single().hours)
        assertNull(bookings.requests.single().quoteId)
        vm.confirm()
        assertEquals(1, bookings.keys.size)
    }

    @Test
    fun quoteContextIsSentAndPrefillsTheDate() {
        val vm = viewModel()
        vm.start("v1", quoteId = "q-9", date = day0.plusDays(30))
        vm.setHours("5")
        vm.confirm()
        assertEquals("q-9", bookings.requests.single().quoteId)
        assertEquals(day0.plusDays(30), bookings.requests.single().date)
    }

    @Test
    fun withAQuoteTheDateIsFixedAndTheQuotedTotalIsKept() {
        val vm = viewModel()
        vm.start("v1", quoteId = "q-9", date = day0.plusDays(30), quotedTotal = java.math.BigDecimal("48000"))
        vm.setDate(day0.plusDays(40))
        assertEquals(day0.plusDays(30), vm.state.value.date)
        assertEquals("48000", vm.state.value.draft.quotedTotal)
    }

    @Test
    fun pastDateOrZeroHoursNeverReachTheServer() {
        val vm = viewModel()
        vm.start("v1")
        vm.setDate(day0.minusDays(1))
        vm.setHours("0")
        vm.confirm()
        assertEquals(BookingProblem.PAST_DATE, vm.state.value.fieldErrors[BookingField.DATE])
        assertEquals(BookingProblem.REQUIRED, vm.state.value.fieldErrors[BookingField.HOURS])
        assertTrue(bookings.keys.isEmpty())
    }

    @Test
    fun hoursBelowTheVenueMinimumAreRejectedLocally() {
        val vm = viewModel()
        vm.start("v1")
        vm.setDate(day0.plusDays(3))
        vm.setHours("3")
        vm.confirm()
        assertEquals(BookingProblem.BELOW_MINIMUM, vm.state.value.fieldErrors[BookingField.HOURS])
    }

    @Test
    fun networkRetryReusesTheKeyAndCreatesOnlyOneBooking() {
        val vm = viewModel().ready()
        bookings.createFailure = AppResult.Failure(AppError.Network)
        vm.confirm()
        assertEquals(BookingError.NETWORK, vm.state.value.error)
        bookings.createFailure = null
        vm.confirm()
        assertEquals(2, bookings.keys.size)
        assertEquals(bookings.keys[0], bookings.keys[1])
        assertEquals(1, bookings.bookings.size)
    }

    @Test
    fun changingDateOrHoursStartsANewAttempt() {
        val vm = viewModel().ready()
        val first = vm.state.value.draft.key
        vm.setHours("6")
        assertNotEquals(first, vm.state.value.draft.key)
        val second = vm.state.value.draft.key
        vm.setHours("6")
        assertEquals(second, vm.state.value.draft.key)
    }

    @Test
    fun serverRejectionIsShownNextToHours() {
        val vm = viewModel().ready()
        bookings.createFailure = AppResult.Failure(AppError.Validation(emptyMap()))
        vm.confirm()
        assertTrue(vm.state.value.rejected)
        assertFalse(vm.state.value.sending)
        vm.setHours("7")
        assertFalse(vm.state.value.rejected)
    }

    @Test
    fun otherErrorsAreMapped() {
        val vm = viewModel().ready()
        bookings.createFailure = AppResult.Failure(AppError.NotFound)
        vm.confirm()
        assertEquals(BookingError.NOT_FOUND, vm.state.value.error)
        bookings.createFailure = AppResult.Failure(AppError.Forbidden)
        vm.confirm()
        assertEquals(BookingError.FORBIDDEN, vm.state.value.error)
        bookings.createFailure = AppResult.Failure(AppError.Server("t"))
        vm.confirm()
        assertEquals(BookingError.SERVER, vm.state.value.error)
    }

    @Test
    fun unauthorizedEndsTheSession() =
        runTest {
            session.establish(sampleAuthSession())
            val vm = viewModel().ready()
            bookings.createFailure = AppResult.Failure(AppError.Unauthorized)
            vm.confirm()
            assertNull(session.state.value.session)
            assertEquals(SessionEndReason.REVOKED, session.state.value.endReason)
        }

    @Test
    fun venueFailuresAreHandled() {
        venues.result = AppResult.Failure(AppError.Network)
        val vm = viewModel().apply { start("v1") }
        assertTrue(vm.state.value.venueFailed)
        venues.result = AppResult.Success(sampleVenueDetail())
        vm.loadVenue()
        assertFalse(vm.state.value.venueFailed)
        venues.result = AppResult.Failure(AppError.NotFound)
        val missing = viewModel(SavedStateHandle()).apply { start("gone") }
        assertEquals(BookingError.NOT_FOUND, missing.state.value.error)
    }

    @Test
    fun draftSurvivesRecreationWithTheSameKey() {
        val vm = viewModel().ready()
        val key = vm.state.value.draft.key
        val restored = viewModel(SavedStateHandle(mapOf("booking_draft" to saved.get<String>("booking_draft"))))
        restored.start("v1")
        assertEquals(key, restored.state.value.draft.key)
        assertEquals("5", restored.state.value.draft.hours)
    }

    @Test
    fun bookingAgainAfterAReceiptStartsFresh() {
        val vm = viewModel().ready()
        vm.confirm()
        vm.start("v1")
        assertNull(vm.state.value.booking)
        assertEquals("", vm.state.value.draft.hours)
    }
}
