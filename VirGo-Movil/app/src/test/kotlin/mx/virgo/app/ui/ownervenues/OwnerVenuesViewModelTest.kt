package mx.virgo.app.ui.ownervenues

import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.testing.FakeOwnerVenuesRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleVenueDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OwnerVenuesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeOwnerVenuesRepository()
    private val store = FakeSessionTokenStore()
    private val session = SessionRepository(store, Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    private fun viewModel() = OwnerVenuesViewModel(GetMyVenuesUseCase(repository), session)

    @Test
    fun loadsMyVenuesAndKeepsTheOutcome() {
        val venue = OwnerVenue("v1", sampleVenueDraft(), VenueStatus.DRAFT)
        repository.venues += venue
        val vm = viewModel()
        assertFalse(vm.state.value.loading)
        assertEquals(listOf(venue), vm.state.value.venues)
        vm.refresh(VenueWizardOutcome.PUBLISHED)
        assertEquals(VenueWizardOutcome.PUBLISHED, vm.state.value.outcome)
        vm.refresh()
        assertNull(vm.state.value.outcome)
    }

    @Test
    fun failureIsShownWithoutEndingTheSession() {
        repository.mineFailure = AppResult.Failure(AppError.Network)
        val state = viewModel().state.value
        assertEquals(AppError.Network, state.error)
        assertFalse(state.loading)
    }

    @Test
    fun unauthorizedEndsTheSession() =
        runTest {
            session.establish(sampleAuthSession())
            repository.mineFailure = AppResult.Failure(AppError.Unauthorized)
            viewModel()
            assertNull(session.state.value.session)
            assertEquals(SessionEndReason.REVOKED, session.state.value.endReason)
        }
}
