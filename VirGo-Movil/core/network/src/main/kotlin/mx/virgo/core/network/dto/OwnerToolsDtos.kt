package mx.virgo.core.network.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/** Precio como lo devuelve `GET /venues/mine` (`baseAmount`) más los campos del contrato §6. */
@Serializable
data class VenueToolsPriceDto(
    val baseAmount: Double? = null,
    val unit: String? = null,
    val minHours: Int? = null,
    val weekendPct: Int? = null,
    val highSeasonPct: Int? = null,
    val highSeasonMonths: List<Int>? = null,
)

@Serializable
data class VenueSetupsDto(
    val banquet: Int = 0,
    val auditorium: Int = 0,
    val cocktail: Int = 0,
)

@Serializable
data class VenueToolsDto(
    val id: String,
    val name: String,
    val status: String? = null,
    val minimumHours: Int? = null,
    val price: VenueToolsPriceDto? = null,
    val setups: VenueSetupsDto? = null,
)

@Serializable
data class VenueToolsResponseDto(
    val data: List<VenueToolsDto> = emptyList(),
)

/** `price` completo como lo pide `PATCH /venues/:id` (`base`, `unit`, `minHours`) más los aumentos. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VenuePricingRequestDto(
    val base: Double,
    val unit: String,
    val minHours: Int,
    @EncodeDefault val weekendPct: Int = 0,
    @EncodeDefault val highSeasonPct: Int = 0,
    @EncodeDefault val highSeasonMonths: List<Int> = emptyList(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class VenueSetupsRequestDto(
    @EncodeDefault val banquet: Int = 0,
    @EncodeDefault val auditorium: Int = 0,
    @EncodeDefault val cocktail: Int = 0,
)

@Serializable
data class VenueToolsPatchDto(
    val price: VenuePricingRequestDto? = null,
    val setups: VenueSetupsRequestDto? = null,
)

@Serializable
data class ReviewReplyDto(
    val text: String,
    val at: String? = null,
)

@Serializable
data class ReviewDto(
    val id: String,
    val authorName: String? = null,
    val stars: Int = 0,
    val text: String? = null,
    val createdAt: String? = null,
    val reply: ReviewReplyDto? = null,
)

@Serializable
data class VenueReviewsDto(
    val rating: Double = 0.0,
    val count: Int = 0,
    val data: List<ReviewDto> = emptyList(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class CreateReviewDto(
    @EncodeDefault val targetType: String = "venue",
    val targetId: String,
    val bookingId: String,
    val stars: Int,
    val text: String,
)

@Serializable
data class ReplyReviewDto(
    val text: String,
)
