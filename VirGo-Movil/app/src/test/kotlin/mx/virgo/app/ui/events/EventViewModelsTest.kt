package mx.virgo.app.ui.events

import kotlinx.coroutines.test.runTest
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.EventField
import mx.virgo.core.domain.EventProblem
import mx.virgo.core.domain.GuestField
import mx.virgo.core.domain.GuestStatus
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.TaskField
import mx.virgo.core.testing.FakeEventPlanningRepository
import mx.virgo.core.testing.FakeEventsRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleAuthSession
import mx.virgo.core.testing.sampleEvent
import mx.virgo.core.testing.sampleGuest
import mx.virgo.core.testing.sampleTask
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

class EventViewModelsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val events = FakeEventsRepository()
    private val planning = FakeEventPlanningRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))
    private val day0 = LocalDate.parse("2026-11-25")

    private fun form() = EventFormViewModel(events, session).also { it.today = { day0 } }

    @Test
    fun listAndDetailComeFromTheServer() {
        events.events += sampleEvent()
        val vm = EventsViewModel(events, session)
        assertEquals(listOf(sampleEvent()), vm.state.value.events)
        vm.open(sampleEvent().id)
        assertEquals(
            sampleEvent(),
            vm.state.value.detail
                ?.event,
        )
        vm.open("gone")
        assertEquals(
            EventError.NOT_FOUND,
            vm.state.value.detail
                ?.error,
        )
        vm.closeDetail()
        assertNull(vm.state.value.detail)
        events.failure = AppResult.Failure(AppError.Network)
        vm.refresh()
        assertTrue(vm.state.value.failed)
    }

    @Test
    fun createSendsOnceAndReportsTheSavedEvent() {
        val vm = form()
        vm.start(null)
        vm.update(
            vm.state.value.form
                .copy(name = "Boda", eventType = QuoteEventType.WEDDING, date = day0.plusDays(9)),
        )
        vm.update(
            vm.state.value.form
                .copy(guests = "100", hours = "5", budget = "90000"),
        )
        vm.save()
        assertEquals(
            "event-1",
            vm.state.value.saved
                ?.id,
        )
        assertEquals(1, events.inputs.size)
        vm.save()
        assertEquals(2, events.inputs.size)
    }

    @Test
    fun editingKeepsTheVenueAndAPastOriginalDate() {
        events.events += sampleEvent().copy(date = day0.minusDays(2))
        val vm = form()
        vm.start(sampleEvent().id)
        assertEquals("150000", vm.state.value.form.budget)
        vm.update(
            vm.state.value.form
                .copy(name = "Boda civil"),
        )
        vm.save()
        val (id, input) = events.inputs.single()
        assertEquals(sampleEvent().id, id)
        assertEquals(sampleEvent().venueId, input.venueId)
        assertEquals("Boda civil", input.name)
    }

    @Test
    fun invalidFormAndServerFieldErrorsAreShown() {
        val vm = form()
        vm.start(null)
        vm.save()
        assertEquals(EventProblem.REQUIRED, vm.state.value.fieldErrors[EventField.NAME])
        assertTrue(events.inputs.isEmpty())
        vm.update(
            vm.state.value.form
                .copy(name = "x"),
        )
        assertNull(vm.state.value.fieldErrors[EventField.NAME])
        vm.update(
            vm.state.value.form.copy(
                eventType = QuoteEventType.OTHER,
                date = day0,
                guests = "1",
                hours = "1",
                budget = "0",
            ),
        )
        events.failure = AppResult.Failure(AppError.Validation(mapOf("date" to listOf("date no es válida."))))
        vm.save()
        assertEquals("date no es válida.", vm.state.value.serverErrors[EventField.DATE])
        events.failure = AppResult.Failure(AppError.Network)
        vm.save()
        assertEquals(EventError.NETWORK, vm.state.value.error)
        assertFalse(vm.state.value.saving)
    }

    @Test
    fun missingEventToEditShowsTheError() {
        val vm = form()
        vm.start("gone")
        assertEquals(EventError.NOT_FOUND, vm.state.value.error)
    }

    @Test
    fun guestsAreAddedEditedAndDeletedAfterConfirmation() {
        planning.guests += sampleGuest()
        val vm = GuestsViewModel(planning, session)
        vm.start("e1")
        assertEquals(2, vm.state.value.totals.confirmed)
        vm.edit(null)
        vm.updateEditor(
            vm.state.value.editor!!
                .copy(name = "Luis", phone = ""),
        )
        vm.save()
        assertEquals(
            setOf(GuestField.PHONE),
            vm.state.value.editor
                ?.errors,
        )
        vm.updateEditor(
            vm.state.value.editor!!
                .copy(phone = "4420000000", status = GuestStatus.DECLINED),
        )
        vm.save()
        assertNull(vm.state.value.editor)
        assertEquals(2, vm.state.value.guests.size)
        vm.edit(sampleGuest())
        vm.updateEditor(
            vm.state.value.editor!!
                .copy(companions = "3"),
        )
        vm.save()
        assertEquals(4, vm.state.value.totals.confirmed)
        vm.delete(sampleGuest())
        assertEquals(sampleGuest(), vm.state.value.deleting)
        assertEquals(2, vm.state.value.guests.size)
        vm.delete(sampleGuest(), confirm = true)
        assertEquals(1, vm.state.value.guests.size)
        planning.failure = AppResult.Failure(AppError.Server(null))
        vm.edit(null, close = true)
        vm.refresh()
        assertTrue(vm.state.value.failed)
    }

    @Test
    fun tasksAreToggledSavedAndDeleted() {
        planning.tasks += sampleTask()
        val vm = TasksViewModel(planning, session)
        vm.start("e1")
        vm.toggle(sampleTask())
        assertTrue(
            vm.state.value.tasks
                .single()
                .done,
        )
        assertTrue(planning.savedTasks.single().done)
        vm.edit(null)
        vm.save()
        assertEquals(
            setOf(TaskField.TITLE, TaskField.DUE_DATE),
            vm.state.value.editor
                ?.errors,
        )
        vm.updateEditor(
            vm.state.value.editor!!
                .copy(title = "Flores", dueDate = day0),
        )
        vm.save()
        assertEquals(2, vm.state.value.tasks.size)
        assertNull(vm.state.value.editor)
        vm.delete(
            vm.state.value.tasks
                .last(),
            confirm = true,
        )
        assertEquals(1, vm.state.value.tasks.size)
        planning.failure = AppResult.Failure(AppError.NotFound)
        vm.toggle(
            vm.state.value.tasks
                .single(),
        )
        assertEquals(EventError.NOT_FOUND, vm.state.value.error)
    }

    @Test
    fun unauthorizedEndsTheSession() =
        runTest {
            session.establish(sampleAuthSession())
            events.failure = AppResult.Failure(AppError.Unauthorized)
            EventsViewModel(events, session)
            assertEquals(SessionEndReason.REVOKED, session.state.value.endReason)
        }
}
