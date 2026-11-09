package mx.virgo.core.domain

import mx.virgo.core.common.AppResult

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
        device: String? = null,
    ): AppResult<AuthSession>
}
