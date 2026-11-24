package mx.virgo.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.ui.guest.SAMPLE_VENUE_ID_PREFIX
import mx.virgo.app.ui.guest.VenueSample
import mx.virgo.app.ui.guest.VenueSamples
import mx.virgo.app.ui.guest.toSamples
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetFavoritesUseCase
import mx.virgo.core.domain.GetNoticesUseCase
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.ToggleFavoriteUseCase
import mx.virgo.core.domain.Venue
import javax.inject.Inject

/**
 * Pantalla principal del Cliente (spec `005`): mapa y lista de salones (B8/C9),
 * filtros (C10) y recomendaciones (C11), más favoritos y aviso de notificaciones.
 */
@HiltViewModel
class ExploreViewModel
    @Inject
    constructor(
        private val getVenues: GetVenuesUseCase,
        private val getFavorites: GetFavoritesUseCase,
        private val toggleFavorite: ToggleFavoriteUseCase,
        private val getNotices: GetNoticesUseCase,
    ) : ViewModel() {
        private var catalog: List<VenueSample> = VenueSamples
        private var venuesById: Map<String, Venue> = emptyMap()

        private val _state = MutableStateFlow(ExploreUiState())
        val state: StateFlow<ExploreUiState> = _state.asStateFlow()

        private val _events = MutableSharedFlow<ExploreUiEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<ExploreUiEvent> = _events.asSharedFlow()

        init {
            refresh()
        }

        fun refresh() {
            _state.value = _state.value.copy(isLoading = true, hasError = false)
            viewModelScope.launch {
                when (val result = getVenues()) {
                    is AppResult.Success -> {
                        val venues = result.value
                        if (venues.isEmpty()) {
                            catalog = VenueSamples
                            venuesById = emptyMap()
                        } else {
                            venuesById = venues.associateBy { it.id }
                            catalog = venues.toSamples()
                        }
                        // Favoritos y avisos: si aún no responden, se quedan vacíos (Art. VIII.6).
                        val favorites = getFavorites()
                        if (favorites is AppResult.Success) {
                            val ids = favorites.value.map { it.targetId }.toSet()
                            _state.value = _state.value.copy(favoriteIds = ids)
                        }
                        val notices = getNotices()
                        if (notices is AppResult.Success) {
                            val unread = notices.value.count { !it.isRead }
                            _state.value = _state.value.copy(unreadNotices = unread)
                        }
                        _state.value = _state.value.copy(isLoading = false, hasError = false)
                        recompute()
                    }
                    is AppResult.Failure -> {
                        _state.value = _state.value.copy(isLoading = false, hasError = true)
                    }
                }
            }
        }

        fun onFiltersChange(transform: ExploreFilters.() -> ExploreFilters) {
            _state.value =
                _state.value.copy(
                    filters = _state.value.filters.transform(),
                    fieldError = ExploreFieldError.NONE,
                )
            recompute()
        }

        fun onFiltersApply() {
            val error = validateExploreFilters(_state.value.filters)
            if (error != ExploreFieldError.NONE) {
                _state.value = _state.value.copy(fieldError = error)
                return
            }
            _state.value = _state.value.copy(fieldError = ExploreFieldError.NONE, sheet = ExploreSheet.NONE)
        }

        fun onFiltersClear() {
            _state.value = _state.value.copy(filters = ExploreFilters(), fieldError = ExploreFieldError.NONE)
            recompute()
        }

        fun onModeChange(mode: ExploreMode) {
            _state.value = _state.value.copy(mode = mode)
        }

        fun onSheetChange(sheet: ExploreSheet) {
            _state.value = _state.value.copy(sheet = sheet)
        }

        fun onEventTypeChange(eventType: ExploreEventType) {
            _state.value = _state.value.copy(eventType = eventType)
            recompute()
        }

        fun onVenueSelect(id: Int?) {
            val target = id ?: EXPLORE_NO_VENUE
            val selected = _state.value.results.firstOrNull { it.id == target }
            _state.value =
                _state.value.copy(
                    selectedVenueId = selected?.id ?: EXPLORE_NO_VENUE,
                    selectedVenue = selected,
                )
        }

        /** RF-08: alterna el favorito con actualización optimista. */
        fun onFavoriteToggle(venue: VenueSample) {
            val current = _state.value
            val wasFavorite = venue.venueId in current.favoriteIds
            val nextIds =
                if (wasFavorite) {
                    current.favoriteIds - venue.venueId
                } else {
                    current.favoriteIds + venue.venueId
                }
            _state.value = current.copy(favoriteIds = nextIds)
            if (venue.venueId.startsWith(SAMPLE_VENUE_ID_PREFIX)) {
                // La muestra local no existe en el backend: el favorito no viaja a la API.
                return
            }
            val domain = venuesById[venue.venueId] ?: return
            viewModelScope.launch {
                when (toggleFavorite(domain, wasFavorite)) {
                    is AppResult.Success -> Unit
                    is AppResult.Failure -> {
                        val ids = _state.value.favoriteIds
                        val restored = if (wasFavorite) ids + venue.venueId else ids - venue.venueId
                        _state.value = _state.value.copy(favoriteIds = restored)
                        _events.tryEmit(ExploreUiEvent.FavoriteError)
                    }
                }
            }
        }

        private fun recompute() {
            val current = _state.value
            val results =
                sortExploreResults(
                    applyExploreFilters(catalog, current.filters),
                    current.filters.order,
                )
            val selected = results.firstOrNull { it.id == current.selectedVenueId }
            _state.value =
                current.copy(
                    results = results,
                    selectedVenueId = selected?.id ?: EXPLORE_NO_VENUE,
                    selectedVenue = selected,
                    recommendations =
                        buildExploreRecommendations(results, current.filters, current.eventType),
                )
        }
    }
