package mx.virgo.app.ui.ownertools

import mx.virgo.core.domain.VenuePricing
import mx.virgo.core.domain.VenueSetups

/** Callbacks de precios y montajes, agrupados para probar el contenido sin `ViewModel`. */
data class VenueToolActions(
    val onVenue: (String) -> Unit = {},
    val onPricing: (VenuePricing) -> Unit = {},
    val onSetups: (VenueSetups) -> Unit = {},
    val onSave: () -> Unit = {},
    val onRetry: () -> Unit = {},
)

/** Callbacks de reseñas del propietario. */
data class OwnerReviewsActions(
    val onVenue: (String) -> Unit = {},
    val onReply: (Pair<String, String>?) -> Unit = {},
    val onSend: () -> Unit = {},
)

/** Qué vista de datos se muestra. */
enum class OwnerInsight {
    PAYMENTS,
    STATS,
}

/** Qué herramienta del salón se muestra. */
enum class VenueTool {
    PRICING,
    SETUPS,
}
