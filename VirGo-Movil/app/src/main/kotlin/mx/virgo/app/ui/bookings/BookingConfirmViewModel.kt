package mx.virgo.app.ui.bookings

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
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.BookingField
import mx.virgo.core.domain.BookingProblem
import mx.virgo.core.domain.BookingRequest
import mx.virgo.core.domain.BookingValidator
import mx.virgo.core.domain.CreateBookingUseCase
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.domain.VenueDetail
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

private const val DRAFT_KEY = "booking_draft"
private val draftJson = Json { ignoreUnknownKeys = true }

/**
 * Lo que sobrevive a la rotación y a la muerte del proceso. `key` es la `Idempotency-Key`
 * del intento: se reutiliza en cada reintento y solo cambia si cambian fecha u horas (009 ADR-02).
 */
@Serializable
data class BookingDraft(
    val venueId: String? = null,
    val quoteId: String? = null,
    val date: String? = null,
    val hours: String = "",
    val key: String = UUID.randomUUID().toString(),
)

/** Callbacks de C8/C23, agrupados para probar el contenido sin `ViewModel`. */
data class BookingConfirmActions(
    val onBack: () -> Unit = {},
    val onDate: (LocalDate) -> Unit = {},
    val onHours: (String) -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onRetryVenue: () -> Unit = {},
    val onMyBookings: () -> Unit = {},
    val onCatalog: () -> Unit = {},
)

enum class BookingError {
    NETWORK,
    NOT_FOUND,
    FORBIDDEN,
    SERVER,
}

data class BookingConfirmUiState(
    val draft: BookingDraft = BookingDraft(),
    val venue: VenueDetail? = null,
    val loadingVenue: Boolean = true,
    val venueFailed: Boolean = false,
    val fieldErrors: Map<BookingField, BookingProblem> = emptyMap(),
    /** El servidor respondió 400 sin detalle por campo: se muestra junto a las horas (009 CA-05). */
    val rejected: Boolean = false,
    val sending: Boolean = false,
    val error: BookingError? = null,
    val booking: Booking? = null,
) {
    val date: LocalDate? get() = draft.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

@HiltViewModel
class BookingConfirmViewModel
    @Inject
    constructor(
        private val savedState: SavedStateHandle,
        private val getVenue: GetVenueDetailUseCase,
        private val createBooking: CreateBookingUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        /** Reloj inyectable en pruebas. */
        internal var today: () -> LocalDate = { LocalDate.now() }

        private val mutableState =
            MutableStateFlow(
                BookingConfirmUiState(
                    draft =
                        savedState.get<String>(DRAFT_KEY)?.let {
                            runCatching { draftJson.decodeFromString<BookingDraft>(it) }.getOrNull()
                        } ?: BookingDraft(),
                ),
            )
        val state: StateFlow<BookingConfirmUiState> = mutableState.asStateFlow()

        /** Abre la confirmación para un salón; con el mismo salón no reinicia (rotación). */
        fun start(
            venueId: String,
            quoteId: String? = null,
            date: LocalDate? = null,
        ) {
            val current = mutableState.value
            // Un comprobante anterior no se reabre: reservar de nuevo es un intento nuevo.
            if (current.draft.venueId != venueId || current.draft.quoteId != quoteId || current.booking != null) {
                emit(BookingConfirmUiState(draft = BookingDraft(venueId, quoteId, date?.toString())))
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
                            emit(mutableState.value.copy(loadingVenue = false, error = BookingError.NOT_FOUND))
                        } else {
                            emit(mutableState.value.copy(loadingVenue = false, venueFailed = true))
                        }
                }
            }
        }

        fun setDate(date: LocalDate) = change(mutableState.value.draft.copy(date = date.toString()), BookingField.DATE)

        fun setHours(hours: String) =
            change(
                mutableState.value.draft.copy(hours = hours.filter(Char::isDigit).take(MAX_HOURS_DIGITS)),
                BookingField.HOURS,
            )

        /** Valida y envía; un reintento usa la misma clave y nunca crea dos reservaciones. */
        fun confirm() {
            val current = mutableState.value
            val request = current.toRequest().takeIf { !current.sending && current.booking == null } ?: return
            val errors = BookingValidator.validate(request, today(), current.venue?.minimumHours)
            emit(current.copy(sending = errors.isEmpty(), fieldErrors = errors, rejected = false, error = null))
            if (errors.isNotEmpty()) return
            viewModelScope.launch {
                when (val result = createBooking(request, current.draft.key)) {
                    is AppResult.Success -> emit(mutableState.value.copy(sending = false, booking = result.value))
                    is AppResult.Failure -> fail(result.error)
                }
            }
        }

        /** Cambiar fecha u horas es un intento nuevo: nueva clave. */
        private fun change(
            draft: BookingDraft,
            field: BookingField,
        ) {
            val current = mutableState.value
            if (draft == current.draft) return
            emit(
                current.copy(
                    draft = draft.copy(key = UUID.randomUUID().toString()),
                    fieldErrors = current.fieldErrors - field,
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
                is AppError.Validation -> emit(current.copy(rejected = true))
                AppError.Network, AppError.Timeout -> emit(current.copy(error = BookingError.NETWORK))
                AppError.NotFound -> emit(current.copy(error = BookingError.NOT_FOUND))
                AppError.Forbidden -> emit(current.copy(error = BookingError.FORBIDDEN))
                else -> emit(current.copy(error = BookingError.SERVER))
            }
        }

        private fun emit(state: BookingConfirmUiState) {
            mutableState.value = state
            savedState[DRAFT_KEY] = draftJson.encodeToString(BookingDraft.serializer(), state.draft)
        }
    }

private const val MAX_HOURS_DIGITS = 3

private fun BookingConfirmUiState.toRequest(): BookingRequest? =
    draft.venueId?.let { BookingRequest(it, date, draft.hours.toIntOrNull(), draft.quoteId) }
