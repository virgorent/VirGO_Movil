package mx.virgo.app.ui.quotes

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.CreateQuoteUseCase
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.QuoteField
import mx.virgo.core.domain.QuoteProblem
import mx.virgo.core.domain.QuoteStatus
import mx.virgo.core.testing.FakeQuotesRepository
import mx.virgo.core.testing.FakeVenueDetailRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleVenueDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class QuoteRequestViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val venues = FakeVenueDetailRepository().apply { result = AppResult.Success(sampleVenueDetail()) }
    private val quotes = FakeQuotesRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))
    private val saved = SavedStateHandle()
    private val day0 = LocalDate.parse("2026-11-25")

    private fun viewModel(handle: SavedStateHandle = saved) =
        QuoteRequestViewModel(handle, GetVenueDetailUseCase(venues), CreateQuoteUseCase(quotes), session)
            .also { it.today = { day0 } }

    private fun QuoteRequestViewModel.ready(): QuoteRequestViewModel {
        start("v1")
        setEventType(QuoteEventType.WEDDING)
        setDate(day0.plusDays(30))
        setGuests("120")
        return this
    }

    @Test
    fun sendCreatesOneQuoteAndShowsItAsSent() {
        val vm = viewModel().ready()
        vm.setMessage("Somos 120")
        vm.send()
        assertEquals(
            QuoteStatus.SENT,
            vm.state.value.sent
                ?.status,
        )
        assertEquals(1, quotes.requests.size)
        val sent = quotes.requests.single()
        assertEquals("v1", sent.venueId)
        assertEquals(QuoteEventType.WEDDING, sent.eventType)
        assertEquals(120, sent.guests)
        assertEquals("Somos 120", sent.message)
        vm.send()
        assertEquals(1, quotes.requests.size)
    }

    @Test
    fun invalidFieldsNeverReachTheServer() {
        val vm = viewModel()
        vm.start("v1")
        vm.setDate(day0.minusDays(1))
        vm.setGuests("0")
        vm.setMessage("a".repeat(501))
        vm.send()
        val errors = vm.state.value.fieldErrors
        assertEquals(QuoteProblem.REQUIRED, errors[QuoteField.EVENT_TYPE])
        assertEquals(QuoteProblem.PAST_DATE, errors[QuoteField.DATE])
        assertEquals(QuoteProblem.REQUIRED, errors[QuoteField.GUESTS])
        assertEquals(QuoteProblem.TOO_LONG, errors[QuoteField.MESSAGE])
        assertTrue(quotes.requests.isEmpty())
        vm.setEventType(QuoteEventType.OTHER)
        assertNull(vm.state.value.fieldErrors[QuoteField.EVENT_TYPE])
    }

    @Test
    fun guestsAboveCapacityWarnWithoutBlocking() {
        val vm = viewModel().ready()
        assertFalse(vm.state.value.capacityWarning)
        vm.setGuests("1000x")
        assertEquals("1000", vm.state.value.draft.guests)
        assertTrue(vm.state.value.capacityWarning)
        vm.send()
        assertEquals(1, quotes.requests.size)
    }

    @Test
    fun serverFieldErrorsAreShownNextToTheirField() {
        val vm = viewModel().ready()
        quotes.createFailure =
            AppResult.Failure(
                AppError.Validation(mapOf("eventDate" to listOf("La fecha no puede estar en el pasado."))),
            )
        vm.send()
        assertEquals("La fecha no puede estar en el pasado.", vm.state.value.serverErrors[QuoteField.DATE])
        assertFalse(vm.state.value.rejected)
        vm.setDate(day0.plusDays(40))
        assertTrue(
            vm.state.value.serverErrors
                .isEmpty(),
        )
        quotes.createFailure = AppResult.Failure(AppError.Validation(mapOf("target.id" to listOf("x"))))
        vm.send()
        assertTrue(vm.state.value.rejected)
    }

    @Test
    fun retryAfterANetworkFailureKeepsTheDataAndDoesNotDuplicate() {
        val vm = viewModel().ready()
        quotes.createFailure = AppResult.Failure(AppError.Timeout)
        quotes.landsDespiteFailure = true
        vm.send()
        assertEquals(QuoteError.NETWORK, vm.state.value.error)
        assertEquals("120", vm.state.value.draft.guests)
        assertTrue(vm.state.value.draft.uncertain)
        quotes.createFailure = null
        vm.send()
        assertEquals(1, quotes.quotes.size)
        assertEquals(1, quotes.requests.size)
        assertEquals(
            quotes.quotes.single().id,
            vm.state.value.sent
                ?.id,
        )
        assertFalse(vm.state.value.draft.uncertain)
    }

    @Test
    fun otherErrorsAreMapped() {
        val vm = viewModel().ready()
        quotes.createFailure = AppResult.Failure(AppError.NotFound)
        vm.send()
        assertEquals(QuoteError.NOT_FOUND, vm.state.value.error)
        quotes.createFailure = AppResult.Failure(AppError.Forbidden)
        vm.send()
        assertEquals(QuoteError.FORBIDDEN, vm.state.value.error)
        quotes.createFailure = AppResult.Failure(AppError.Server("t"))
        vm.send()
        assertEquals(QuoteError.SERVER, vm.state.value.error)
        assertFalse(vm.state.value.draft.uncertain)
    }

    @Test
    fun unauthorizedEndsTheSession() =
        runTest {
            session.establish(sampleAuthSession())
            val vm = viewModel().ready()
            quotes.createFailure = AppResult.Failure(AppError.Unauthorized)
            vm.send()
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
        assertEquals(QuoteError.NOT_FOUND, missing.state.value.error)
    }

    @Test
    fun draftSurvivesRecreationAndANewVenueStartsFresh() {
        viewModel().ready()
        val restored = viewModel(SavedStateHandle(mapOf("quote_draft" to saved.get<String>("quote_draft"))))
        restored.start("v1")
        assertEquals("120", restored.state.value.draft.guests)
        assertEquals(QuoteEventType.WEDDING, restored.state.value.eventType)
        restored.start("v2")
        assertEquals("", restored.state.value.draft.guests)
    }
}
