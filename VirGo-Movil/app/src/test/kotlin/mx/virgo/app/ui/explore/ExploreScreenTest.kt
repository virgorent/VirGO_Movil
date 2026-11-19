package mx.virgo.app.ui.explore

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import mx.virgo.app.R
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

    private fun exploreViewModel() =
        ExploreViewModel(
            getVenues = GetVenuesUseCase(FakeVenueRepository()),
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
    fun mapModeShowsHeaderAndActions() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.guest_brand)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_search_hint)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_category_all)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_category_gardens)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_for_you)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_providers)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_mode_map)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_mode_list)).assertIsDisplayed()
    }

    @Test
    fun listModeShowsVenueCards() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_mode_list)).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("Casa Oliva").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.explore_sort_recommended)).assertIsDisplayed()
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

    /** RF-19: cuando ningún salón cumple todos los filtros se muestra el aviso. */
    @Test
    fun filtersWithNoMatchShowEmptyState() {
        setContent(exploreViewModel())
        compose.onNodeWithText(string(R.string.explore_category_elegant)).performClick()
        compose.onNodeWithText(string(R.string.explore_search_hint)).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("2 km").performClick()
        compose.onNodeWithText(string(R.string.explore_filters_apply)).performClick()
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
