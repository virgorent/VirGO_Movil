package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.RecoveryRepository

class FakeRecoveryRepository : RecoveryRepository {
    var forgotResult: AppResult<Unit> = AppResult.Success(Unit)

    var resetResult: AppResult<Unit> = AppResult.Success(Unit)

    var forgotCalls: Int = 0
        private set

    var resetCalls: Int = 0
        private set

    var lastEmail: String? = null
        private set

    var lastCode: String? = null
        private set

    var lastPassword: String? = null
        private set

    override suspend fun forgotPassword(email: String): AppResult<Unit> {
        forgotCalls += 1
        lastEmail = email
        return forgotResult
    }

    override suspend fun resetPassword(
        code: String,
        password: String,
    ): AppResult<Unit> {
        resetCalls += 1
        lastCode = code
        lastPassword = password
        return resetResult
    }
}
