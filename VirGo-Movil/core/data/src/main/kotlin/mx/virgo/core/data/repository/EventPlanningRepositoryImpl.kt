package mx.virgo.core.data.repository

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.EventGuest
import mx.virgo.core.domain.EventPlanningRepository
import mx.virgo.core.domain.EventTask
import mx.virgo.core.domain.GuestInput
import mx.virgo.core.domain.GuestStatus
import mx.virgo.core.domain.TaskInput
import mx.virgo.core.network.api.EventPlanningApi
import mx.virgo.core.network.dto.EventGuestDto
import mx.virgo.core.network.dto.EventGuestRequestDto
import mx.virgo.core.network.dto.EventTaskDto
import mx.virgo.core.network.dto.EventTaskRequestDto
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class EventPlanningRepositoryImpl
    @Inject
    constructor(
        private val api: EventPlanningApi,
    ) : EventPlanningRepository {
        override suspend fun guests(eventId: String): AppResult<List<EventGuest>> =
            eventCall { AppResult.Success(api.guests(eventId).data.map { it.toGuest() }) }

        override suspend fun saveGuest(
            eventId: String,
            guestId: String?,
            input: GuestInput,
        ): AppResult<EventGuest> =
            eventCall {
                val body =
                    EventGuestRequestDto(input.name, input.phone, input.companions, input.status.wire, input.table)
                val saved =
                    if (guestId == null) api.createGuest(eventId, body) else api.updateGuest(eventId, guestId, body)
                AppResult.Success(saved.toGuest())
            }

        override suspend fun deleteGuest(
            eventId: String,
            guestId: String,
        ): AppResult<Unit> = eventCall { api.deleteGuest(eventId, guestId).toUnit() }

        override suspend fun tasks(eventId: String): AppResult<List<EventTask>> =
            eventCall { AppResult.Success(api.tasks(eventId).data.map { it.toTask() }) }

        override suspend fun saveTask(
            eventId: String,
            taskId: String?,
            input: TaskInput,
        ): AppResult<EventTask> =
            eventCall {
                val body = EventTaskRequestDto(input.title, input.dueDate.toString(), input.done)
                val saved = if (taskId == null) api.createTask(eventId, body) else api.updateTask(eventId, taskId, body)
                AppResult.Success(saved.toTask())
            }

        override suspend fun deleteTask(
            eventId: String,
            taskId: String,
        ): AppResult<Unit> = eventCall { api.deleteTask(eventId, taskId).toUnit() }
    }

private fun Response<Unit>.toUnit(): AppResult<Unit> =
    if (isSuccessful) AppResult.Success(Unit) else AppResult.Failure(HttpException(this).toAppError())

internal fun EventGuestDto.toGuest(): EventGuest =
    EventGuest(
        id = id,
        name = name.orEmpty(),
        phone = phone.orEmpty(),
        companions = companions ?: 0,
        status = GuestStatus.fromWire(status),
        table = table?.takeIf { it.isNotBlank() },
    )

internal fun EventTaskDto.toTask(): EventTask =
    EventTask(id = id, title = title.orEmpty(), dueDate = parseDate(dueDate), done = done ?: false)
