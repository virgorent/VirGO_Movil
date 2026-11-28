package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class GetMyVenuesUseCase
    @Inject
    constructor(
        private val repository: OwnerVenuesRepository,
    ) {
        suspend operator fun invoke(): AppResult<List<OwnerVenue>> = repository.mine()
    }

/** Crea el salón si aún no tiene `id` (022 RF-05); si ya existe, solo lo actualiza. */
class SaveVenueDraftUseCase
    @Inject
    constructor(
        private val repository: OwnerVenuesRepository,
    ) {
        suspend operator fun invoke(
            venueId: String?,
            draft: VenueDraft,
            includeServices: Boolean = true,
        ): AppResult<OwnerVenue> =
            if (venueId == null) {
                repository.create(draft)
            } else {
                repository.update(venueId, draft, includeServices)
            }
    }

/**
 * Publica un salón ya guardado (022 RF-06). Quien llama guarda primero con
 * [SaveVenueDraftUseCase] y conserva el `id`, para no duplicar el salón si la publicación falla.
 */
class PublishVenueUseCase
    @Inject
    constructor(
        private val repository: OwnerVenuesRepository,
    ) {
        suspend operator fun invoke(venueId: String): AppResult<VenueStatus> = repository.publish(venueId)
    }
