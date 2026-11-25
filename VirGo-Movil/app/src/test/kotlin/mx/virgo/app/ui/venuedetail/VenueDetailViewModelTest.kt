package mx.virgo.app.ui.venuedetail

import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.testing.FakeVenueDetailRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleVenueDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

class VenueDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeVenueDetailRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    private fun viewModel() = VenueDetailViewModel(GetVenueDetailUseCase(repository), session)

    @Test
    fun loadsTheVenueForAGuest() {
        repository.result = AppResult.Success(sampleVenueDetail())
        val vm = viewModel()
        vm.load("v1")
        val state = vm.state.value
        assertFalse(state.loading)
        assertEquals("Salón Las Palmas", state.venue?.name)
        assertEquals(DetailViewer.GUEST, state.viewer)
        assertEquals(listOf("v1"), repository.requested)
    }

    @Test
    fun sameIdIsNotRequestedTwiceButRetryIs() {
        repository.result = AppResult.Success(sampleVenueDetail())
        val vm = viewModel()
        vm.load("v1")
        vm.load("v1")
        assertEquals(1, repository.requested.size)
        vm.retry()
        assertEquals(2, repository.requested.size)
    }

    @Test
    fun failureKeepsTheErrorAndRetryRecovers() {
        repository.result = AppResult.Failure(AppError.Network)
        val vm = viewModel()
        vm.load("v1")
        assertEquals(AppError.Network, vm.state.value.error)
        assertNull(vm.state.value.venue)
        repository.result = AppResult.Success(sampleVenueDetail())
        vm.load("v1")
        assertNull(vm.state.value.error)
        assertEquals("v1", vm.state.value.venueId)
    }

    @Test
    fun notFoundIsReported() {
        repository.result = AppResult.Failure(AppError.NotFound)
        val vm = viewModel()
        vm.load("gone")
        assertEquals(AppError.NotFound, vm.state.value.error)
    }

    @Test
    fun sessionRoleDecidesTheViewer() =
        runTest {
            repository.result = AppResult.Success(sampleVenueDetail())
            session.establish(sampleAuthSession())
            val client = viewModel().apply { load("v1") }
            assertEquals(DetailViewer.CLIENT, client.state.value.viewer)
            session.establish(sampleAuthSession().copy(user = sampleAuthSession().user.copy(role = "owner")))
            val owner = viewModel().apply { load("v1") }
            assertEquals(DetailViewer.OTHER_ROLE, owner.state.value.viewer)
        }

    @Test
    fun moneyUsesMxnAndHidesZeroCents() {
        assertEquals("$1,600", formatMxn(1600.0, Locale.forLanguageTag("es-MX")))
        assertEquals("$1,600.50", formatMxn(1600.5, Locale.forLanguageTag("es-MX")))
    }
}
