package mx.virgo.app.ui.ownervenues

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
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.OwnerVenue

/** Resultado del asistente que «Mis salones» confirma al volver. */
enum class VenueWizardOutcome {
    SAVED,
    PUBLISHED,
}

data class OwnerVenuesUiState(
    val loading: Boolean = true,
    val venues: List<OwnerVenue> = emptyList(),
    val error: AppError? = null,
    val outcome: VenueWizardOutcome? = null,
)

@HiltViewModel
class OwnerVenuesViewModel
    @javax.inject.Inject
    constructor(
        private val getMyVenues: GetMyVenuesUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(OwnerVenuesUiState())
        val state: StateFlow<OwnerVenuesUiState> = mutableState.asStateFlow()

        init {
            refresh()
        }

        /** Recarga la lista; `outcome` se muestra una vez arriba de la lista. */
        fun refresh(outcome: VenueWizardOutcome? = null) {
            mutableState.value = mutableState.value.copy(loading = true, error = null, outcome = outcome)
            viewModelScope.launch {
                when (val result = getMyVenues()) {
                    is AppResult.Success ->
                        mutableState.value = mutableState.value.copy(loading = false, venues = result.value)
                    is AppResult.Failure -> reject(result.error)
                }
            }
        }

        private suspend fun reject(error: AppError) {
            if (error == AppError.Unauthorized) {
                session.invalidate(SessionEndReason.REVOKED)
            } else {
                mutableState.value = mutableState.value.copy(loading = false, error = error)
            }
        }
    }
