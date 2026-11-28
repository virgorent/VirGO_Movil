package mx.virgo.app.ui.providers

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
import mx.virgo.core.domain.Provider
import mx.virgo.core.domain.ProviderCategory
import mx.virgo.core.domain.ProvidersRepository
import mx.virgo.core.domain.VenueReviews
import javax.inject.Inject

/** Estado de carga de las pantallas de proveedores; `UNAVAILABLE` = el endpoint aún no existe. */
enum class Load {
    LOADING,
    READY,
    UNAVAILABLE,
    FAILED,
}

/** `null` si la sesión terminó (401): la app ya volvió al inicio. */
internal suspend fun SessionRepository.loadFor(error: AppError): Load? =
    when {
        endsSession(error) -> null
        error == AppError.NotFound -> Load.UNAVAILABLE
        else -> Load.FAILED
    }

const val MAX_COMPARE = 3
const val MIN_COMPARE = 2

data class ProvidersUiState(
    val load: Load = Load.LOADING,
    val providers: List<Provider> = emptyList(),
    val category: ProviderCategory? = null,
    val query: String = "",
    val compare: List<String> = emptyList(),
    val comparing: Boolean = false,
) {
    /** Solo publicados de la categoría y con el nombre buscado (`011` RF-01). */
    val visible: List<Provider>
        get() =
            providers.filter { provider ->
                provider.published &&
                    (category == null || provider.category == category) &&
                    (query.isBlank() || provider.businessName.contains(query.trim(), ignoreCase = true))
            }

    val compared: List<Provider> get() = compare.mapNotNull { id -> providers.firstOrNull { it.id == id } }
}

/** Catálogo y comparación de proveedores del cliente (`011` RF-01–RF-03). */
@HiltViewModel
class ProvidersViewModel
    @Inject
    constructor(
        private val repository: ProvidersRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(ProvidersUiState())
        val state: StateFlow<ProvidersUiState> = ui.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            ui.value = ui.value.copy(load = Load.LOADING)
            viewModelScope.launch {
                when (val result = repository.catalog()) {
                    is AppResult.Success -> ui.value = ui.value.copy(load = Load.READY, providers = result.value)
                    is AppResult.Failure -> session.loadFor(result.error)?.let { ui.value = ui.value.copy(load = it) }
                }
            }
        }

        fun selectCategory(category: ProviderCategory?) {
            ui.value = ui.value.copy(category = category)
        }

        fun search(query: String) {
            ui.value = ui.value.copy(query = query)
        }

        /** Marca o desmarca; no deja pasar de [MAX_COMPARE]. */
        fun toggleCompare(id: String) {
            val current = ui.value.compare
            val next =
                when {
                    id in current -> current - id
                    current.size >= MAX_COMPARE -> current
                    else -> current + id
                }
            ui.value = ui.value.copy(compare = next, comparing = ui.value.comparing && next.size >= MIN_COMPARE)
        }

        fun showCompare(show: Boolean) {
            ui.value = ui.value.copy(comparing = show && ui.value.compare.size >= MIN_COMPARE)
        }
    }

data class ProviderDetailUiState(
    val load: Load = Load.LOADING,
    val provider: Provider? = null,
    val reviews: VenueReviews? = null,
)

/** Detalle del proveedor con sus reseñas (`011` RF-03). */
@HiltViewModel
class ProviderDetailViewModel
    @Inject
    constructor(
        private val repository: ProvidersRepository,
        private val session: SessionRepository,
    ) : ViewModel() {
        private val ui = MutableStateFlow(ProviderDetailUiState())
        val state: StateFlow<ProviderDetailUiState> = ui.asStateFlow()
        private var providerId: String? = null

        fun open(id: String) {
            if (id == providerId && ui.value.load == Load.READY) return
            providerId = id
            ui.value = ProviderDetailUiState()
            viewModelScope.launch {
                when (val result = repository.provider(id)) {
                    is AppResult.Success -> {
                        ui.value = ui.value.copy(load = Load.READY, provider = result.value)
                        // Las reseñas son opcionales: si fallan, el detalle sigue.
                        (repository.reviews(id) as? AppResult.Success)?.let {
                            ui.value =
                                ui.value.copy(reviews = it.value)
                        }
                    }
                    is AppResult.Failure -> session.loadFor(result.error)?.let { ui.value = ui.value.copy(load = it) }
                }
            }
        }

        fun retry() {
            val id = providerId ?: return
            providerId = null
            open(id)
        }
    }
