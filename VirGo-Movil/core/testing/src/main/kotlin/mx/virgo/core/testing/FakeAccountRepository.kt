package mx.virgo.core.testing

import kotlinx.coroutines.CompletableDeferred
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.AccountRepository
import mx.virgo.core.domain.UserProfile

class FakeAccountRepository : AccountRepository {
    var profileResult: AppResult<UserProfile> = AppResult.Success(sampleUserProfile())
    var logoutResult: AppResult<Unit> = AppResult.Success(Unit)

    /** Si se asigna, `logout()` espera a que se complete (pruebas de doble toque y timeout). */
    var logoutGate: CompletableDeferred<Unit>? = null

    var profileCalls: Int = 0
        private set
    var logoutCalls: Int = 0
        private set

    override suspend fun getProfile(): AppResult<UserProfile> {
        profileCalls += 1
        return profileResult
    }

    override suspend fun logout(): AppResult<Unit> {
        logoutCalls += 1
        logoutGate?.await()
        return logoutResult
    }
}

fun sampleUserProfile(role: String = "client"): UserProfile =
    UserProfile(
        id = "00000000-0000-0000-0000-000000000001",
        name = "Ana Lopez",
        email = "ana@mail.com",
        phone = "+525512345678",
        role = role,
        isVerified = true,
    )
