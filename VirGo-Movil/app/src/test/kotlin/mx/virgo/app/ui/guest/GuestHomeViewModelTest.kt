package mx.virgo.app.ui.guest

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.Venue
import mx.virgo.core.testing.FakeVenueRepository
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GuestHomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeRepository = FakeVenueRepository()

    private fun viewModel() = GuestHomeViewModel(GetVenuesUseCase(fakeRepository))

    @Test
    fun defaultStateShowsFiveVenuesWithinFiveKm() {
        val state = viewModel().state.value
        assertEquals(DEFAULT_RANGE_KM, state.radiusKm)
        assertEquals(5, state.nearby.size)
        assertTrue(state.nearby.all { it.distanceKm <= 5.0 })
        assertNull(state.selectedVenue)
        assertEquals(NO_VENUE, state.selectedVenueId)
    }

    @Test
    fun nearbyIsSortedByDistance() {
        val distances =
            viewModel()
                .state.value.nearby
                .map { it.distanceKm }
        assertEquals(distances.sorted(), distances)
    }

    @Test
    fun selectingVenueShowsPreview() {
        val viewModel = viewModel()
        viewModel.selectVenue(2)
        assertEquals(
            2,
            viewModel.state.value.selectedVenue
                ?.id,
        )
        assertEquals(2, viewModel.state.value.selectedVenueId)
    }

    @Test
    fun shrinkingRadiusClearsSelectionOutOfRange() {
        val viewModel = viewModel()
        viewModel.selectVenue(2)
        viewModel.selectRadius(2)
        assertEquals(2, viewModel.state.value.nearby.size)
        assertNull(viewModel.state.value.selectedVenue)
        assertEquals(NO_VENUE, viewModel.state.value.selectedVenueId)
    }

    @Test
    fun selectionSurvivesRadiusThatKeepsItInRange() {
        val viewModel = viewModel()
        viewModel.selectVenue(0)
        viewModel.selectRadius(2)
        assertEquals(
            0,
            viewModel.state.value.selectedVenue
                ?.id,
        )
    }

    @Test
    fun clearVenueHidesPreview() {
        val viewModel = viewModel()
        viewModel.selectVenue(1)
        viewModel.clearVenue()
        assertNull(viewModel.state.value.selectedVenue)
        assertEquals(NO_VENUE, viewModel.state.value.selectedVenueId)
    }

    @Test
    fun tenKilometerRangeShowsAllSampleVenues() {
        val viewModel = viewModel()
        viewModel.selectRadius(10)
        assertEquals(VenueSamples.size, viewModel.state.value.nearby.size)
    }

    @Test
    fun catalogResponseReplacesSampleVenues() {
        fakeRepository.result = AppResult.Success(listOf(venueNearCenter("Salon Real")))
        val state = viewModel().state.value
        assertEquals(1, state.nearby.size)
        assertEquals("Salon Real", state.nearby.first().name)
        assertFalse(state.hasError)
        assertFalse(state.isLoading)
        assertEquals(1, fakeRepository.calls)
    }

    @Test
    fun emptyCatalogFallsBackToSampleVenues() {
        fakeRepository.result = AppResult.Success(emptyList())
        val state = viewModel().state.value
        assertEquals(VenueSamples.count { it.distanceKm <= DEFAULT_RANGE_KM }, state.nearby.size)
        assertFalse(state.hasError)
    }

    @Test
    fun failedLoadShowsErrorAndKeepsSampleVenues() {
        fakeRepository.result = AppResult.Failure(AppError.Network)
        val state = viewModel().state.value
        assertTrue(state.hasError)
        assertFalse(state.isLoading)
        assertEquals(VenueSamples.count { it.distanceKm <= DEFAULT_RANGE_KM }, state.nearby.size)
    }

    @Test
    fun retryAfterFailureClearsError() {
        fakeRepository.result = AppResult.Failure(AppError.Network)
        val viewModel = viewModel()
        assertTrue(viewModel.state.value.hasError)
        fakeRepository.result = AppResult.Success(emptyList())
        viewModel.refresh()
        assertFalse(viewModel.state.value.hasError)
        assertEquals(2, fakeRepository.calls)
    }

    @Test
    fun venuesOutsideRadiusAreFilteredOut() {
        fakeRepository.result =
            AppResult.Success(
                listOf(
                    venueNearCenter("Cerca"),
                    venue(
                        id = "uuid-far",
                        name = "Lejos",
                        latitude = GUEST_CENTER_LAT + 1.0,
                        longitude = GUEST_CENTER_LNG,
                    ),
                ),
            )
        val state = viewModel().state.value
        assertEquals(1, state.nearby.size)
        assertEquals("Cerca", state.nearby.first().name)
    }

    @Test
    fun venueWithoutLocationIsNotPlacedOnMap() {
        fakeRepository.result = AppResult.Success(listOf(venue(id = "uuid-1", name = "Sin Mapa")))
        val state = viewModel().state.value
        assertTrue(state.nearby.isEmpty())
        assertFalse(state.hasError)
    }

    private fun venue(
        id: String,
        name: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ) = Venue(
        id = id,
        name = name,
        shortName = null,
        zone = "Centro",
        capacity = 120,
        ratingAverage = 4.5,
        ratingCount = 10,
        latitude = latitude,
        longitude = longitude,
    )

    private fun venueNearCenter(name: String) =
        venue(
            id = "uuid-near",
            name = name,
            latitude = GUEST_CENTER_LAT + 0.001,
            longitude = GUEST_CENTER_LNG + 0.001,
        )
}
