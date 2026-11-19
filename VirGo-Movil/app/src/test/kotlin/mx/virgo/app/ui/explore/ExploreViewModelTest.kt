package mx.virgo.app.ui.explore

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import mx.virgo.app.ui.guest.CATEGORY_GARDENS
import mx.virgo.app.ui.guest.VenueSamples
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Favorite
import mx.virgo.core.domain.FavoritesRepository
import mx.virgo.core.domain.GetFavoritesUseCase
import mx.virgo.core.domain.GetNoticesUseCase
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.Notice
import mx.virgo.core.domain.NoticesRepository
import mx.virgo.core.domain.ToggleFavoriteUseCase
import mx.virgo.core.domain.Venue
import mx.virgo.core.testing.FakeVenueRepository
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExploreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val venueRepository = FakeVenueRepository()
    private val favoritesRepository = FakeFavoritesRepository()
    private val noticesRepository = FakeNoticesRepository()

    private fun viewModel() =
        ExploreViewModel(
            getVenues = GetVenuesUseCase(venueRepository),
            getFavorites = GetFavoritesUseCase(favoritesRepository),
            toggleFavorite = ToggleFavoriteUseCase(favoritesRepository),
            getNotices = GetNoticesUseCase(noticesRepository),
        )

    @Test
    fun emptyCatalogFallsBackToLocalSamples() {
        val state = viewModel().state.value
        assertFalse(state.isLoading)
        assertFalse(state.hasError)
        assertEquals(VenueSamples.size, state.results.size)
        assertEquals(ExploreMode.MAP, state.mode)
        assertEquals(ExploreSheet.NONE, state.sheet)
    }

    @Test
    fun radiusFilterKeepsOnlyNearbyVenues() {
        val viewModel = viewModel()
        viewModel.onFiltersChange { copy(radiusKm = 2) }
        val results = viewModel.state.value.results
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.distanceKm <= 2.0 })
    }

    @Test
    fun categoryFilterKeepsOnlyMatchingVenues() {
        val viewModel = viewModel()
        viewModel.onFiltersChange { copy(category = CATEGORY_GARDENS) }
        val results = viewModel.state.value.results
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { CATEGORY_GARDENS in it.categories })
    }

    @Test
    fun guestsFilterExcludesSmallerVenues() {
        val viewModel = viewModel()
        viewModel.onFiltersChange { copy(guests = "150") }
        val results = viewModel.state.value.results
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.capacity >= 150 })
        assertTrue(results.none { it.name.contains("Brincol") })
    }

    @Test
    fun budgetFilterExcludesPricierVenues() {
        val viewModel = viewModel()
        viewModel.onFiltersChange { copy(budget = "40000") }
        val results = viewModel.state.value.results
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { (it.priceBaseAmount ?: 0.0) <= 40000.0 })
    }

    @Test
    fun queryMatchesNameAndZoneIgnoringCase() {
        val viewModel = viewModel()
        viewModel.onFiltersChange { copy(query = "JURiquilla") }
        assertEquals(2, viewModel.state.value.results.size)
        viewModel.onFiltersChange { copy(query = "hacienda") }
        val results = viewModel.state.value.results
        assertEquals(1, results.size)
        assertEquals("Hacienda Los Arcos", results.first().name)
    }

    @Test
    fun priceOrderSortsAscending() {
        val viewModel = viewModel()
        viewModel.onFiltersChange { copy(order = ExploreOrder.PRICE) }
        val results = viewModel.state.value.results
        val prices = results.mapNotNull { it.priceBaseAmount }
        assertEquals(prices.sorted(), prices)
    }

    @Test
    fun clearFiltersRestoresFullCatalog() {
        val viewModel = viewModel()
        viewModel.onFiltersChange { copy(query = "zzz-no-existe") }
        val filtered = viewModel.state.value.results
        assertTrue(filtered.isEmpty())
        viewModel.onFiltersClear()
        assertEquals(VenueSamples.size, viewModel.state.value.results.size)
        assertEquals(ExploreFilters(), viewModel.state.value.filters)
    }

    @Test
    fun invalidDateKeepsPanelOpenWithFieldError() {
        val viewModel = viewModel()
        viewModel.onSheetChange(ExploreSheet.FILTERS)
        viewModel.onFiltersChange { copy(date = "2026-12-25") }
        viewModel.onFiltersApply()
        val state = viewModel.state.value
        assertEquals(ExploreFieldError.DATE, state.fieldError)
        assertEquals(ExploreSheet.FILTERS, state.sheet)
    }

    @Test
    fun validDateClosesFiltersPanel() {
        val viewModel = viewModel()
        viewModel.onSheetChange(ExploreSheet.FILTERS)
        viewModel.onFiltersChange { copy(date = "25/12/2026") }
        viewModel.onFiltersApply()
        val state = viewModel.state.value
        assertEquals(ExploreFieldError.NONE, state.fieldError)
        assertEquals(ExploreSheet.NONE, state.sheet)
    }

    @Test
    fun invalidGuestsValueFlagsErrorAndKeepsPanelOpen() {
        val viewModel = viewModel()
        viewModel.onSheetChange(ExploreSheet.FILTERS)
        viewModel.onFiltersChange { copy(guests = "abc") }
        viewModel.onFiltersApply()
        val state = viewModel.state.value
        assertEquals(ExploreFieldError.GUESTS, state.fieldError)
        assertEquals(ExploreSheet.FILTERS, state.sheet)
    }

    @Test
    fun recommendationsAreBoundedAndSortedByFit() {
        val viewModel = viewModel()
        viewModel.onSheetChange(ExploreSheet.RECOMMENDED)
        val recommendations = viewModel.state.value.recommendations
        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.size <= EXPLORE_RECOMMENDATION_LIMIT)
        val percents = recommendations.map { it.fitPercent }
        assertEquals(percents.sortedDescending(), percents)
        assertTrue(percents.all { it in 0..100 })
        assertTrue(recommendations.all { it.reason in ExploreReason.entries })
    }

    @Test
    fun modeChangeSwitchesBetweenMapAndList() {
        val viewModel = viewModel()
        viewModel.onModeChange(ExploreMode.LIST)
        assertEquals(ExploreMode.LIST, viewModel.state.value.mode)
        viewModel.onModeChange(ExploreMode.MAP)
        assertEquals(ExploreMode.MAP, viewModel.state.value.mode)
    }

    @Test
    fun filteringClearsSelectionThatNoLongerMatches() {
        val viewModel = viewModel()
        val results = viewModel.state.value.results
        val farVenue = results.first { it.distanceKm > 5.0 }
        viewModel.onVenueSelect(farVenue.id)
        assertEquals(farVenue.id, viewModel.state.value.selectedVenueId)
        viewModel.onFiltersChange { copy(radiusKm = 2) }
        assertNull(viewModel.state.value.selectedVenue)
        assertEquals(EXPLORE_NO_VENUE, viewModel.state.value.selectedVenueId)
    }

    @Test
    fun sampleFavoriteToggleStaysLocal() {
        val viewModel = viewModel()
        val sample = VenueSamples.first()
        viewModel.onFavoriteToggle(sample)
        assertTrue(sample.venueId in viewModel.state.value.favoriteIds)
        assertEquals(0, favoritesRepository.addCalls)
        viewModel.onFavoriteToggle(sample)
        assertFalse(sample.venueId in viewModel.state.value.favoriteIds)
    }

    @Test
    fun realVenueFavoriteRevertsWhenApiFails() =
        runTest {
            venueRepository.result = AppResult.Success(listOf(realVenue()))
            favoritesRepository.addResult = AppResult.Failure(AppError.Network)
            val viewModel = viewModel()
            val results = viewModel.state.value.results
            val venue = results.first()
            viewModel.events.test {
                viewModel.onFavoriteToggle(venue)
                val event = awaitItem()
                assertTrue(event is ExploreUiEvent.FavoriteError)
            }
            assertFalse(venue.venueId in viewModel.state.value.favoriteIds)
            assertEquals(1, favoritesRepository.addCalls)
        }

    @Test
    fun unreadNoticesAreCounted() {
        noticesRepository.result =
            AppResult.Success(
                listOf(
                    newNotice(id = "1", isRead = false),
                    newNotice(id = "2", isRead = false),
                    newNotice(id = "3", isRead = true),
                ),
            )
        val state = viewModel().state.value
        assertEquals(2, state.unreadNotices)
    }

    @Test
    fun noticesFailureKeepsCountAtZeroWithoutError() {
        noticesRepository.result = AppResult.Failure(AppError.NotFound)
        val state = viewModel().state.value
        assertEquals(0, state.unreadNotices)
        assertFalse(state.hasError)
    }

    @Test
    fun loadFailureShowsErrorAndRetryRecovers() {
        venueRepository.result = AppResult.Failure(AppError.Network)
        val viewModel = viewModel()
        assertTrue(viewModel.state.value.hasError)
        venueRepository.result = AppResult.Success(emptyList())
        viewModel.refresh()
        val state = viewModel.state.value
        assertFalse(state.hasError)
        assertEquals(VenueSamples.size, state.results.size)
    }
}

