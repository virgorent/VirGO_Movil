package mx.virgo.app.ui.ownerrequests

import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AnswerField
import mx.virgo.core.domain.AnswerProblem
import mx.virgo.core.domain.GetQuoteUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.QuoteStatus
import mx.virgo.core.domain.RespondQuoteUseCase
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
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OwnerRequestsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository =
        FakeQuotesRepository().apply {
            quotes += sampleQuote().copy(id = "new")
            quotes += sampleAnsweredQuote().copy(id = "done", createdAt = "2026-11-20T00:00:00Z")
            quotes += sampleQuote().copy(id = "gone", status = QuoteStatus.EXPIRED, createdAt = "2026-11-01T00:00:00Z")
        }
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    private fun viewModel() =
        OwnerRequestsViewModel(
            GetQuotesUseCase(repository),
            GetQuoteUseCase(repository),
            RespondQuoteUseCase(repository),
            session,
        )

    @Test
    fun tabsFilterByRealStatus() {
        val vm = viewModel()
        assertEquals(
            listOf("new"),
            vm.state.value.visible
                .map { it.id },
        )
        vm.selectTab(RequestsTab.ANSWERED)
        assertEquals(
            listOf("done"),
            vm.state.value.visible
                .map { it.id },
        )
        vm.selectTab(RequestsTab.ALL)
        assertEquals(
            listOf("new", "done", "gone"),
            vm.state.value.visible
                .map { it.id },
        )
    }

    @Test
    fun answeringSendsOnceAndUpdatesTheList() {
        val vm = viewModel()
        vm.open("new")
        vm.updateForm(AnswerForm(total = "48000", deposit = "14400", message = "Incluye limpieza"))
        vm.send()
        val detail = vm.state.value.detail!!
        assertEquals(QuoteStatus.ANSWERED, detail.quote?.status)
        assertTrue(detail.justAnswered)
        assertEquals(AnswerForm(), detail.form)
        assertEquals(1, repository.answers.size)
        assertEquals(
            0,
            BigDecimal("48000").compareTo(
                repository.answers
                    .single()
                    .second.total,
            ),
        )
        assertEquals(
            QuoteStatus.ANSWERED,
            vm.state.value.quotes
                .first { it.id == "new" }
                .status,
        )
        vm.send()
        assertEquals(1, repository.answers.size)
    }

    @Test
    fun invalidAmountsNeverReachTheServer() {
        val vm = viewModel()
        vm.open("new")
        vm.updateForm(AnswerForm(total = "", deposit = "10"))
        vm.send()
        assertEquals(
            AnswerProblem.REQUIRED,
            vm.state.value.detail!!
                .fieldErrors[AnswerField.TOTAL],
        )
        vm.updateForm(AnswerForm(total = "5", deposit = "10"))
        assertNull(
            vm.state.value.detail!!
                .fieldErrors[AnswerField.TOTAL],
        )
        vm.send()
        assertEquals(
            AnswerProblem.ABOVE_TOTAL,
            vm.state.value.detail!!
                .fieldErrors[AnswerField.DEPOSIT],
        )
        assertTrue(repository.answers.isEmpty())
    }

    @Test
    fun conflictWarnsAndReloadsTheServerState() {
        val vm = viewModel()
        vm.open("new")
        repository.quotes[0] = repository.quotes[0].copy(status = QuoteStatus.ANSWERED)
        // El detalle abierto aún cree que está «Enviada».
        repository.respondFailure = null
        val stale = vm.state.value.detail!!
        assertEquals(QuoteStatus.SENT, stale.quote?.status)
        vm.updateForm(AnswerForm(total = "100"))
        vm.send()
        val detail = vm.state.value.detail!!
        assertEquals(AnswerError.ALREADY_ANSWERED, detail.error)
        assertEquals(QuoteStatus.ANSWERED, detail.quote?.status)
        assertFalse(detail.sending)
    }

    @Test
    fun otherErrorsKeepTheForm() {
        val vm = viewModel()
        vm.open("new")
        vm.updateForm(AnswerForm(total = "100"))
        listOf(
            AppError.Network to AnswerError.NETWORK,
            AppError.NotFound to AnswerError.NOT_FOUND,
            AppError.Forbidden to AnswerError.FORBIDDEN,
            AppError.Validation(emptyMap()) to AnswerError.REJECTED,
            AppError.Server(null) to AnswerError.SERVER,
        ).forEach { (error, expected) ->
            repository.respondFailure = AppResult.Failure(error)
            vm.send()
            assertEquals(
                expected,
                vm.state.value.detail!!
                    .error,
            )
            assertEquals(
                "100",
                vm.state.value.detail!!
                    .form.total,
            )
        }
    }

    @Test
    fun closedDetailIsNotReopenedAndFailuresRetry() {
        val vm = viewModel()
        vm.open("missing")
        assertTrue(
            vm.state.value.detail!!
                .failed,
        )
        vm.closeDetail()
        assertNull(vm.state.value.detail)
        repository.listFailure = AppResult.Failure(AppError.Network)
        vm.refresh()
        assertTrue(vm.state.value.failed)
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
