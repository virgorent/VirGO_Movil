package mx.virgo.core.testing

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Venue
import mx.virgo.core.domain.VenueDetail
import mx.virgo.core.domain.VenueDetailRepository
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

class FakeVenueDetailRepository : VenueDetailRepository {
    var result: AppResult<VenueDetail> = AppResult.Failure(AppError.NotFound)
    val requested = mutableListOf<String>()

    override suspend fun venue(id: String): AppResult<VenueDetail> {
        requested += id
        return result
    }
}

fun sampleVenueDetail(): VenueDetail =
    VenueDetail(
        id = "132a1f58-ebf9-4082-acdf-7241975f1811",
        name = "Salón Las Palmas",
        description = "Salón con jardín para 200 invitados en Juriquilla.",
        address = "Av. Juriquilla 100, Querétaro",
        zone = "Juriquilla",
        capacity = 220,
        minimumHours = 4,
        priceBase = 1600.0,
        priceUnit = mx.virgo.core.domain.PriceUnit.DAY,
        categories = listOf("Salon", "Jardin"),
        amenities = listOf("Estacionamiento", "Sonido"),
        ratingAverage = 0.0,
        ratingCount = 0,
        verified = false,
    )
