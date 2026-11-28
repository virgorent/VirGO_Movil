package mx.virgo.app.ui.bookings

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
import mx.virgo.core.domain.Booking
import mx.virgo.core.domain.BookingFolio
import mx.virgo.core.domain.FindBookingByFolioUseCase
import mx.virgo.core.domain.GetBookingUseCase
import mx.virgo.core.domain.GetBookingsUseCase
import javax.inject.Inject

/** Comprobante abierto desde la lista: se lee de nuevo del servidor (009 RF-09). */
data class ReceiptState(
    val bookingId: String,
    val loading: Boolean = true,
    val booking: Booking? = null,
    val failed: Boolean = false,
)

/** Por qué no se abrió un folio (`010` RF-02 y RF-04). */
enum class FolioError {
    FORMAT,
    NOT_FOUND,
    FAILED,
}

data class BookingsUiState(
    val loading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val failed: Boolean = false,
    val receipt: ReceiptState? = null,
    val folioQuery: String = "",
    val searching: Boolean = false,
    val folioError: FolioError? = null,
)

@HiltViewModel
class BookingsViewModel
    @Inject
    constructor(
        private val getBookings: GetBookingsUseCase,
        private val getBooking: GetBookingUseCase,
        private val findByFolio: FindBookingByFolioUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(BookingsUiState())
        val state: StateFlow<BookingsUiState> = mutableState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            mutableState.value = mutableState.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                when (val result = getBookings()) {
                    is AppResult.Success ->
                        mutableState.value = mutableState.value.copy(loading = false, bookings = result.value)
                    is AppResult.Failure ->
                        if (!endsSession(result.error)) {
                            mutableState.value = mutableState.value.copy(loading = false, failed = true)
                        }
                }
            }
        }

        fun open(bookingId: String) {
            mutableState.value = mutableState.value.copy(receipt = ReceiptState(bookingId))
            viewModelScope.launch {
                val receipt =
                    when (val result = getBooking(bookingId)) {
                        is AppResult.Success -> ReceiptState(bookingId, loading = false, booking = result.value)
                        is AppResult.Failure ->
                            if (endsSession(
                                    result.error,
                                )
                            ) {
                                return@launch
                            } else {
                                ReceiptState(bookingId, false, failed = true)
                            }
                    }
                if (mutableState.value.receipt?.bookingId == bookingId) {
                    mutableState.value = mutableState.value.copy(receipt = receipt)
                }
            }
        }

        /** `010` RF-05: borrar el campo quita el error y deja la lista como estaba. */
        fun onFolioChange(value: String) {
            mutableState.value = mutableState.value.copy(folioQuery = value, folioError = null)
        }

        /** `010` RF-02–RF-05: valida antes de llamar; un 200 abre el mismo comprobante que la lista. */
        fun searchFolio() {
            val current = mutableState.value
            if (current.searching) return
            val folio = BookingFolio.normalize(current.folioQuery)
            if (folio == null) {
                mutableState.value = current.copy(folioError = FolioError.FORMAT)
                return
            }
            mutableState.value = current.copy(searching = true, folioError = null)
            viewModelScope.launch {
                when (val result = findByFolio(folio)) {
                    is AppResult.Success -> {
                        val booking = result.value
                        mutableState.value =
                            mutableState.value.copy(
                                searching = false,
                                receipt = ReceiptState(booking.id, loading = false, booking = booking),
                            )
                    }
                    is AppResult.Failure -> onFolioFailure(result.error)
                }
            }
        }

        private suspend fun onFolioFailure(error: AppError) {
            if (endsSession(error)) return
            val reason = if (error == AppError.NotFound) FolioError.NOT_FOUND else FolioError.FAILED
            mutableState.value = mutableState.value.copy(searching = false, folioError = reason)
        }

        fun closeReceipt() {
            mutableState.value = mutableState.value.copy(receipt = null)
        }

        private suspend fun endsSession(error: AppError): Boolean {
            if (error != AppError.Unauthorized) return false
            session.invalidate(SessionEndReason.REVOKED)
            return true
        }
    }
