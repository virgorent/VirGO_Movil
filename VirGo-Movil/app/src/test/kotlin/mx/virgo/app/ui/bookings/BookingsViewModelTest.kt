package mx.virgo.app.ui.bookings

import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetBookingUseCase
import mx.virgo.core.domain.GetBookingsUseCase
import mx.virgo.core.testing.FakeBookingsRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleBooking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class BookingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookingsRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    private fun viewModel() = BookingsViewModel(GetBookingsUseCase(repository), GetBookingUseCase(repository), session)

    @Test
    fun listsAndOpensAReceipt() {
        repository.bookings += sampleBooking()
        val vm = viewModel()
        assertEquals(listOf(sampleBooking()), vm.state.value.bookings)
        vm.open(sampleBooking().id)
        assertEquals(
            "VG-2027-0001",
            vm.state.value.receipt
                ?.booking
                ?.folio,
        )
        vm.closeReceipt()
        assertNull(vm.state.value.receipt)
    }

    @Test
    fun missingReceiptFailsWithoutLeavingTheList() {
        val vm = viewModel()
        vm.open("gone")
        assertTrue(
            vm.state.value.receipt
                ?.failed == true,
        )
    }

    @Test
    fun listFailureCanBeRetried() {
        repository.listFailure = AppResult.Failure(AppError.Network)
        val vm = viewModel()
        assertTrue(vm.state.value.failed)
        repository.listFailure = null
        vm.refresh()
        assertTrue(!vm.state.value.failed)
    }

    @Test
    fun unauthorizedEndsTheSession() =
        runTest {
            session.establish(sampleAuthSession())
            repository.listFailure = AppResult.Failure(AppError.Unauthorized)
            viewModel()
            assertEquals(SessionEndReason.REVOKED, session.state.value.endReason)
        }
}
