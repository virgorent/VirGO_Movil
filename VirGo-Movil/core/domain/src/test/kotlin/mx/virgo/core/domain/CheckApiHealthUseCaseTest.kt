package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckApiHealthUseCaseTest {
    private class FakeHealthRepository(
        var result: AppResult<ApiHealth> = AppResult.Success(ApiHealth(status = "Healthy", latencyMs = 12)),
    ) : HealthRepository {
        var calls = 0

        override suspend fun checkHealth(): AppResult<ApiHealth> {
            calls += 1
            return result
        }
    }

    @Test
    fun invokeDelegatesToRepository() =
        runTest {
            val repository = FakeHealthRepository()
            val useCase = CheckApiHealthUseCase(repository)

            val result = useCase()

            assertEquals(AppResult.Success(ApiHealth(status = "Healthy", latencyMs = 12)), result)
            assertEquals(1, repository.calls)
        }

    @Test
    fun invokePropagatesRepositoryFailure() =
        runTest {
            val repository = FakeHealthRepository(result = AppResult.Failure(AppError.Network))
            val useCase = CheckApiHealthUseCase(repository)

            val result = useCase()

            assertEquals(AppResult.Failure(AppError.Network), result)
            assertEquals(1, repository.calls)
        }
}
