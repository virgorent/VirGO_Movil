package mx.virgo.core.domain

import mx.virgo.core.common.AppResult

interface RecoveryRepository {
    suspend fun forgotPassword(email: String): AppResult<Unit>

    suspend fun resetPassword(
        code: String,
        password: String,
    ): AppResult<Unit>
}
