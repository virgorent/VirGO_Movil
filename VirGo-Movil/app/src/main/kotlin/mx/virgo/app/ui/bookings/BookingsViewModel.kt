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

data class BookingsUiState(
    val loading: Boolean = true,
    val bookings: List<Booking> = emptyList(),
    val failed: Boolean = false,
    val receipt: ReceiptState? = null,
)

@HiltViewModel
class BookingsViewModel
    @Inject
    constructor(
        private val getBookings: GetBookingsUseCase,
        private val getBooking: GetBookingUseCase,
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

        fun closeReceipt() {
            mutableState.value = mutableState.value.copy(receipt = null)
        }

        private suspend fun endsSession(error: AppError): Boolean {
            if (error != AppError.Unauthorized) return false
            session.invalidate(SessionEndReason.REVOKED)
            return true
        }
    }
