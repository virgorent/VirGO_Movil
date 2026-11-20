package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFavoritesUseCaseTest {
    private class FakeFavoritesRepository(
        var result: AppResult<List<Favorite>> = AppResult.Success(emptyList()),
    ) : FavoritesRepository {
        var calls = 0

        override suspend fun getFavorites(): AppResult<List<Favorite>> {
            calls += 1
            return result
        }

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

    private val favorite =
        Favorite(
            targetType = "venue",
            targetId = "uuid-1",
            name = "Casa VirGo",
            photoUrl = "/api/v1/venues.png",
            rating = 4.5,
        )

    @Test
    fun invokeReturnsTheRepositoryFavorites() =
        runTest {
            val repository = FakeFavoritesRepository(result = AppResult.Success(listOf(favorite)))
            val useCase = GetFavoritesUseCase(repository)

            val result = useCase()

            assertEquals(AppResult.Success(listOf(favorite)), result)
            assertEquals(1, repository.calls)
        }

    @Test
    fun invokePropagatesRepositoryFailure() =
        runTest {
            val repository = FakeFavoritesRepository(result = AppResult.Failure(AppError.Network))
            val useCase = GetFavoritesUseCase(repository)

            val result = useCase()

            assertEquals(AppResult.Failure(AppError.Network), result)
        }

    @Test
    fun favoriteComparesAllFields() {
        val other =
            Favorite(
                targetType = "venue",
                targetId = "uuid-1",
                name = "Casa VirGo",
                photoUrl = "/api/v1/venues.png",
                rating = 4.5,
            )

        assertEquals(favorite, other)
        assertEquals(favorite.hashCode(), other.hashCode())
        assertEquals(false, favorite == other.copy(targetId = "uuid-2"))
        assertEquals(true, favorite.copy(rating = null).rating == null)
    }
}
