package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Venue
import mx.virgo.core.domain.VenueRepository

class FakeVenueRepository : VenueRepository {
    var result: AppResult<List<Venue>> = AppResult.Success(emptyList())

    var calls: Int = 0
        private set

    override suspend fun getVenues(
        zone: String?,
        category: String?,
    ): AppResult<List<Venue>> {
        calls += 1
        return result
    }
}
