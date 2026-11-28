package mx.virgo.core.data.repository

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.OwnerToolsRepository
import mx.virgo.core.domain.OwnerVenueTools
import mx.virgo.core.domain.Review
import mx.virgo.core.domain.ReviewReply
import mx.virgo.core.domain.ReviewsRepository
import mx.virgo.core.domain.VenuePricing
import mx.virgo.core.domain.VenueReviews
import mx.virgo.core.domain.VenueSetups
import mx.virgo.core.network.api.OwnerToolsApi
import mx.virgo.core.network.dto.CreateReviewDto
import mx.virgo.core.network.dto.ReplyReviewDto
import mx.virgo.core.network.dto.ReviewDto
import mx.virgo.core.network.dto.VenuePricingRequestDto
import mx.virgo.core.network.dto.VenueSetupsRequestDto
import mx.virgo.core.network.dto.VenueToolsDto
import mx.virgo.core.network.dto.VenueToolsPatchDto
import retrofit2.HttpException
import retrofit2.Response
import java.math.BigDecimal
import javax.inject.Inject

class OwnerToolsRepositoryImpl
    @Inject
    constructor(
        private val api: OwnerToolsApi,
    ) : OwnerToolsRepository,
        ReviewsRepository {
        override suspend fun venues(): AppResult<List<OwnerVenueTools>> =
            contractCall { AppResult.Success(api.mine().data.map { it.toTools() }) }

        override suspend fun savePricing(
            venueId: String,
            pricing: VenuePricing,
        ): AppResult<Unit> =
            contractCall {
                val price =
                    VenuePricingRequestDto(
                        base = pricing.base.toDouble(),
                        unit = pricing.unit,
                        minHours = pricing.minHours,
                        weekendPct = pricing.weekendPct,
                        highSeasonPct = pricing.highSeasonPct,
                        highSeasonMonths = pricing.highSeasonMonths.sorted(),
                    )
                api.patch(venueId, VenueToolsPatchDto(price = price)).requireSuccess()
                confirmSaved(venueId) { it.price?.weekendPct != null }
            }

        override suspend fun saveSetups(
            venueId: String,
            setups: VenueSetups,
        ): AppResult<Unit> =
            contractCall {
                val body = VenueSetupsRequestDto(setups.banquet, setups.auditorium, setups.cocktail)
                api.patch(venueId, VenueToolsPatchDto(setups = body)).requireSuccess()
                confirmSaved(venueId) { it.setups != null }
            }

        /**
         * El backend actual ignora los campos que no conoce y responde `200`: se relee el salón y, si el
         * campo no volvió, se trata como endpoint ausente («llegará pronto») en vez de decir «Guardado».
         */
        private suspend fun confirmSaved(
            venueId: String,
            persisted: (VenueToolsDto) -> Boolean,
        ): AppResult<Unit> {
            val saved = api.mine().data.firstOrNull { it.id == venueId }
            val confirmed = saved != null && persisted(saved)
            return if (confirmed) AppResult.Success(Unit) else AppResult.Failure(AppError.NotFound)
        }

        override suspend fun venueReviews(venueId: String): AppResult<VenueReviews> =
            contractCall {
                val dto = api.reviews(venueId)
                AppResult.Success(VenueReviews(dto.rating, dto.count, dto.data.map { it.toReview() }))
            }

        override suspend fun create(
            venueId: String,
            bookingId: String,
            stars: Int,
            text: String,
        ): AppResult<Review> =
            contractCall {
                AppResult.Success(
                    api
                        .createReview(
                            CreateReviewDto(
                                targetId = venueId,
                                bookingId = bookingId,
                                stars = stars,
                                text = text.trim(),
                            ),
                        ).toReview(),
                )
            }

        override suspend fun reply(
            reviewId: String,
            text: String,
        ): AppResult<Review> =
            contractCall { AppResult.Success(api.reply(reviewId, ReplyReviewDto(text.trim())).toReview()) }
    }

private fun Response<Unit>.requireSuccess() {
    if (!isSuccessful) throw HttpException(this)
}

private fun VenueToolsDto.toTools(): OwnerVenueTools =
    OwnerVenueTools(
        id = id,
        name = name,
        published = status == "published",
        pricing =
            VenuePricing(
                base = BigDecimal.valueOf(price?.baseAmount ?: 0.0),
                unit = price?.unit ?: "hour",
                minHours = price?.minHours ?: minimumHours ?: 1,
                weekendPct = price?.weekendPct ?: 0,
                highSeasonPct = price?.highSeasonPct ?: 0,
                highSeasonMonths = price?.highSeasonMonths.orEmpty().toSet(),
            ),
        setups = setups?.let { VenueSetups(it.banquet, it.auditorium, it.cocktail) },
    )

internal fun ReviewDto.toReview(): Review =
    Review(
        id = id,
        authorName = authorName.orEmpty(),
        stars = stars,
        text = text.orEmpty(),
        createdAt = createdAt,
        reply = reply?.let { ReviewReply(it.text, it.at) },
    )
