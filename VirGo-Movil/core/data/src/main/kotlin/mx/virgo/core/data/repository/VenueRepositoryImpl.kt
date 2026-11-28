package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.PriceUnit
import mx.virgo.core.domain.Venue
import mx.virgo.core.domain.VenueDetail
import mx.virgo.core.domain.VenueDetailRepository
import mx.virgo.core.domain.VenueRepository
import mx.virgo.core.network.api.VenuesApi
import mx.virgo.core.network.dto.VenueDetailDto
import mx.virgo.core.network.dto.VenueDto
import javax.inject.Inject

class VenueRepositoryImpl
    @Inject
    constructor(
        private val venuesApi: VenuesApi,
    ) : VenueRepository,
        VenueDetailRepository {
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

        @Suppress("TooGenericExceptionCaught")
        override suspend fun venue(id: String): AppResult<VenueDetail> =
            try {
                AppResult.Success(venuesApi.venue(id).toVenueDetail())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun VenueDetailDto.toVenueDetail(): VenueDetail =
    VenueDetail(
        id = id,
        name = name,
        description = description?.takeIf { it.isNotBlank() },
        address = address?.takeIf { it.isNotBlank() },
        zone = zone?.takeIf { it.isNotBlank() },
        capacity = capacity ?: 0,
        minimumHours = minimumHours,
        priceBase = price?.baseAmount,
        priceUnit = price?.unit?.let { unit -> PriceUnit.entries.firstOrNull { it.wire == unit } },
        categories = categories,
        amenities = amenities,
        ratingAverage = rating?.average ?: 0.0,
        ratingCount = rating?.count ?: 0,
        verified = verified,
        photos = photos,
    )

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
