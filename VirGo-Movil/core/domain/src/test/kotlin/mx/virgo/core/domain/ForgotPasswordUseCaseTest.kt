package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ForgotPasswordUseCaseTest {
    private class FakeRecoveryRepository(
        var result: AppResult<Unit> = AppResult.Success(Unit),
    ) : RecoveryRepository {
        var calls = 0
        var lastEmail: String? = null

        override suspend fun forgotPassword(email: String): AppResult<Unit> {
            calls += 1
            lastEmail = email
            return result
        }

        override suspend fun resetPassword(
            code: String,
            password: String,
        ): AppResult<Unit> = result
    }

    @Test
    fun invokeDelegatesToRepositoryWithEmail() =
        runTest {
            val repository = FakeRecoveryRepository()
            val useCase = ForgotPasswordUseCase(repository)

            val result = useCase("ana@ejemplo.mx")

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(1, repository.calls)
            assertEquals("ana@ejemplo.mx", repository.lastEmail)
        }

    @Test
    fun invokePropagatesRepositoryFailure() =
        runTest {
            val repository = FakeRecoveryRepository(result = AppResult.Failure(AppError.Network))
            val useCase = ForgotPasswordUseCase(repository)

            val result = useCase("ana@ejemplo.mx")

            assertEquals(AppResult.Failure(AppError.Network), result)
            assertEquals(1, repository.calls)
        }
}
