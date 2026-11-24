package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

/**
 * Agrega o quita un salón de favoritos según el estado actual de [isFavorite].
 * El índice único del backend ({userId, targetType, targetId}) hace que repetir la
 * operación sea idempotente en el origen: el `remove` se omite cuando no existe.
 */
class ToggleFavoriteUseCase
    @Inject
    constructor(
        private val repository: FavoritesRepository,
    ) {
        suspend operator fun invoke(
            venue: Venue,
            isFavorite: Boolean,
        ): AppResult<Unit> =
            if (isFavorite) {
                repository.removeFavorite(TARGET_TYPE_VENUE, venue.id)
            } else {
                repository.addFavorite(
                    targetType = TARGET_TYPE_VENUE,
                    targetId = venue.id,
                    name = venue.name,
                    photoUrl = null,
                    rating = venue.ratingAverage.takeIf { it > 0.0 },
                )
            }

        private companion object {
            const val TARGET_TYPE_VENUE = "venue"
        }
    }
