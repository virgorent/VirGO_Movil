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

/** Detalle de `GET /venues/:id`; tolerante a campos ausentes (006 ADR-01). */
@Serializable
data class VenueDetailDto(
    val id: String,
    val name: String,
    val shortName: String? = null,
    val description: String? = null,
    val address: String? = null,
    val zone: String? = null,
    val capacity: Int? = null,
    val minimumHours: Int? = null,
    val price: VenuePriceDto? = null,
    val categories: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val rating: VenueRatingDto? = null,
    val verified: Boolean = false,
)
