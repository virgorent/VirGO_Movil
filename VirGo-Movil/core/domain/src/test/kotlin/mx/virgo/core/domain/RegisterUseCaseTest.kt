package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterUseCaseTest {
    private class FakeRepository : AuthRepository {
        var registerResult: AppResult<RegisteredUser> =
            AppResult.Success(
                RegisteredUser(
                    id = "id-1",
                    name = "Ana Lopez",
                    email = "ana@ejemplo.mx",
                    phone = "+525512345678",
                    role = "client",
                    isVerified = false,
                ),
            )

        var registerCalls: Int = 0
            private set

        var lastName: String = ""
            private set

        var lastEmail: String = ""
            private set

        var lastPhone: String = ""
            private set

        var lastPassword: String = ""
            private set

        override suspend fun login(
            email: String,
            password: String,
            device: String?,
        ): AppResult<AuthSession> = AppResult.Failure(AppError.Unknown)

        override suspend fun register(
            name: String,
            email: String,
            phone: String,
            password: String,
        ): AppResult<RegisteredUser> {
            registerCalls += 1
            lastName = name
            lastEmail = email
            lastPhone = phone
            lastPassword = password
            return registerResult
        }
    }

    private val repository = FakeRepository()

    private val useCase = RegisterUseCase(repository)

    @Test
    fun trimsAndLowercasesEmailAndAddsCountryCodeToPhone() =
        runTest {
            val result = useCase("  Ana Lopez  ", "  ANA@Ejemplo.MX ", "5512345678", "secret1")

            assertTrue(result is AppResult.Success)
            assertEquals(1, repository.registerCalls)
            assertEquals("Ana Lopez", repository.lastName)
            assertEquals("ana@ejemplo.mx", repository.lastEmail)
            assertEquals("+525512345678", repository.lastPhone)
            assertEquals("secret1", repository.lastPassword)
        }

    @Test
    fun failureIsPropagatedUnchanged() =
        runTest {
            repository.registerResult = AppResult.Failure(AppError.Conflict)

            val result = useCase("Ana Lopez", "dup@mail.com", "5512345678", "secret1")

            assertEquals(AppError.Conflict, (result as AppResult.Failure).error)
        }
}
