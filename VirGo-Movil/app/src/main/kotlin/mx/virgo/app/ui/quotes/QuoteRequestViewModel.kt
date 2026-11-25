package mx.virgo.app.ui.quotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.CreateQuoteUseCase
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.QuoteField
import mx.virgo.core.domain.QuoteProblem
import mx.virgo.core.domain.QuoteRequest
import mx.virgo.core.domain.QuoteValidator
import mx.virgo.core.domain.VenueDetail
import java.time.LocalDate
import javax.inject.Inject

private const val DRAFT_KEY = "quote_draft"
private const val MAX_GUEST_DIGITS = 5
private val draftJson = Json { ignoreUnknownKeys = true }

/**
 * Lo capturado sobrevive a la rotación y a la muerte del proceso (008 RF-06). `uncertain`
 * marca que el último envío quedó sin respuesta: el reintento revisa antes de reenviar.
 */
@Serializable
data class QuoteDraft(
    val venueId: String? = null,
    val eventType: String? = null,
    val date: String? = null,
    val guests: String = "",
    val message: String = "",
    val uncertain: Boolean = false,
)

/** Callbacks de C21, agrupados para probar el contenido sin `ViewModel`. */
data class QuoteRequestActions(
    val onBack: () -> Unit = {},
    val onEventType: (QuoteEventType) -> Unit = {},
    val onDate: (LocalDate) -> Unit = {},
    val onGuests: (String) -> Unit = {},
    val onMessage: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onRetryVenue: () -> Unit = {},
    val onMyQuotes: () -> Unit = {},
)

enum class QuoteError {
    NETWORK,
    NOT_FOUND,
    FORBIDDEN,
    SERVER,
}

data class QuoteRequestUiState(
    val draft: QuoteDraft = QuoteDraft(),
    val venue: VenueDetail? = null,
    val loadingVenue: Boolean = true,
    val venueFailed: Boolean = false,
    val fieldErrors: Map<QuoteField, QuoteProblem> = emptyMap(),
    /** Mensajes en español del Problem Details del servidor, por campo (008 RF-06). */
    val serverErrors: Map<QuoteField, String> = emptyMap(),
    /** `400` sin detalle de campo que la app conozca. */
    val rejected: Boolean = false,
    val sending: Boolean = false,
    val error: QuoteError? = null,
    val sent: Quote? = null,
) {
    val eventType: QuoteEventType? get() = QuoteEventType.fromWire(draft.eventType)
    val date: LocalDate? get() = draft.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val capacityWarning: Boolean get() = QuoteValidator.exceedsCapacity(draft.guests.toIntOrNull(), venue?.capacity)
}

