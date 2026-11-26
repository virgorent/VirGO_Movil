package mx.virgo.core.testing

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ClientEvent
import mx.virgo.core.domain.EventGuest
import mx.virgo.core.domain.EventInput
import mx.virgo.core.domain.EventPlanningRepository
import mx.virgo.core.domain.EventTask
import mx.virgo.core.domain.EventsRepository
import mx.virgo.core.domain.GuestInput
import mx.virgo.core.domain.GuestStatus
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.TaskInput
import java.math.BigDecimal
import java.time.LocalDate

/** Como el servidor: crear agrega, editar reemplaza todo. `failure` aplica a cualquier llamada. */
class FakeEventsRepository : EventsRepository {
    val events = mutableListOf<ClientEvent>()
    val inputs = mutableListOf<Pair<String?, EventInput>>()
    var failure: AppResult.Failure? = null

    override suspend fun list(): AppResult<List<ClientEvent>> = failure ?: AppResult.Success(events.toList())

    override suspend fun get(id: String): AppResult<ClientEvent> =
        failure ?: events.firstOrNull { it.id == id }?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.NotFound)

    override suspend fun create(input: EventInput): AppResult<ClientEvent> {
        inputs += null to input
        failure?.let { return it }
        val created = input.toEvent("event-${events.size + 1}", venueName = null)
        events += created
        return AppResult.Success(created)
    }

    override suspend fun update(
        id: String,
        input: EventInput,
    ): AppResult<ClientEvent> {
        inputs += id to input
        val index = events.indexOfFirst { it.id == id }
        val current = failure ?: if (index < 0) AppResult.Failure(AppError.NotFound) else null
        if (current != null) return current
        val updated = input.toEvent(id, events[index].venueName.takeIf { input.venueId != null })
        events[index] = updated
        return AppResult.Success(updated)
    }
}

private fun EventInput.toEvent(
    id: String,
    venueName: String?,
) = ClientEvent(id, name, eventType, date, guests, budget, hours, venueId, venueName)

/** Invitados y pendientes en memoria, con los mismos ids que daría el servidor. */
class FakeEventPlanningRepository : EventPlanningRepository {
    val guests = mutableListOf<EventGuest>()
    val tasks = mutableListOf<EventTask>()
    var failure: AppResult.Failure? = null
    val savedGuests = mutableListOf<GuestInput>()
    val savedTasks = mutableListOf<TaskInput>()

    override suspend fun guests(eventId: String): AppResult<List<EventGuest>> =
        failure ?: AppResult.Success(guests.toList())

    override suspend fun saveGuest(
        eventId: String,
        guestId: String?,
        input: GuestInput,
    ): AppResult<EventGuest> {
        savedGuests += input
        failure?.let { return it }
        val guest =
            EventGuest(
                guestId ?: "guest-${guests.size + 1}",
                input.name,
                input.phone,
                input.companions,
                input.status,
                input.table,
            )
        val index = guests.indexOfFirst { it.id == guest.id }
        if (index < 0) guests += guest else guests[index] = guest
        return AppResult.Success(guest)
    }

    override suspend fun deleteGuest(
        eventId: String,
        guestId: String,
    ): AppResult<Unit> = failure ?: AppResult.Success(Unit).also { guests.removeAll { it.id == guestId } }

    override suspend fun tasks(eventId: String): AppResult<List<EventTask>> =
        failure ?: AppResult.Success(tasks.toList())

    override suspend fun saveTask(
        eventId: String,
        taskId: String?,
        input: TaskInput,
    ): AppResult<EventTask> {
        savedTasks += input
        failure?.let { return it }
        val task = EventTask(taskId ?: "task-${tasks.size + 1}", input.title, input.dueDate, input.done)
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index < 0) tasks += task else tasks[index] = task
        return AppResult.Success(task)
    }

    override suspend fun deleteTask(
        eventId: String,
        taskId: String,
    ): AppResult<Unit> = failure ?: AppResult.Success(Unit).also { tasks.removeAll { it.id == taskId } }
}

fun sampleEvent(): ClientEvent =
    ClientEvent(
        id = "3f0c1d2e-0000-4000-8000-000000000001",
        name = "Boda de Ana y Luis",
        eventType = QuoteEventType.WEDDING,
        date = LocalDate.parse("2027-05-15"),
        guests = 120,
        budget = BigDecimal("150000"),
        hours = 6,
        venueId = "132a1f58-ebf9-4082-acdf-7241975f1811",
        venueName = "Salón Las Palmas",
    )

fun sampleGuest(): EventGuest = EventGuest("guest-a", "María Pérez", "4421234567", 1, GuestStatus.CONFIRMED, "Mesa 3")

fun sampleTask(): EventTask = EventTask("task-a", "Contratar fotógrafo", LocalDate.parse("2027-02-01"), done = false)
