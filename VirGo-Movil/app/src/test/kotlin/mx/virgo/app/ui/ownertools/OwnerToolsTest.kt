package mx.virgo.app.ui.ownertools

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import mx.virgo.app.session.FakeSessionTokenStore
import mx.virgo.app.session.SessionRepository
import mx.virgo.app.ui.finance.SaveNotice
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.GetMyVenuesUseCase
import mx.virgo.core.domain.OwnerVenue
import mx.virgo.core.domain.VenueReviews
import mx.virgo.core.domain.VenueStatus
import mx.virgo.core.testing.FakeOwnerToolsRepository
import mx.virgo.core.testing.FakeOwnerVenuesRepository
import mx.virgo.core.testing.FakeReviewsRepository
import mx.virgo.core.testing.MainDispatcherRule
import mx.virgo.core.testing.sampleReview
import mx.virgo.core.testing.sampleVenueDraft
import mx.virgo.core.testing.sampleVenueTools
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w320dp-h800dp")
class OwnerToolsTest {
    @get:Rule
    val compose = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tools = FakeOwnerToolsRepository()
    private val reviews = FakeReviewsRepository()
    private val venues = FakeOwnerVenuesRepository()
    private val session =
        SessionRepository(FakeSessionTokenStore(), Clock.fixed(Instant.parse("2026-11-25T00:00:00Z"), ZoneOffset.UTC))

    @Test
    fun pricingIsSavedAndMissingFieldSaysSoon() {
        val vm = VenueToolsViewModel(tools, session)
        vm.editPricing(sampleVenueTools().pricing.copy(weekendPct = 15))
        vm.savePricing()
        assertEquals(15, tools.savedPricing.single().weekendPct)
        assertEquals(SaveNotice.SAVED, vm.state.value.notice)
        tools.saveFailure = AppResult.Failure(AppError.Validation(emptyMap()))
        vm.saveSetups()
        assertEquals(SaveNotice.UNAVAILABLE, vm.state.value.notice)
    }

    @Test
    fun ownerRepliesToAReview() {
        venues.venues += OwnerVenue(sampleVenueTools().id, sampleVenueDraft(), VenueStatus.PUBLISHED)
        val vm = OwnerReviewsViewModel(GetMyVenuesUseCase(venues), reviews, session)
        assertEquals(ReviewsStatus.READY, vm.state.value.status)
        vm.editReply("r-1" to "¡Gracias por venir!")
        vm.sendReply()
        assertEquals(
            "¡Gracias por venir!",
            vm.state.value.reviews
                ?.reviews
                ?.single()
                ?.reply
                ?.text,
        )
    }

    @Test
    fun clientReviewConflictSaysAlreadyRated() {
        reviews.failure = AppResult.Failure(AppError.Conflict)
        val vm = ReviewFormViewModel(reviews, session)
        vm.edit(ReviewFormState(open = true, stars = 4, text = "Bien"))
        vm.publish("v1", "b1")
        assertEquals(ReviewOutcome.ALREADY, vm.state.value.outcome)
        assertEquals(listOf<Any>("v1", "b1", 4, "Bien"), reviews.created.single())
    }

    @Test
    fun pricingScreenShowsPreviewAndBlocksInvalidPercent() {
        val pricing = sampleVenueTools().pricing.copy(weekendPct = 150)
        val state =
            VenueToolsUiState(
                loading = false,
                venues = listOf(sampleVenueTools()),
                venueId = sampleVenueTools().id,
                pricing = pricing,
            )
        compose.setContent {
            VirGoTheme { VenueToolContent(VenueTool.PRICING, state, VenueToolActions(), LocalDate.parse("2026-11-27")) }
        }
        compose.onNodeWithText("Así lo verá el cliente").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Los aumentos van de 0 a 100 %.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Guardar").performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun reviewsScreenShowsSummary() {
        val state =
            OwnerReviewsUiState(status = ReviewsStatus.READY, reviews = VenueReviews(4.5, 1, listOf(sampleReview())))
        compose.setContent { VirGoTheme { OwnerReviewsContent(state, OwnerReviewsActions()) } }
        compose.onNodeWithText("Todo excelente").assertIsDisplayed()
        compose.onNodeWithText("Responder").assertIsDisplayed()
    }
}
