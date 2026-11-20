package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Venue
import mx.virgo.core.domain.VenueRepository
import mx.virgo.core.network.api.VenuesApi
import mx.virgo.core.network.dto.VenueDto
import javax.inject.Inject

class VenueRepositoryImpl
    @Inject
    constructor(
        private val venuesApi: VenuesApi,
    ) : VenueRepository {
        @Suppress("TooGenericExceptionCaught")
        override suspend fun getVenues(
            zone: String?,
            category: String?,
        ): AppResult<List<Venue>> =
            try {
                val response = venuesApi.venues(zone = zone, category = category)
                AppResult.Success(response.data.map { it.toVenue() })
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun VenueDto.toVenue(): Venue =
    Venue(
        id = id,
        name = name,
        shortName = shortName,
        zone = zone,
        capacity = capacity ?: 0,
        ratingAverage = rating?.average ?: 0.0,
        ratingCount = rating?.count ?: 0,
        latitude = location?.coordinates?.getOrNull(LAT_INDEX),
        longitude = location?.coordinates?.getOrNull(LNG_INDEX),
        priceBaseAmount = price?.baseAmount,
        priceUnit = price?.unit,
        categories = categories,
        photos = photos,
    )

private const val LNG_INDEX = 0
private const val LAT_INDEX = 1
