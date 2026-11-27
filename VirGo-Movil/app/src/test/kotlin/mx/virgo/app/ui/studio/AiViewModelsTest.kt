package mx.virgo.app.ui.studio

import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.ui.assistant.AssistantProblem
import mx.virgo.app.ui.assistant.AssistantViewModel
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AskAssistantUseCase
import mx.virgo.core.domain.AssistantReply
import mx.virgo.core.domain.GetDecorIdeasUseCase
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.domain.StaffRole
import mx.virgo.core.testing.FakeAiRepository
import mx.virgo.core.testing.FakeEventsRepository
import mx.virgo.core.testing.FakeVenueDetailRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleEvent
import mx.virgo.core.testing.sampleVenueDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AiViewModelsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val events = FakeEventsRepository()
    private val venues = FakeVenueDetailRepository()
    private val ai = FakeAiRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    private fun studio() = StudioViewModel(events, GetVenueDetailUseCase(venues), GetDecorIdeasUseCase(ai), session)

    private fun assistant() = AssistantViewModel(AskAssistantUseCase(ai), events, session)

    @Test
    fun studioTakesGuestsAndCapacityFromTheEvent() {
        events.events += sampleEvent()
        venues.result = AppResult.Success(sampleVenueDetail())
        val state = studio().state.value
        assertEquals(120, state.guests)
        assertEquals(sampleVenueDetail().capacity, state.venueCapacity)
        assertEquals(4, state.staff[StaffRole.WAITERS])
    }

    @Test
    fun staffCanBeAdjustedAndReset() {
        val vm = studio()
        vm.changeGuests("200")
        vm.adjustStaff(StaffRole.WAITERS, 2)
        assertEquals(9, vm.state.value.staff[StaffRole.WAITERS])
        assertTrue(vm.state.value.staffEdited)
        vm.resetStaff()
        assertFalse(vm.state.value.staffEdited)
        vm.changeGuests("abc")
        assertEquals("", vm.state.value.guestsInput)
        assertNull(vm.state.value.guests)
    }

    @Test
    fun ideasAreRequestedWhenTheTabOpensAndMissingEndpointIsReported() {
        events.events += sampleEvent()
        ai.ideasResult = AppResult.Failure(AppError.NotFound)
        val vm = studio()
        vm.selectTab(StudioTab.IDEAS)
        assertEquals(IdeasStatus.UNAVAILABLE, vm.state.value.ideasStatus)
        assertEquals(120, ai.ideaRequests.single().guests)
    }

    @Test
    fun assistantSendsWithTheEventAndShowsTheReply() {
        events.events += sampleEvent()
        ai.replyResult = AppResult.Success(AssistantReply("Unos 4 meseros.", uncertain = true))
        val vm = assistant()
        vm.send("¿Cuántos meseros necesito?")
        val messages = vm.state.value.messages
        assertEquals(2, messages.size)
        assertTrue(messages.last().uncertain)
        assertEquals(sampleEvent().id, ai.questions.single().second)
    }

    @Test
    fun assistantFailureKeepsTheQuestionForRetry() {
        ai.replyResult = AppResult.Failure(AppError.Network)
        val vm = assistant()
        vm.send("Hola")
        assertEquals(AssistantProblem.FAILED, vm.state.value.problem)
        ai.replyResult = AppResult.Success(AssistantReply("¡Hola!", uncertain = false))
        vm.retry()
        assertNull(vm.state.value.problem)
        assertEquals(
            listOf("Hola", "¡Hola!"),
            vm.state.value.messages
                .map { it.text },
        )
        assertEquals(2, ai.questions.size)
    }

    @Test
    fun assistantWithoutEndpointSaysItIsNotConnected() {
        ai.replyResult = AppResult.Failure(AppError.NotFound)
        val vm = assistant()
        vm.send("Hola")
        assertEquals(AssistantProblem.UNAVAILABLE, vm.state.value.problem)
    }
}
