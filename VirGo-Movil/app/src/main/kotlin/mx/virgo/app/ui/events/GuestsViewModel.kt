package mx.virgo.app.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.EventGuest
import mx.virgo.core.domain.EventPlanningRepository
import mx.virgo.core.domain.GuestField
import mx.virgo.core.domain.GuestStatus
import mx.virgo.core.domain.GuestTotals
import mx.virgo.core.domain.GuestValidator
import javax.inject.Inject

/** Callbacks de D32/C31, agrupados para probar el contenido sin `ViewModel`. */
data class GuestsActions(
    val onAdd: () -> Unit = {},
    val onEdit: (EventGuest) -> Unit = {},
    val onCancel: () -> Unit = {},
    val onEditor: (GuestEditor) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: (EventGuest?) -> Unit = {},
    val onConfirmDelete: (EventGuest) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onShare: () -> Unit = {},
    val onBack: () -> Unit = {},
)

/** Invitado en captura; `guestId` nulo es uno nuevo (016 RF-04). */
data class GuestEditor(
    val guestId: String? = null,
    val name: String = "",
    val phone: String = "",
    val companions: String = "0",
    val status: GuestStatus = GuestStatus.PENDING,
    val table: String = "",
    val errors: Set<GuestField> = emptySet(),
) {
    companion object {
        fun of(guest: EventGuest): GuestEditor =
            GuestEditor(
                guest.id,
                guest.name,
                guest.phone,
                guest.companions.toString(),
                guest.status.takeIf { it != GuestStatus.UNKNOWN } ?: GuestStatus.PENDING,
                guest.table.orEmpty(),
            )
    }
}

data class GuestsUiState(
    val eventId: String? = null,
    val loading: Boolean = true,
    val guests: List<EventGuest> = emptyList(),
    val failed: Boolean = false,
    val editor: GuestEditor? = null,
    val saving: Boolean = false,
    val error: EventError? = null,
    /** Invitado que espera confirmación para eliminarse. */
    val deleting: EventGuest? = null,
) {
    val totals: GuestTotals get() = GuestTotals.of(guests)
}

@HiltViewModel
class GuestsViewModel
    @Inject
    constructor(
        private val repository: EventPlanningRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(GuestsUiState())
        val state: StateFlow<GuestsUiState> = mutableState.asStateFlow()

        fun start(eventId: String) {
            if (mutableState.value.eventId == eventId) return
            mutableState.value = GuestsUiState(eventId = eventId)
            refresh()
        }

        fun refresh() {
            val eventId = mutableState.value.eventId ?: return
            mutableState.value = mutableState.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                when (val result = repository.guests(eventId)) {
                    is AppResult.Success ->
                        mutableState.value =
                            mutableState.value.copy(loading = false, guests = result.value)
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value = mutableState.value.copy(loading = false, failed = true)
                        }
                }
            }
        }

        /** Abre el formulario vacío (`null`) o con un invitado; cerrar es `edit(null, close = true)`. */
        fun edit(
            guest: EventGuest?,
            close: Boolean = false,
        ) {
            val editor = if (close) null else guest?.let(GuestEditor::of) ?: GuestEditor()
            mutableState.value = mutableState.value.copy(editor = editor, error = null)
        }

        fun updateEditor(editor: GuestEditor) {
            mutableState.value = mutableState.value.copy(editor = editor.copy(errors = emptySet()), error = null)
        }

        fun save() {
            val current = mutableState.value.takeIf { !it.saving }
            val editor = current?.editor
            val eventId = current?.eventId
            if (editor == null || eventId == null) return
            val (input, errors) =
                GuestValidator.validate(editor.name, editor.phone, editor.companions, editor.status, editor.table)
            mutableState.value = current.copy(editor = editor.copy(errors = errors), saving = input != null)
            if (input == null) return
            viewModelScope.launch {
                when (val result = repository.saveGuest(eventId, editor.guestId, input)) {
                    is AppResult.Success -> {
                        val saved = result.value
                        val others = mutableState.value.guests.filterNot { it.id == saved.id }
                        val guests =
                            if (editor.guestId ==
                                null
                            ) {
                                others + saved
                            } else {
                                mutableState.value.guests.map {
                                    if (it.id ==
                                        saved.id
                                    ) {
                                        saved
                                    } else {
                                        it
                                    }
                                }
                            }
                        mutableState.value = mutableState.value.copy(saving = false, editor = null, guests = guests)
                    }
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value =
                                mutableState.value.copy(saving = false, error = result.error.toEventError())
                        }
                }
            }
        }

        /** Primero pide confirmación (`confirm = false`); después elimina (016 RF-04). */
        fun delete(
            guest: EventGuest?,
            confirm: Boolean = false,
        ) {
            val eventId = mutableState.value.eventId ?: return
            if (!confirm || guest == null) {
                mutableState.value = mutableState.value.copy(deleting = guest)
                return
            }
            mutableState.value = mutableState.value.copy(deleting = null)
            viewModelScope.launch {
                when (val result = repository.deleteGuest(eventId, guest.id)) {
                    is AppResult.Success ->
                        mutableState.value =
                            mutableState.value.copy(guests = mutableState.value.guests.filterNot { it.id == guest.id })
                    is AppResult.Failure ->
                        if (!session.endsOn(result.error)) {
                            mutableState.value = mutableState.value.copy(error = result.error.toEventError())
                        }
                }
            }
        }
    }
