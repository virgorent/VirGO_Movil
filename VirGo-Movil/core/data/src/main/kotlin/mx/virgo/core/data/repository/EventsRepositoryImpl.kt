package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ClientEvent
import mx.virgo.core.domain.EventInput
import mx.virgo.core.domain.EventsRepository
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.network.api.EventsApi
import mx.virgo.core.network.dto.EventDto
import mx.virgo.core.network.dto.EventRequestDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

class EventsRepositoryImpl
    @Inject
    constructor(
        private val api: EventsApi,
    ) : EventsRepository {
        override suspend fun list(): AppResult<List<ClientEvent>> =
            eventCall { AppResult.Success(api.list().data.map { it.toEvent() }) }

        override suspend fun get(id: String): AppResult<ClientEvent> =
            eventCall { AppResult.Success(api.get(id).toEvent()) }

        override suspend fun create(input: EventInput): AppResult<ClientEvent> =
            eventCall { AppResult.Success(api.create(input.toDto()).toEvent()) }

        override suspend fun update(
            id: String,
            input: EventInput,
        ): AppResult<ClientEvent> = eventCall { AppResult.Success(api.update(id, input.toDto()).toEvent()) }
    }

/** Mismo manejo de errores que el resto de repositorios: cancelación se propaga, lo demás es `AppError`. */
@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> eventCall(block: suspend () -> AppResult<T>): AppResult<T> =
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: Exception) {
        AppResult.Failure(error.toAppError())
    }

internal fun parseDate(value: String?): LocalDate? = value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

private fun EventInput.toDto(): EventRequestDto =
    EventRequestDto(
        name = name,
        eventType = eventType.wire,
        date = date.toString(),
        guests = guests,
        budget = budget.toDouble(),
        hours = hours,
        venueId = venueId,
    )

internal fun EventDto.toEvent(): ClientEvent =
    ClientEvent(
        id = id,
        name = name.orEmpty(),
        eventType = QuoteEventType.fromWire(eventType),
        date = parseDate(date),
        guests = guests ?: 0,
        budget = budget?.let(BigDecimal::valueOf) ?: BigDecimal.ZERO,
        hours = hours ?: 0,
        venueId = venue?.venueId,
        venueName = venue?.name,
    )
