package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Test

class ResetPasswordUseCaseTest {
    private class FakeRecoveryRepository(
        var result: AppResult<Unit> = AppResult.Success(Unit),
    ) : RecoveryRepository {
        var calls = 0
        var lastCode: String? = null
        var lastPassword: String? = null

        override suspend fun forgotPassword(email: String): AppResult<Unit> = result

        override suspend fun resetPassword(
            code: String,
            password: String,
        ): AppResult<Unit> {
            calls += 1
            lastCode = code
            lastPassword = password
            return result
        }
    }

    @Test
    fun invokeDelegatesToRepositoryWithCodeAndPassword() =
        runTest {
            val repository = FakeRecoveryRepository()
            val useCase = ResetPasswordUseCase(repository)

            val result = useCase("123456", "password1")

            assertEquals(AppResult.Success(Unit), result)
            assertEquals(1, repository.calls)
            assertEquals("123456", repository.lastCode)
            assertEquals("password1", repository.lastPassword)
        }

    @Test
    fun invokePropagatesRepositoryFailure() =
        runTest {
            val repository = FakeRecoveryRepository(result = AppResult.Failure(AppError.Validation(emptyMap())))
            val useCase = ResetPasswordUseCase(repository)

            val result = useCase("000000", "password1")

            assertEquals(AppResult.Failure(AppError.Validation(emptyMap())), result)
            assertEquals(1, repository.calls)
        }
}
