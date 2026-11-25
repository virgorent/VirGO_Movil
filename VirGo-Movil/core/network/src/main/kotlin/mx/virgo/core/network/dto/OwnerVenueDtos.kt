package mx.virgo.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class OwnerVenuesResponseDto(
    val data: List<OwnerVenueDto> = emptyList(),
)

/** Respuesta de `mine`, `POST` y `PATCH`; `price.baseAmount` y `minimumHours` en la raíz. */
@Serializable
data class OwnerVenueDto(
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
    val status: String? = null,
)

/** Precio de la petición: `base`, `unit` (`hour`/`day`) y `minHours`. */
@Serializable
data class VenuePriceRequestDto(
    val base: Double,
    val unit: String,
    val minHours: Int,
)

@Serializable
data class VenueServiceRequestDto(
    val name: String,
    val included: Boolean,
    val price: Double,
)

/**
 * Sin valores por omisión: el serializador no emite los campos con su valor por omisión
 * y el servidor exige `services`, `categories` y `amenities` aunque vayan vacíos.
 */
@Serializable
data class CreateVenueRequestDto(
    val name: String,
    val shortName: String,
    val description: String,
    val address: String,
    val zone: String,
    val capacity: Int,
    val price: VenuePriceRequestDto,
    val services: List<VenueServiceRequestDto>,
    val categories: List<String>,
    val amenities: List<String>,
)

/** Solo se envían los campos presentes; `null` se omite. */
@Serializable
data class PatchVenueRequestDto(
    val name: String? = null,
    val shortName: String? = null,
    val description: String? = null,
    val address: String? = null,
    val zone: String? = null,
    val capacity: Int? = null,
    val price: VenuePriceRequestDto? = null,
    val services: List<VenueServiceRequestDto>? = null,
    val categories: List<String>? = null,
    val amenities: List<String>? = null,
)

@Serializable
data class PublishVenueResponseDto(
    val id: String,
    val status: String,
)
