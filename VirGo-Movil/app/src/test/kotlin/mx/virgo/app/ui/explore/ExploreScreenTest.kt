package mx.virgo.app.ui.explore

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.designsystem.theme.VirGoTheme
import mx.virgo.core.domain.Favorite
import mx.virgo.core.domain.FavoritesRepository
import mx.virgo.core.domain.GetFavoritesUseCase
import mx.virgo.core.domain.GetNoticesUseCase
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.Notice
import mx.virgo.core.domain.NoticesRepository
import mx.virgo.core.domain.ToggleFavoriteUseCase
import mx.virgo.core.testing.FakeVenueRepository
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Pruebas de UI de la pantalla B8/C9/C10/C11 del spec `005`. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExploreScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private fun string(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)

    private fun exploreViewModel(venues: FakeVenueRepository = FakeVenueRepository()) =
        ExploreViewModel(
            getVenues = GetVenuesUseCase(venues),
            getFavorites = GetFavoritesUseCase(ScreenFakeFavoritesRepository()),
            toggleFavorite = ToggleFavoriteUseCase(ScreenFakeFavoritesRepository()),
            getNotices = GetNoticesUseCase(ScreenFakeNoticesRepository()),
        )

    private fun setContent(viewModel: ExploreViewModel) {
        compose.setContent {
            VirGoTheme {
                ExploreScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun initialListShowsHeaderAndActions() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_home_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_search_hint)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_category_all)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_category_gardens)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_for_you)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_providers)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_mode_map)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_mode_list)).assertIsDisplayed()
    }

    @Test
    fun sampleCreditsAreAvailableInApp() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_mode_map)).performClick()
        compose.onNodeWithText(string(R.string.sample_photo_credits)).performClick()
        compose.onNodeWithText(string(R.string.sample_photo_credits_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.action_close)).performClick()
        compose.onNodeWithText(string(R.string.sample_photo_credits_title)).assertDoesNotExist()
    }

    @Test
    fun initialListCanScrollToSampleCredits() {
        setContent(exploreViewModel())
        compose
            .onNode(hasScrollToIndexAction())
            .performScrollToNode(hasText(string(R.string.sample_photo_credits)))
        compose.onNodeWithText(string(R.string.sample_photo_credits)).performClick()
        compose.onNodeWithText(string(R.string.sample_photo_credits_title)).assertIsDisplayed()
    }

    @Test
    fun listModeShowsVenueCards() {
        setContent(exploreViewModel())
        compose.waitForIdle()
        compose.onNodeWithText("Casa Oliva").assertIsDisplayed()
        compose.onAllNodesWithText(string(R.string.explore_sample_image)).onFirst().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_sort_recommended)).assertIsDisplayed()
    }

    @Test
    fun venueCardOpensMapAndCanReturnToList() {
        val viewModel = exploreViewModel()
        setContent(viewModel)
        compose.onNodeWithText("Casa Oliva").performClick()
        compose.onNodeWithText(string(R.string.explore_featured)).assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(ExploreMode.MAP, viewModel.state.value.mode)
            val selectedVenue = viewModel.state.value.selectedVenue
            assertEquals("Casa Oliva", selectedVenue?.name)
        }
        compose.onNodeWithText(string(R.string.explore_mode_list)).performClick()
        compose.onAllNodesWithText(string(R.string.explore_sample_image)).onFirst().assertIsDisplayed()
        compose.runOnIdle { assertEquals(ExploreMode.LIST, viewModel.state.value.mode) }
    }

    @Test
    fun apiFailureShowsSafeMessageAndRetry() {
        val repository =
            FakeVenueRepository().apply {
                result = AppResult.Failure(AppError.Server("internal-trace-not-for-display"))
            }
        setContent(exploreViewModel(repository))
        compose.onNodeWithText(string(R.string.guest_error_load)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.guest_retry)).assertIsDisplayed()
        compose.onNodeWithText("internal-trace-not-for-display").assertDoesNotExist()
    }

    @Test
    fun offlineErrorCanRetryAndShowCatalog() {
        val repository = FakeVenueRepository().apply { result = AppResult.Failure(AppError.Network) }
        setContent(exploreViewModel(repository))
        compose.onNodeWithText(string(R.string.guest_error_load)).assertIsDisplayed()
        compose.runOnIdle { repository.result = AppResult.Success(emptyList()) }
        compose.onNodeWithText(string(R.string.guest_retry)).performClick()
        compose.onNodeWithText("Casa Oliva").assertIsDisplayed()
        compose.runOnIdle { assertEquals(2, repository.calls) }
    }

    @Test
    fun searchBarOpensFiltersPanel() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_search_hint)).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.explore_filters_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_filters_distance)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_filters_apply)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun forYouOpensRecommendationsPanel() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_for_you)).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.explore_recommended_title)).assertIsDisplayed()
        compose
            .onAllNodesWithText(string(R.string.explore_ai_badge))
            .onFirst()
            .assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_event_wedding)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun recommendationsIdentifySamplePhotosAndExposeCredits() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_for_you)).performClick()
        compose.onNodeWithText(string(R.string.sample_photo_credits)).performScrollTo().performClick()
        compose.onNodeWithText(string(R.string.sample_photo_credits_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.action_close)).performClick()
        compose.onNodeWithText(string(R.string.sample_photo_credits_title)).assertDoesNotExist()
        compose.onNodeWithText(string(R.string.explore_recommended_title)).assertExists()
    }

    /** RF-19: cuando ningún salón cumple todos los filtros se muestra el aviso. */
    @Test
    fun filtersWithNoMatchShowEmptyState() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_category_elegant)).performClick()
        compose.onNodeWithText(string(R.string.explore_search_hint)).performClick()
        compose.waitForIdle()
        // Bring the horizontal distance row into the vertically scrolling panel first.
        compose.onNodeWithText("2 km").onParent().performScrollTo()
        compose
            .onNodeWithText("2 km")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.onNodeWithText(string(R.string.explore_filters_title)).assertExists()
        compose
            .onNodeWithText(string(R.string.explore_filters_apply))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.explore_empty)).assertIsDisplayed()
    }

    /** RF-13: un valor no numérico de invitados marca el error y no cierra el panel. */
    @Test
    fun invalidGuestsKeepsFiltersPanelOpen() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_search_hint)).performClick()
        compose.waitForIdle()
        compose.onNodeWithTag(EXPLORE_GUESTS_FIELD_TAG).performTextInput("abc")
        compose.onNodeWithText(string(R.string.explore_filters_apply)).performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText(string(R.string.explore_filters_guests_error)).performScrollTo().assertIsDisplayed()
        // El panel sigue abierto: el título existe aunque esté fuera del área visible.
        compose.onNodeWithText(string(R.string.explore_filters_title)).assertExists()
    }
}

private class ScreenFakeFavoritesRepository : FavoritesRepository {
    override suspend fun getFavorites(): AppResult<List<Favorite>> = AppResult.Success(emptyList())

    override suspend fun addFavorite(
        targetType: String,
        targetId: String,
        name: String?,
        photoUrl: String?,
        rating: Double?,
    ): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun removeFavorite(
        targetType: String,
        targetId: String,
    ): AppResult<Unit> = AppResult.Success(Unit)
}

private class ScreenFakeNoticesRepository : NoticesRepository {
    override suspend fun getNotices(): AppResult<List<Notice>> = AppResult.Success(emptyList())
}
