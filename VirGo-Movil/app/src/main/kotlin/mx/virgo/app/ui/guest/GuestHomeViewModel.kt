package mx.virgo.app.ui.guest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetVenuesUseCase
import javax.inject.Inject

internal const val NO_VENUE = -1
internal const val DEFAULT_RANGE_KM = 5

data class GuestUiState(
    val radiusKm: Int = DEFAULT_RANGE_KM,
    val selectedVenueId: Int = NO_VENUE,
    val nearby: List<VenueSample> = emptyList(),
    val selectedVenue: VenueSample? = null,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
)

@HiltViewModel
class GuestHomeViewModel
    @Inject
    constructor(
        private val getVenues: GetVenuesUseCase,
    ) : ViewModel() {
        private var catalog: List<VenueSample> = VenueSamples

        private val _state = MutableStateFlow(guestState(DEFAULT_RANGE_KM, NO_VENUE, catalog))
        val state: StateFlow<GuestUiState> = _state.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            _state.value = _state.value.copy(isLoading = true, hasError = false)
            viewModelScope.launch {
                when (val result = getVenues()) {
                    is AppResult.Success -> {
                        catalog = if (result.value.isEmpty()) VenueSamples else result.value.toSamples()
                        _state.value = guestState(_state.value.radiusKm, _state.value.selectedVenueId, catalog)
                    }
                    is AppResult.Failure -> {
                        _state.value = _state.value.copy(isLoading = false, hasError = true)
                    }
                }
            }
        }

        fun selectRadius(radiusKm: Int) = apply(radiusKm, _state.value.selectedVenueId)

        fun selectVenue(venueId: Int) = apply(_state.value.radiusKm, venueId)

        fun clearVenue() = apply(_state.value.radiusKm, NO_VENUE)

        private fun apply(
            radiusKm: Int,
            selectedVenueId: Int,
        ) {
            val current = _state.value
            _state.value = guestState(radiusKm, selectedVenueId, catalog, current.isLoading, current.hasError)
        }
    }

private fun guestState(
    radiusKm: Int,
    selectedVenueId: Int,
    catalog: List<VenueSample>,
    isLoading: Boolean = false,
    hasError: Boolean = false,
): GuestUiState {
    val nearby = catalog.filter { it.distanceKm <= radiusKm }.sortedBy { it.distanceKm }
    val selected = nearby.firstOrNull { it.id == selectedVenueId }
    return GuestUiState(
        radiusKm = radiusKm,
        selectedVenueId = selected?.id ?: NO_VENUE,
        nearby = nearby,
        selectedVenue = selected,
        isLoading = isLoading,
        hasError = hasError,
    )
}
