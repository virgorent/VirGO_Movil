package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AuthRepository
import mx.virgo.core.domain.AuthSession
import mx.virgo.core.domain.LoggedInUser
import mx.virgo.core.domain.RegisteredUser

class FakeAuthRepository : AuthRepository {
    var result: AppResult<AuthSession> = AppResult.Success(sampleAuthSession())

    var registerResult: AppResult<RegisteredUser> = AppResult.Success(sampleRegisteredUser())

    var calls: Int = 0
        private set

    var registerCalls: Int = 0
        private set

    var lastRegisterName: String = ""
        private set

    var lastRegisterEmail: String = ""
        private set

    var lastRegisterPhone: String = ""
        private set

    var lastRegisterPassword: String = ""
        private set

    override suspend fun login(
        email: String,
        password: String,
        device: String?,
    ): AppResult<AuthSession> {
        calls += 1
        return result
    }

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String,
    ): AppResult<RegisteredUser> {
        registerCalls += 1
        lastRegisterName = name
        lastRegisterEmail = email
        lastRegisterPhone = phone
        lastRegisterPassword = password
        return registerResult
    }
}

private const val SAMPLE_EXPIRES_IN = 900L

fun sampleAuthSession(): AuthSession =
    AuthSession(
        accessToken = "sample-access-token",
        tokenType = "Bearer",
        expiresIn = SAMPLE_EXPIRES_IN,
        refreshToken = "sample-refresh-token",
        refreshTokenExpiresAt = "2026-12-03T18:00:00Z",
        user =
            LoggedInUser(
                id = "00000000-0000-0000-0000-000000000001",
                name = "Ana Lopez",
                email = "ana@mail.com",
                role = "client",
            ),
    )

fun sampleRegisteredUser(): RegisteredUser =
    RegisteredUser(
        id = "00000000-0000-0000-0000-000000000002",
        name = "Ana Lopez",
        email = "ana@mail.com",
        phone = "+525512345678",
        role = "client",
        isVerified = false,
    )
