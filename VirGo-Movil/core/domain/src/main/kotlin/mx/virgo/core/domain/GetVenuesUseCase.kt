package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class GetVenuesUseCase
    @Inject
    constructor(
        private val repository: VenueRepository,
    ) {
        suspend operator fun invoke(
            zone: String? = null,
            category: String? = null,
        ): AppResult<List<Venue>> = repository.getVenues(zone, category)
    }
