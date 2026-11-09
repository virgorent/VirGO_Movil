package mx.virgo.app.ui.guest

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

internal const val NO_VENUE = -1
internal const val DEFAULT_RANGE_KM = 5

data class GuestUiState(
    val radiusKm: Int = DEFAULT_RANGE_KM,
    val selectedVenueId: Int = NO_VENUE,
    val nearby: List<VenueSample> = emptyList(),
    val selectedVenue: VenueSample? = null,
)

@HiltViewModel
class GuestHomeViewModel
    @Inject
    constructor() : ViewModel() {
        private val _state = MutableStateFlow(guestState(DEFAULT_RANGE_KM, NO_VENUE))
        val state: StateFlow<GuestUiState> = _state.asStateFlow()

        fun selectRadius(radiusKm: Int) = apply(radiusKm, _state.value.selectedVenueId)

        fun selectVenue(venueId: Int) = apply(_state.value.radiusKm, venueId)

        fun clearVenue() = apply(_state.value.radiusKm, NO_VENUE)

        private fun apply(
            radiusKm: Int,
            selectedVenueId: Int,
        ) {
            _state.value = guestState(radiusKm, selectedVenueId)
        }
    }

private fun guestState(
    radiusKm: Int,
    selectedVenueId: Int,
): GuestUiState {
    val nearby = VenueSamples.filter { it.distanceKm <= radiusKm }.sortedBy { it.distanceKm }
    return GuestUiState(
        radiusKm = radiusKm,
        selectedVenueId = selectedVenueId,
        nearby = nearby,
        selectedVenue = nearby.firstOrNull { it.id == selectedVenueId },
    )
}
