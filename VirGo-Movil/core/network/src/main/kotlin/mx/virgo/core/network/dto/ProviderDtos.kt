package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProvidersResponseDto(
    val data: List<ProviderDto> = emptyList(),
)

/** Esquema `providers` del modelo de datos; tolerante a campos ausentes. */
@Serializable
data class ProviderDto(
    val id: String,
    val businessName: String = "",
    val category: String? = null,
    val description: String? = null,
    val pricing: String? = null,
    val responseTime: String? = null,
    val coverUrl: String? = null,
    val packages: List<ProviderPackageDto> = emptyList(),
    val gallery: List<ProviderPhotoDto> = emptyList(),
    val zones: List<String> = emptyList(),
    val status: String? = null,
    val rating: VenueRatingDto? = null,
)

@Serializable
data class ProviderPackageDto(
    val id: String? = null,
    val name: String = "",
    val price: Double? = null,
    val includes: String? = null,
)

@Serializable
data class ProviderPhotoDto(
    val url: String = "",
    val order: Int = 0,
)

/** Crear (`POST /providers`) o editar (`PATCH /providers/me`); los nulos no se envían. */
@Serializable
data class ProviderRequestDto(
    val businessName: String? = null,
    val category: String? = null,
    val description: String? = null,
    val pricing: String? = null,
    val responseTime: String? = null,
    val packages: List<ProviderPackageDto>? = null,
    val zones: List<String>? = null,
    val status: String? = null,
)

@Serializable
data class GigsResponseDto(
    val data: List<GigDto> = emptyList(),
)

/** Esquema `gigs`: `date` `AAAA-MM-DD`, `start`/`end` `HH:mm`. */
@Serializable
data class GigDto(
    val id: String? = null,
    val eventName: String = "",
    val clientName: String = "",
    val place: String = "",
    val date: String? = null,
    val start: String? = null,
    val end: String? = null,
    val amount: Double? = null,
    val quoteId: String? = null,
)
