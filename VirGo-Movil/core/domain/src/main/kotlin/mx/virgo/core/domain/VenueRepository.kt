package mx.virgo.core.domain

import mx.virgo.core.common.AppResult

interface VenueRepository {
    suspend fun getVenues(
        zone: String? = null,
        category: String? = null,
    ): AppResult<List<Venue>>
}
