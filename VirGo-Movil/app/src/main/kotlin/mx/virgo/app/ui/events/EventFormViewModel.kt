package mx.virgo.app.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ClientEvent
import mx.virgo.core.domain.EventField
import mx.virgo.core.domain.EventForm
import mx.virgo.core.domain.EventProblem
import mx.virgo.core.domain.EventValidator
import mx.virgo.core.domain.EventsRepository
import java.time.LocalDate
import javax.inject.Inject

/** Callbacks de C12, agrupados para probar el contenido sin `ViewModel`. */
data class EventFormActions(
    val onForm: (EventForm) -> Unit = {},
    val onSave: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onBack: () -> Unit = {},
)

data class EventFormUiState(
    val eventId: String? = null,
    val loading: Boolean = false,
    /** Evento guardado al editar: conserva salón y fecha original (007 RF-04). */
    val original: ClientEvent? = null,
    val form: EventForm = EventForm(),
    val fieldErrors: Map<EventField, EventProblem> = emptyMap(),
    val serverErrors: Map<EventField, String> = emptyMap(),
    val saving: Boolean = false,
    val error: EventError? = null,
    val saved: ClientEvent? = null,
)

/** Crear o editar un evento (007). */
@HiltViewModel
class EventFormViewModel
    @Inject
    constructor(
        private val repository: EventsRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        /** Reloj inyectable en pruebas. */
        internal var today: () -> LocalDate = { LocalDate.now() }

        private val mutableState = MutableStateFlow(EventFormUiState())
        val state: StateFlow<EventFormUiState> = mutableState.asStateFlow()

        /** Abre el formulario; con el mismo evento conserva lo capturado (rotación). */
        fun start(eventId: String?) {
            val current = mutableState.value
            val loaded = current.original != null || eventId == null
            val sameForm = current.eventId == eventId && current.saved == null
            if (sameForm && loaded) {
                return
            }
            mutableState.value = EventFormUiState(eventId = eventId, loading = eventId != null)
            if (eventId != null) load(eventId)
        }

        fun load(eventId: String) {
            mutableState.value = mutableState.value.copy(loading = true, error = null)
            viewModelScope.launch {
                when (val result = repository.get(eventId)) {
                    is AppResult.Success ->
                        mutableState.value =
                            mutableState.value.copy(
                                loading = false,
                                original = result.value,
                                form = result.value.toForm(),
                            )
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value =
                                mutableState.value.copy(loading = false, error = result.error.toEventError())
                        }
                }
            }
        }

        fun update(form: EventForm) {
            val current = mutableState.value
            val changed = EventField.entries.filter { it.read(form) != it.read(current.form) }.toSet()
            mutableState.value =
                current.copy(
                    form = form,
                    fieldErrors = current.fieldErrors - changed,
                    serverErrors = current.serverErrors - changed,
                    error = null,
                )
        }

        /** Valida y guarda una sola vez (007 RF-05). */
        fun save() {
            val current = mutableState.value
            if (current.saving || current.loading) return
            val (input, errors) =
                EventValidator.validate(current.form, today(), current.original?.venueId, current.original?.date)
            mutableState.value =
                current.copy(fieldErrors = errors, serverErrors = emptyMap(), saving = input != null, error = null)
            if (input == null) return
            viewModelScope.launch {
                val id = current.eventId
                val result = if (id == null) repository.create(input) else repository.update(id, input)
                when (result) {
                    is AppResult.Success ->
                        mutableState.value =
                            mutableState.value.copy(saving = false, saved = result.value)
                    is AppResult.Failure -> fail(result.error)
                }
            }
        }

        private suspend fun fail(error: AppError) {
            val current = mutableState.value.copy(saving = false)
            if (session.endsOn(error)) {
                mutableState.value = current
                return
            }
            val fields =
                (error as? AppError.Validation)
                    ?.fields
                    ?.mapNotNull { (name, messages) ->
                        val field = EventField.fromWire(name)
                        val message = messages.firstOrNull()
                        if (field != null && message != null) field to message else null
                    }?.toMap()
                    .orEmpty()
            mutableState.value =
                current.copy(serverErrors = fields, error = if (fields.isEmpty()) error.toEventError() else null)
        }
    }

private fun EventField.read(form: EventForm): Any? =
    when (this) {
        EventField.NAME -> form.name
        EventField.TYPE -> form.eventType
        EventField.DATE -> form.date
        EventField.GUESTS -> form.guests
        EventField.BUDGET -> form.budget
        EventField.HOURS -> form.hours
    }

private fun ClientEvent.toForm(): EventForm =
    EventForm(
        name = name,
        eventType = eventType,
        date = date,
        guests = guests.toString(),
        budget = budget.stripTrailingZeros().toPlainString(),
        hours = hours.toString(),
    )
