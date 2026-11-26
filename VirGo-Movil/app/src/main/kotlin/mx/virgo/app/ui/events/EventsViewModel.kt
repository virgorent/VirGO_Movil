package mx.virgo.app.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ClientEvent
import mx.virgo.core.domain.EventsRepository
import javax.inject.Inject

/** Errores que muestran las pantallas del evento. */
enum class EventError {
    NETWORK,
    NOT_FOUND,
    REJECTED,
    SERVER,
}

internal fun AppError.toEventError(): EventError =
    when (this) {
        AppError.Network, AppError.Timeout -> EventError.NETWORK
        AppError.NotFound -> EventError.NOT_FOUND
        is AppError.Validation -> EventError.REJECTED
        else -> EventError.SERVER
    }

/** Cierra la sesión ante un `401` (030); devuelve si lo hizo. */
internal suspend fun SessionRepository.endsOn(error: AppError): Boolean {
    if (error != AppError.Unauthorized) return false
    invalidate(SessionEndReason.REVOKED)
    return true
}

/** Paso abierto dentro de «Mi evento». */
enum class EventStep {
    CREATE,
    EDIT,
    GUESTS,
    TASKS,
}

data class EventDetailActions(
    val onEdit: () -> Unit = {},
    val onGuests: () -> Unit = {},
    val onTasks: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onBack: () -> Unit = {},
)

data class EventDetailState(
    val eventId: String,
    val loading: Boolean = true,
    val event: ClientEvent? = null,
    val error: EventError? = null,
)

data class EventsUiState(
    val loading: Boolean = true,
    val events: List<ClientEvent> = emptyList(),
    val failed: Boolean = false,
    val detail: EventDetailState? = null,
)

/** Lista de eventos y detalle abierto (014). */
@HiltViewModel
class EventsViewModel
    @Inject
    constructor(
        private val repository: EventsRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(EventsUiState())
        val state: StateFlow<EventsUiState> = mutableState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            mutableState.value = mutableState.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                when (val result = repository.list()) {
                    is AppResult.Success ->
                        mutableState.value = mutableState.value.copy(loading = false, events = result.value)
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value = mutableState.value.copy(loading = false, failed = true)
                        }
                }
            }
        }

        fun open(eventId: String) {
            mutableState.value = mutableState.value.copy(detail = EventDetailState(eventId))
            viewModelScope.launch {
                val result = repository.get(eventId)
                if (result is AppResult.Failure && session.endsOn(result.error)) return@launch
                val detail =
                    when (result) {
                        is AppResult.Success -> EventDetailState(eventId, loading = false, event = result.value)
                        is AppResult.Failure ->
                            EventDetailState(
                                eventId,
                                loading = false,
                                error = result.error.toEventError(),
                            )
                    }
                if (mutableState.value.detail?.eventId == eventId) {
                    mutableState.value = mutableState.value.copy(detail = detail)
                }
            }
        }

        fun closeDetail() {
            mutableState.value = mutableState.value.copy(detail = null)
        }
    }
