package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Evento del cliente tal como lo guarda el servidor (007, 014). */
data class ClientEvent(
    val id: String,
    val name: String,
    val eventType: QuoteEventType?,
    val date: LocalDate?,
    val guests: Int,
    val budget: BigDecimal,
    val hours: Int,
    val venueId: String?,
    val venueName: String?,
) {
    /** Días que faltan; negativo si ya pasó; `null` sin fecha (014 RF-02). */
    fun daysUntil(today: LocalDate): Long? = date?.let { ChronoUnit.DAYS.between(today, it) }
}

/** Datos que se envían al crear o editar; el `PATCH` exige todos (007 RF-04). */
data class EventInput(
    val name: String,
    val eventType: QuoteEventType,
    val date: LocalDate,
    val guests: Int,
    val budget: BigDecimal,
    val hours: Int,
    val venueId: String? = null,
)

enum class EventField {
    NAME,
    TYPE,
    DATE,
    GUESTS,
    BUDGET,
    HOURS,
    ;

    companion object {
        fun fromWire(name: String): EventField? =
            when (name) {
                "name" -> NAME
                "eventType" -> TYPE
                "date" -> DATE
                "guests" -> GUESTS
                "budget" -> BUDGET
                "hours" -> HOURS
                else -> null
            }
    }
}

enum class EventProblem {
    REQUIRED,
    INVALID,
    PAST_DATE,
    TOO_LONG,
}

/** Lo capturado en el formulario, como texto (007 RF-01–RF-03). */
data class EventForm(
    val name: String = "",
    val eventType: QuoteEventType? = null,
    val date: LocalDate? = null,
    val guests: String = "",
    val budget: String = "",
    val hours: String = "",
)

object EventValidator {
    const val MAX_NAME = 100

    /**
     * `originalDate` es la fecha guardada al editar: si no cambia, una fecha pasada no impide
     * corregir otros datos del evento.
     */
    fun validate(
        form: EventForm,
        today: LocalDate,
        venueId: String? = null,
        originalDate: LocalDate? = null,
    ): Pair<EventInput?, Map<EventField, EventProblem>> {
        val errors = mutableMapOf<EventField, EventProblem>()
        val name = form.name.trim()
        when {
            name.isEmpty() -> errors[EventField.NAME] = EventProblem.REQUIRED
            name.length > MAX_NAME -> errors[EventField.NAME] = EventProblem.TOO_LONG
        }
        if (form.eventType == null) errors[EventField.TYPE] = EventProblem.REQUIRED
        when {
            form.date == null -> errors[EventField.DATE] = EventProblem.REQUIRED
            form.date.isBefore(today) && form.date != originalDate -> errors[EventField.DATE] = EventProblem.PAST_DATE
        }
        val guests = positive(form.guests, EventField.GUESTS, errors)
        val hours = positive(form.hours, EventField.HOURS, errors)
        val budget = QuoteAnswerValidator.parseAmount(form.budget)
        when {
            form.budget.isBlank() -> errors[EventField.BUDGET] = EventProblem.REQUIRED
            budget == null || budget.signum() < 0 -> errors[EventField.BUDGET] = EventProblem.INVALID
        }
        if (errors.isNotEmpty()) return null to errors
        val input =
            EventInput(
                name = name,
                eventType = requireNotNull(form.eventType),
                date = requireNotNull(form.date),
                guests = requireNotNull(guests),
                budget = requireNotNull(budget),
                hours = requireNotNull(hours),
                venueId = venueId,
            )
        return input to errors
    }

    private fun positive(
        text: String,
        field: EventField,
        errors: MutableMap<EventField, EventProblem>,
    ): Int? {
        val value = text.trim().toIntOrNull()
        when {
            text.isBlank() -> errors[field] = EventProblem.REQUIRED
            value == null || value < 1 -> errors[field] = EventProblem.INVALID
        }
        return value
    }
}

interface EventsRepository {
    suspend fun list(): AppResult<List<ClientEvent>>

    suspend fun get(id: String): AppResult<ClientEvent>

    suspend fun create(input: EventInput): AppResult<ClientEvent>

    suspend fun update(
        id: String,
        input: EventInput,
    ): AppResult<ClientEvent>
}
