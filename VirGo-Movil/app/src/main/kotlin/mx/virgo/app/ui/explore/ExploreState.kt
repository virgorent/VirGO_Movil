package mx.virgo.app.ui.explore

import mx.virgo.app.ui.guest.VenueSample

internal const val EXPLORE_DEFAULT_RADIUS_KM = 10
internal const val EXPLORE_NO_VENUE = -1
internal const val EXPLORE_RECOMMENDATION_LIMIT = 5
internal const val EXPLORE_DATE_PATTERN = "dd/MM/yyyy"

/** Tag de prueba del campo de invitados del panel de filtros (RF-13). */
const val EXPLORE_GUESTS_FIELD_TAG = "explore_filters_guests"

/** Distancias que ofrece el filtro de distancia máxima (RF-15). */
internal val ExploreRadiusOptions = listOf(2, 5, 10, 20)

/** Órdenes disponibles en C9 y C10 (RF-11 y RF-17). */
internal val ExploreOrders =
    listOf(
        ExploreOrder.RECOMMENDED,
        ExploreOrder.NEARNESS,
        ExploreOrder.PRICE,
        ExploreOrder.RATING,
    )

/** Tipos de evento que usan las recomendaciones (RF-21). */
internal val ExploreEvents =
    listOf(
        ExploreEventType.WEDDING,
        ExploreEventType.BIRTHDAY,
        ExploreEventType.CORPORATE,
        ExploreEventType.FAMILY,
    )

enum class ExploreMode {
    MAP,
    LIST,
}

enum class ExploreOrder {
    RECOMMENDED,
    NEARNESS,
    PRICE,
    RATING,
}

enum class ExploreEventType {
    WEDDING,
    BIRTHDAY,
    CORPORATE,
    FAMILY,
}

enum class ExploreSheet {
    NONE,
    FILTERS,
    RECOMMENDED,
}

/** Motivo por el que un salón «encaja» en las recomendaciones (RF-22). */
enum class ExploreReason {
    CAPACITY,
    BUDGET,
    CLOSE,
    RATING,
}

/** Campo con valor inválido en el panel de filtros; `NONE` cuando todo está bien (RF-18). */
enum class ExploreFieldError {
    NONE,
    GUESTS,
    BUDGET,
    DATE,
}

data class ExploreFilters(
    val query: String = "",
    val category: String? = null,
    val radiusKm: Int = EXPLORE_DEFAULT_RADIUS_KM,
    val guests: String = "",
    val budget: String = "",
    val date: String = "",
    val order: ExploreOrder = ExploreOrder.RECOMMENDED,
)

internal val ExploreFilters.guestsCount: Int?
    get() = guests.trim().toIntOrNull()?.takeIf { it > 0 }

internal val ExploreFilters.budgetAmount: Double?
    get() = budget.trim().toDoubleOrNull()?.takeIf { it > 0 }

data class ExploreRecommendation(
    val venue: VenueSample,
    val fitPercent: Int,
    val reason: ExploreReason,
)

data class ExploreUiState(
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val mode: ExploreMode = ExploreMode.MAP,
    val sheet: ExploreSheet = ExploreSheet.NONE,
    val filters: ExploreFilters = ExploreFilters(),
    val fieldError: ExploreFieldError = ExploreFieldError.NONE,
    val results: List<VenueSample> = emptyList(),
    val selectedVenueId: Int = EXPLORE_NO_VENUE,
    val selectedVenue: VenueSample? = null,
    val favoriteIds: Set<String> = emptySet(),
    val unreadNotices: Int = 0,
    val eventType: ExploreEventType = ExploreEventType.WEDDING,
    val recommendations: List<ExploreRecommendation> = emptyList(),
)

sealed interface ExploreUiEvent {
    data object FavoriteError : ExploreUiEvent
}
