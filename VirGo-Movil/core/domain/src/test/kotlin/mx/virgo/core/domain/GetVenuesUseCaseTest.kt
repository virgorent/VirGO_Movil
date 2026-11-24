package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Test

class GetVenuesUseCaseTest {
    private class FakeVenueRepository(
        var result: AppResult<List<Venue>> = AppResult.Success(emptyList()),
    ) : VenueRepository {
        var calls = 0
        var lastZone: String? = null
        var lastCategory: String? = null

        override suspend fun getVenues(
            zone: String?,
            category: String?,
        ): AppResult<List<Venue>> {
            calls += 1
            lastZone = zone
            lastCategory = category
            return result
        }
    }

    private val sampleVenue =
        Venue(
            id = "uuid-1",
            name = "Casa VirGo",
            shortName = "Casa",
            zone = "Centro",
            capacity = 150,
            ratingAverage = 4.5,
            ratingCount = 12,
            latitude = 20.7001,
            longitude = -100.4455,
        )

    @Test
    fun invokeDelegatesToRepositoryWithFilters() =
        runTest {
            val repository = FakeVenueRepository(result = AppResult.Success(listOf(sampleVenue)))
            val useCase = GetVenuesUseCase(repository)

            val result = useCase(zone = "Centro", category = "Elegante")

            assertEquals(AppResult.Success(listOf(sampleVenue)), result)
            assertEquals(1, repository.calls)
            assertEquals("Centro", repository.lastZone)
            assertEquals("Elegante", repository.lastCategory)
        }

    @Test
    fun invokeWithoutFiltersSendsNulls() =
        runTest {
            val repository = FakeVenueRepository()
            val useCase = GetVenuesUseCase(repository)

            useCase()

            assertEquals(1, repository.calls)
            assertEquals(null, repository.lastZone)
            assertEquals(null, repository.lastCategory)
        }

    @Test
    fun invokePropagatesRepositoryFailure() =
        runTest {
            val repository = FakeVenueRepository(result = AppResult.Failure(AppError.Network))
            val useCase = GetVenuesUseCase(repository)

            val result = useCase()

            assertEquals(AppResult.Failure(AppError.Network), result)
            assertEquals(1, repository.calls)
        }
}
