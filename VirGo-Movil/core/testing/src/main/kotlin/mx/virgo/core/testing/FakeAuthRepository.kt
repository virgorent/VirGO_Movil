package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AuthRepository
import mx.virgo.core.domain.AuthSession
import mx.virgo.core.domain.LoggedInUser

class FakeAuthRepository : AuthRepository {
    var result: AppResult<AuthSession> = AppResult.Success(sampleAuthSession())

    var calls: Int = 0
        private set

    override suspend fun login(
        email: String,
        password: String,
        device: String?,
    ): AppResult<AuthSession> {
        calls += 1
        return result
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
