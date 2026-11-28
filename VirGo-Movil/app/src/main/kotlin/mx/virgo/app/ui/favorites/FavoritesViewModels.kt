package mx.virgo.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.session.endsSession
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Favorite
import mx.virgo.core.domain.FavoritesRepository
import javax.inject.Inject

enum class FavoritesStatus {
    LOADING,
    READY,
    UNAVAILABLE,
    FAILED,
}

data class FavoritesUiState(
    val status: FavoritesStatus = FavoritesStatus.LOADING,
    val favorites: List<Favorite> = emptyList(),
    val removing: String? = null,
    val removeFailed: Boolean = false,
)

private const val VENUE = "venue"

/** «Favoritos» (`021` v2.1, contrato §7): sin endpoint (`404`) dice «pronto». */
@HiltViewModel
class FavoritesViewModel
    @Inject
    constructor(
        private val repository: FavoritesRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(FavoritesUiState())
        val state: StateFlow<FavoritesUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(status = FavoritesStatus.LOADING)
            viewModelScope.launch {
                when (val result = repository.getFavorites()) {
                    is AppResult.Success ->
                        ui.value =
                            ui.value.copy(
                                status = FavoritesStatus.READY,
                                favorites =
                                    result.value.filter {
                                        it.targetType ==
                                            VENUE
                                    },
                            )
                    is AppResult.Failure -> {
                        if (session.endsSession(result.error)) return@launch
                        val status =
                            if (result.error ==
                                AppError.NotFound
                            ) {
                                FavoritesStatus.UNAVAILABLE
                            } else {
                                FavoritesStatus.FAILED
                            }
                        ui.value = ui.value.copy(status = status)
                    }
                }
            }
        }

        fun remove(targetId: String) {
            if (ui.value.removing != null) return
            ui.value = ui.value.copy(removing = targetId, removeFailed = false)
            viewModelScope.launch {
                when (val result = repository.removeFavorite(VENUE, targetId)) {
                    is AppResult.Success ->
                        ui.value =
                            ui.value.copy(
                                removing = null,
                                favorites =
                                    ui.value.favorites.filterNot {
                                        it.targetId ==
                                            targetId
                                    },
                            )
                    is AppResult.Failure ->
                        if (!session.endsSession(result.error)) {
                            ui.value =
                                ui.value.copy(removing = null, removeFailed = true)
                        }
                }
            }
        }
    }

/** Corazón del detalle: `null` = aún no se sabe o el backend no tiene favoritos (no se dibuja). */
data class HeartState(
    val favorite: Boolean? = null,
    val busy: Boolean = false,
    val failed: Boolean = false,
)

@HiltViewModel
class FavoriteHeartViewModel
    @Inject
    constructor(
        private val repository: FavoritesRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(HeartState())
        val state: StateFlow<HeartState> = ui.asStateFlow()
        private var venueId: String? = null

        fun load(venueId: String) {
            if (this.venueId == venueId) return
            this.venueId = venueId
            viewModelScope.launch {
                val result = repository.getFavorites()
                if (result is AppResult.Success) {
                    ui.value =
                        HeartState(favorite = result.value.any { it.targetType == VENUE && it.targetId == venueId })
                }
            }
        }

        fun toggle(name: String) {
            val current = ui.value
            val id = venueId
            val favorite = current.favorite
            if (id == null || favorite == null || current.busy) return
            ui.value = HeartState(favorite = !favorite, busy = true)
            viewModelScope.launch {
                val result =
                    if (favorite) {
                        repository.removeFavorite(VENUE, id)
                    } else {
                        repository.addFavorite(VENUE, id, name, null, null)
                    }
                ui.value =
                    when (result) {
                        is AppResult.Success -> ui.value.copy(busy = false)
                        is AppResult.Failure ->
                            if (session.endsSession(
                                    result.error,
                                )
                            ) {
                                return@launch
                            } else {
                                HeartState(favorite = favorite, failed = true)
                            }
                    }
            }
        }
    }
