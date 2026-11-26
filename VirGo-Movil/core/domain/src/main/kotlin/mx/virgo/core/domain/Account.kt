package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

/** Perfil público de la cuenta autenticada (`GET /api/v1/users/me`). No se persiste. */
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val isVerified: Boolean,
)

interface AccountRepository {
    suspend fun getProfile(): AppResult<UserProfile>

    /** Revoca la sesión actual en el servidor (`POST /api/v1/auth/logout`). */
    suspend fun logout(): AppResult<Unit>
}

class GetProfileUseCase
    @Inject
    constructor(
        private val repository: AccountRepository,
    ) {
        suspend operator fun invoke(): AppResult<UserProfile> = repository.getProfile()
    }

class LogoutUseCase
    @Inject
    constructor(
        private val repository: AccountRepository,
    ) {
        suspend operator fun invoke(): AppResult<Unit> = repository.logout()
    }
