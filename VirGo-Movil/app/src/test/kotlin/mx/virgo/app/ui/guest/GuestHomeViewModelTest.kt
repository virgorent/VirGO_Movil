package mx.virgo.app.ui.guest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuestHomeViewModelTest {
    @Test
    fun defaultStateShowsFiveVenuesWithinFiveKm() {
        val state = GuestHomeViewModel().state.value
        assertEquals(DEFAULT_RANGE_KM, state.radiusKm)
        assertEquals(5, state.nearby.size)
        assertTrue(state.nearby.all { it.distanceKm <= 5.0 })
        assertNull(state.selectedVenue)
        assertEquals(NO_VENUE, state.selectedVenueId)
    }

    @Test
    fun nearbyIsSortedByDistance() {
        val distances =
            GuestHomeViewModel()
                .state.value.nearby
                .map { it.distanceKm }
        assertEquals(distances.sorted(), distances)
    }

    @Test
    fun selectingVenueShowsPreview() {
        val viewModel = GuestHomeViewModel()
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
        val viewModel = GuestHomeViewModel()
        viewModel.selectVenue(2)
        viewModel.selectRadius(2)
        assertEquals(2, viewModel.state.value.nearby.size)
        assertNull(viewModel.state.value.selectedVenue)
        assertEquals(NO_VENUE, viewModel.state.value.selectedVenueId)
    }

    @Test
    fun selectionSurvivesRadiusThatKeepsItInRange() {
        val viewModel = GuestHomeViewModel()
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
        val viewModel = GuestHomeViewModel()
        viewModel.selectVenue(1)
        viewModel.clearVenue()
        assertNull(viewModel.state.value.selectedVenue)
        assertEquals(NO_VENUE, viewModel.state.value.selectedVenueId)
    }

    @Test
    fun tenKilometerRangeShowsAllSampleVenues() {
        val viewModel = GuestHomeViewModel()
        viewModel.selectRadius(10)
        assertEquals(VenueSamples.size, viewModel.state.value.nearby.size)
    }
}
