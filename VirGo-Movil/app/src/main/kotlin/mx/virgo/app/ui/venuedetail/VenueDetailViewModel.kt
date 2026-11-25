package mx.virgo.app.ui.venuedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.SessionRole
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetVenueDetailUseCase
import mx.virgo.core.domain.VenueDetail
import javax.inject.Inject

/** Quién mira el detalle: decide si se ve el precio y qué acciones aparecen (006 RF-06–RF-09). */
enum class DetailViewer {
    GUEST,
    CLIENT,
    OTHER_ROLE,
}

/**
 * Acciones del detalle. `onQuote`/`onBook` son `null` mientras `008`/`009` no existan: los
 * botones se muestran deshabilitados con su aviso, sin inventar un flujo.
 */
data class VenueDetailActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSignIn: () -> Unit = {},
    val onQuote: ((String) -> Unit)? = null,
    val onBook: ((String) -> Unit)? = null,
)

data class VenueDetailUiState(
    val venueId: String? = null,
    val loading: Boolean = true,
    val venue: VenueDetail? = null,
    val error: AppError? = null,
    val viewer: DetailViewer = DetailViewer.GUEST,
)

@HiltViewModel
class VenueDetailViewModel
    @Inject
    constructor(
        private val getVenueDetail: GetVenueDetailUseCase,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val mutableState = MutableStateFlow(VenueDetailUiState())
        val state: StateFlow<VenueDetailUiState> = mutableState.asStateFlow()

        /** Carga el salón; repetir con el mismo `id` no vuelve a pedirlo (rotación). */
        fun load(venueId: String) {
            if (mutableState.value.venueId == venueId && mutableState.value.error == null) return
            fetch(venueId)
        }

        fun retry() {
            mutableState.value.venueId?.let(::fetch)
        }

        private fun fetch(venueId: String) {
            mutableState.value = VenueDetailUiState(venueId = venueId, viewer = viewer())
            viewModelScope.launch {
                val result = getVenueDetail(venueId)
                mutableState.value =
                    when (result) {
                        is AppResult.Success -> mutableState.value.copy(loading = false, venue = result.value)
                        is AppResult.Failure -> mutableState.value.copy(loading = false, error = result.error)
                    }
            }
        }

        private fun viewer(): DetailViewer =
            when (
                session.state.value.session
                    ?.role
            ) {
                null -> DetailViewer.GUEST
                SessionRole.CLIENT -> DetailViewer.CLIENT
                else -> DetailViewer.OTHER_ROLE
            }
    }