@HiltViewModel
class QuoteRequestViewModel
    @Inject
    constructor(
        private val savedState: SavedStateHandle,
        private val getVenue: GetVenueDetailUseCase,
        private val createQuote: CreateQuoteUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        /** Reloj inyectable en pruebas. */
        internal var today: () -> LocalDate = { LocalDate.now() }

        private val mutableState =
            MutableStateFlow(
                QuoteRequestUiState(
                    draft =
                        savedState.get<String>(DRAFT_KEY)?.let {
                            runCatching { draftJson.decodeFromString<QuoteDraft>(it) }.getOrNull()
                        } ?: QuoteDraft(),
                ),
            )
        val state: StateFlow<QuoteRequestUiState> = mutableState.asStateFlow()

        /** Abre la solicitud de un salón; con el mismo salón conserva lo capturado (rotación). */
        fun start(venueId: String) {
            val current = mutableState.value
            if (current.draft.venueId != venueId || current.sent != null) {
                emit(QuoteRequestUiState(draft = QuoteDraft(venueId)))
            }
            if (mutableState.value.venue == null) loadVenue()
        }

        fun loadVenue() {
            val venueId = mutableState.value.draft.venueId ?: return
            emit(mutableState.value.copy(loadingVenue = true, venueFailed = false))
            viewModelScope.launch {
                when (val result = getVenue(venueId)) {
                    is AppResult.Success -> emit(mutableState.value.copy(loadingVenue = false, venue = result.value))
                    is AppResult.Failure ->
                        if (result.error == AppError.NotFound) {
                            emit(mutableState.value.copy(loadingVenue = false, error = QuoteError.NOT_FOUND))
                        } else {
                            emit(mutableState.value.copy(loadingVenue = false, venueFailed = true))
                        }
                }
            }
        }

        fun setEventType(type: QuoteEventType) =
            change(mutableState.value.draft.copy(eventType = type.wire), QuoteField.EVENT_TYPE)

        fun setDate(date: LocalDate) = change(mutableState.value.draft.copy(date = date.toString()), QuoteField.DATE)

        fun setGuests(guests: String) =
            change(
                mutableState.value.draft.copy(guests = guests.filter(Char::isDigit).take(MAX_GUEST_DIGITS)),
                QuoteField.GUESTS,
            )

        fun setMessage(message: String) = change(mutableState.value.draft.copy(message = message), QuoteField.MESSAGE)

        /** Valida y envía una sola vez; el botón queda deshabilitado mientras envía (008 RF-05). */
        fun send() {
            val current = mutableState.value
            val request = current.toRequest().takeIf { !current.sending && current.sent == null } ?: return
            val errors = QuoteValidator.validate(request, today())
            emit(
                current.copy(
                    sending = errors.isEmpty(),
                    fieldErrors = errors,
                    serverErrors = emptyMap(),
                    rejected = false,
                    error = null,
                ),
            )
            if (errors.isNotEmpty()) return
            viewModelScope.launch {
                when (val result = createQuote(request, afterUncertainFailure = current.draft.uncertain)) {
                    is AppResult.Success ->
                        emit(
                            mutableState.value.copy(
                                sending = false,
                                sent = result.value,
                                draft = mutableState.value.draft.copy(uncertain = false),
                            ),
                        )
                    is AppResult.Failure -> fail(result.error)
                }
            }
        }

        private fun change(
            draft: QuoteDraft,
            field: QuoteField,
        ) {
            val current = mutableState.value
            if (draft == current.draft) return
            emit(
                current.copy(
                    draft = draft,
                    fieldErrors = current.fieldErrors - field,
                    serverErrors = current.serverErrors - field,
                    rejected = false,
                    error = null,
                ),
            )
        }

        private suspend fun fail(error: AppError) {
            val current = mutableState.value.copy(sending = false)
            when (error) {
                AppError.Unauthorized -> {
                    emit(current)
                    session.invalidate(SessionEndReason.REVOKED)
                }
                is AppError.Validation -> emit(current.withServerErrors(error.fields))
                AppError.Network, AppError.Timeout ->
                    emit(current.copy(error = QuoteError.NETWORK, draft = current.draft.copy(uncertain = true)))
                AppError.NotFound -> emit(current.copy(error = QuoteError.NOT_FOUND))
                AppError.Forbidden -> emit(current.copy(error = QuoteError.FORBIDDEN))
                else -> emit(current.copy(error = QuoteError.SERVER))
            }
        }

        private fun emit(state: QuoteRequestUiState) {
            mutableState.value = state
            savedState[DRAFT_KEY] = draftJson.encodeToString(QuoteDraft.serializer(), state.draft)
        }
    }

private fun QuoteRequestUiState.toRequest(): QuoteRequest? =
    draft.venueId?.let { QuoteRequest(it, eventType, date, draft.guests.toIntOrNull(), draft.message) }

/** Errores de campo conocidos junto a su campo; si no hay ninguno, aviso general. */
private fun QuoteRequestUiState.withServerErrors(fields: Map<String, List<String>>): QuoteRequestUiState {
    val known =
        fields
            .mapNotNull { (name, messages) ->
                val field = QuoteField.fromWire(name)
                val message = messages.firstOrNull()
                if (field != null && message != null) field to message else null
            }.toMap()
    return copy(serverErrors = known, rejected = known.isEmpty())
}
