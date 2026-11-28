package mx.virgo.app.ui.quotes

import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetQuoteUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.testing.FakeQuotesRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAnsweredQuote
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleQuote
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class QuotesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeQuotesRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    private fun viewModel() = QuotesViewModel(GetQuotesUseCase(repository), GetQuoteUseCase(repository), session)

    @Test
    fun listsNewestFirstAndOpensTheDetail() {
        repository.quotes += sampleQuote().copy(id = "old", createdAt = "2026-11-01T00:00:00Z")
        repository.quotes += sampleAnsweredQuote()
        val vm = viewModel()
        assertEquals(
            listOf(sampleAnsweredQuote().id, "old"),
            vm.state.value.quotes
                .map { it.id },
        )
        vm.open(sampleAnsweredQuote().id)
        assertTrue(
            vm.state.value.detail
                ?.quote
                ?.canBook == true,
        )
        vm.closeDetail()
        assertNull(vm.state.value.detail)
    }

    @Test
    fun missingDetailFailsWithoutLeavingTheList() {
        val vm = viewModel()
        vm.open("gone")
        assertTrue(
            vm.state.value.detail
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
        assertFalse(vm.state.value.failed)
        assertFalse(vm.state.value.loading)
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
