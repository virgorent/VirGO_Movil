package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class VenuesResponseDto(
    val data: List<VenueDto> = emptyList(),
)

@Serializable
data class VenueDto(
    val id: String,
    val name: String,
    val shortName: String? = null,
    val zone: String? = null,
    val capacity: Int? = null,
    val price: VenuePriceDto? = null,
    val categories: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val rating: VenueRatingDto? = null,
    val location: VenueLocationDto? = null,
)

@Serializable
data class VenuePriceDto(
    val baseAmount: Double? = null,
    val unit: String? = null,
)

@Serializable
data class VenueRatingDto(
    val average: Double = 0.0,
    val count: Int = 0,
)

@Serializable
data class VenueLocationDto(
    val type: String = "Point",
    val coordinates: List<Double> = emptyList(),
)
