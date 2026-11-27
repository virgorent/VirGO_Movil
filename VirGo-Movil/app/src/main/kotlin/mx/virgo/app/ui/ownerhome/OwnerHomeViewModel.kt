package mx.virgo.app.ui.ownerhome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionEndReason
import mx.virgo.app.session.SessionRepository
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetBookingsUseCase
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.GetQuotesUseCase
import mx.virgo.core.domain.OwnerDashboard
import java.time.LocalDate
import javax.inject.Inject

data class OwnerHomeUiState(
    val loading: Boolean = true,
    val dashboard: OwnerDashboard? = null,
    val failed: Boolean = false,
)

/** «Inicio» del propietario (`023` RF-01–RF-04): salones, solicitudes y reservaciones reales. */
@HiltViewModel
class OwnerHomeViewModel
    @Inject
    constructor(
        private val getVenues: GetMyVenuesUseCase,
        private val getQuotes: GetQuotesUseCase,
        private val getBookings: GetBookingsUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        internal var today: () -> LocalDate = { LocalDate.now() }

        private val mutableState = MutableStateFlow(OwnerHomeUiState())
        val state: StateFlow<OwnerHomeUiState> = mutableState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            mutableState.value = mutableState.value.copy(loading = true, failed = false)
            viewModelScope.launch {
                val venues = async { getVenues() }
                val quotes = async { getQuotes() }
                val bookings = async { getBookings() }
                val results = listOf(venues.await(), quotes.await(), bookings.await())
                val failure = results.filterIsInstance<AppResult.Failure>().firstOrNull()
                if (failure != null) {
                    onFailure(failure.error)
                    return@launch
                }
                val dashboard =
                    OwnerDashboard.from(
                        venues = (venues.await() as AppResult.Success).value,
                        quotes = (quotes.await() as AppResult.Success).value,
                        bookings = (bookings.await() as AppResult.Success).value,
                        today = today(),
                    )
                mutableState.value = OwnerHomeUiState(loading = false, dashboard = dashboard)
            }
        }

        private suspend fun onFailure(error: AppError) {
            if (error == AppError.Unauthorized) {
                session.invalidate(SessionEndReason.REVOKED)
            } else {
                mutableState.value = mutableState.value.copy(loading = false, failed = true)
            }
        }
    }