private class FakeFavoritesRepository : FavoritesRepository {
    var result: AppResult<List<Favorite>> = AppResult.Success(emptyList())
    var addResult: AppResult<Unit> = AppResult.Success(Unit)
    var addCalls: Int = 0
        private set

    override suspend fun getFavorites(): AppResult<List<Favorite>> = result

    override suspend fun addFavorite(
        targetType: String,
        targetId: String,
        name: String?,
        photoUrl: String?,
        rating: Double?,
    ): AppResult<Unit> {
        addCalls += 1
        return addResult
    }

    override suspend fun removeFavorite(
        targetType: String,
        targetId: String,
    ): AppResult<Unit> = AppResult.Success(Unit)
}

private class FakeNoticesRepository : NoticesRepository {
    var result: AppResult<List<Notice>> = AppResult.Success(emptyList())

    override suspend fun getNotices(): AppResult<List<Notice>> = result
}

private fun realVenue(): Venue =
    Venue(
        id = "venue-1",
        name = "Casa Real",
        shortName = "C. Real",
        zone = "Centro",
        capacity = 120,
        ratingAverage = 4.6,
        ratingCount = 12,
        latitude = 20.7,
        longitude = -100.44,
        priceBaseAmount = 30000.0,
        priceUnit = "day",
        categories = listOf(CATEGORY_GARDENS),
    )

private fun newNotice(
    id: String,
    isRead: Boolean,
): Notice =
    Notice(
        id = id,
        type = "booking",
        title = "Aviso $id",
        body = null,
        isRead = isRead,
        createdAt = null,
    )
