package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleFavoriteUseCaseTest {
    private class FakeFavoritesRepository : FavoritesRepository {
        var addCalls = 0
        var removeCalls = 0
        var lastTargetType: String? = null
        var lastTargetId: String? = null
        var lastName: String? = null
        var lastRating: Double? = null
        var result: AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun getFavorites(): AppResult<List<Favorite>> = AppResult.Success(emptyList())

        override suspend fun addFavorite(
            targetType: String,
            targetId: String,
            name: String?,
            photoUrl: String?,
            rating: Double?,
        ): AppResult<Unit> {
            addCalls += 1
            lastTargetType = targetType
            lastTargetId = targetId
            lastName = name
            lastRating = rating
            return result
        }

        override suspend fun removeFavorite(
            targetType: String,
            targetId: String,
        ): AppResult<Unit> {
            removeCalls += 1
            lastTargetType = targetType
            lastTargetId = targetId
            return result
        }
    }

    private val venue =
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
    fun addingFavoritePostsVenueData() =
        runTest {
            val repository = FakeFavoritesRepository()
            val useCase = ToggleFavoriteUseCase(repository)

            val result = useCase(venue, isFavorite = false)

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(1, repository.addCalls)
            assertEquals(0, repository.removeCalls)
            assertEquals("venue", repository.lastTargetType)
            assertEquals("uuid-1", repository.lastTargetId)
            assertEquals("Casa VirGo", repository.lastName)
            assertEquals(4.5, repository.lastRating!!, 0.0)
        }

    @Test
    fun removingFavoriteDeletesByIndexPath() =
        runTest {
            val repository = FakeFavoritesRepository()
            val useCase = ToggleFavoriteUseCase(repository)

            val result = useCase(venue, isFavorite = true)

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(1, repository.removeCalls)
            assertEquals(0, repository.addCalls)
            assertEquals("venue", repository.lastTargetType)
            assertEquals("uuid-1", repository.lastTargetId)
        }

    @Test
    fun unratedVenueSendsNullRating() =
        runTest {
            val repository = FakeFavoritesRepository()
            val useCase = ToggleFavoriteUseCase(repository)

            useCase(venue.copy(ratingAverage = 0.0), isFavorite = false)

            assertEquals(null, repository.lastRating)
        }

    @Test
    fun apiFailureIsPropagatedWithoutRetrying() =
        runTest {
            val repository = FakeFavoritesRepository()
            repository.result = AppResult.Failure(AppError.Network)
            val useCase = ToggleFavoriteUseCase(repository)

            val result = useCase(venue, isFavorite = false)

            assertEquals(AppResult.Failure(AppError.Network), result)
            assertEquals(1, repository.addCalls)
            assertEquals(0, repository.removeCalls)
        }
}
