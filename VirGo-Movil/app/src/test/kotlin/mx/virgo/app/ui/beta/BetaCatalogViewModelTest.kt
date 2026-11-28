package mx.virgo.app.ui.beta

import kotlinx.coroutines.CompletableDeferred
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.GetVenuesUseCase
import mx.virgo.core.domain.Venue
import mx.virgo.core.domain.VenueRepository
import mx.virgo.core.testing.FakeVenueRepository
import mx.virgo.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BetaCatalogViewModelTest {
    @get:Rule
    val dispatcher = MainDispatcherRule()

    @Test
    fun emptyCatalogDoesNotCreateSamples() {
        val repository = FakeVenueRepository()
        val model = BetaCatalogViewModel(GetVenuesUseCase(repository))
        assertTrue(
            model.state.value.venues
                .isEmpty(),
        )
        assertFalse(model.state.value.loading)
        assertFalse(model.state.value.failed)
        assertEquals(1, repository.calls)
    }

    @Test
    fun failureIsDistinctFromEmptyAndRetryCanRecover() {
        val repository = FakeVenueRepository().apply { result = AppResult.Failure(AppError.Network) }
        val model = BetaCatalogViewModel(GetVenuesUseCase(repository))
        assertTrue(model.state.value.failed)
        assertTrue(
            model.state.value.venues
                .isEmpty(),
        )
        repository.result = AppResult.Success(emptyList())
        model.refresh()
        assertFalse(model.state.value.failed)
        assertEquals(2, repository.calls)
    }

    @Test
    fun catalogKeepsRealVenuesEvenWithoutCoordinates() {
        val venue = Venue("real-id", "Real venue", null, null, 100, 0.0, 0, null, null)
        val repository = FakeVenueRepository().apply { result = AppResult.Success(listOf(venue)) }
        val model = BetaCatalogViewModel(GetVenuesUseCase(repository))
        assertEquals(listOf(venue), model.state.value.venues)
    }

    @Test
    fun loadingStaysDistinctAndDuplicateRefreshDoesNotStartAnotherRequest() {
        val result = CompletableDeferred<AppResult<List<Venue>>>()
        var calls = 0
        val repository =
            object : VenueRepository {
                override suspend fun getVenues(
                    zone: String?,
                    category: String?,
                ): AppResult<List<Venue>> {
                    calls++
                    return result.await()
                }
            }
        val model = BetaCatalogViewModel(GetVenuesUseCase(repository))
        assertTrue(model.state.value.loading)
        assertFalse(model.state.value.failed)
        model.refresh()
        assertEquals(1, calls)
        result.complete(AppResult.Success(emptyList()))
        assertFalse(model.state.value.loading)
        assertTrue(
            model.state.value.venues
                .isEmpty(),
        )
    }
}
