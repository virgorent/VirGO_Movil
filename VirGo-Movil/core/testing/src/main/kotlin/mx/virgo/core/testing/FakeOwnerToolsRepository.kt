package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.OwnerToolsRepository
import mx.virgo.core.domain.OwnerVenueTools
import mx.virgo.core.domain.Review
import mx.virgo.core.domain.ReviewReply
import mx.virgo.core.domain.ReviewsRepository
import mx.virgo.core.domain.VenuePricing
import mx.virgo.core.domain.VenueReviews
import mx.virgo.core.domain.VenueSetups
import java.math.BigDecimal

class FakeOwnerToolsRepository : OwnerToolsRepository {
    val venues = mutableListOf(sampleVenueTools())
    var saveFailure: AppResult.Failure? = null
    val savedPricing = mutableListOf<VenuePricing>()
    val savedSetups = mutableListOf<VenueSetups>()

    override suspend fun venues(): AppResult<List<OwnerVenueTools>> = AppResult.Success(venues.toList())

    override suspend fun savePricing(
        venueId: String,
        pricing: VenuePricing,
    ): AppResult<Unit> {
        savedPricing += pricing
        return saveFailure ?: AppResult.Success(Unit)
    }

    override suspend fun saveSetups(
        venueId: String,
        setups: VenueSetups,
    ): AppResult<Unit> {
        savedSetups += setups
        return saveFailure ?: AppResult.Success(Unit)
    }
}

class FakeReviewsRepository : ReviewsRepository {
    var reviews = VenueReviews(4.5, 1, listOf(sampleReview()))
    var failure: AppResult.Failure? = null
    val created = mutableListOf<List<Any>>()

    override suspend fun venueReviews(venueId: String): AppResult<VenueReviews> = failure ?: AppResult.Success(reviews)

    override suspend fun create(
        venueId: String,
        bookingId: String,
        stars: Int,
        text: String,
    ): AppResult<Review> {
        created += listOf(venueId, bookingId, stars, text)
        return failure ?: AppResult.Success(sampleReview().copy(stars = stars, text = text))
    }

    override suspend fun reply(
        reviewId: String,
        text: String,
    ): AppResult<Review> = failure ?: AppResult.Success(sampleReview().copy(reply = ReviewReply(text, null)))
}

fun sampleVenueTools(): OwnerVenueTools =
    OwnerVenueTools(
        id = "132a1f58-ebf9-4082-acdf-7241975f1811",
        name = "Salón Las Palmas",
        published = true,
        pricing = VenuePricing(BigDecimal("3500"), "hour", SAMPLE_MIN_HOURS),
        setups = null,
    )

fun sampleReview(): Review = Review("r-1", "Ana T.", SAMPLE_STARS, "Todo excelente", null, null)

private const val SAMPLE_MIN_HOURS = 5
private const val SAMPLE_STARS = 5
