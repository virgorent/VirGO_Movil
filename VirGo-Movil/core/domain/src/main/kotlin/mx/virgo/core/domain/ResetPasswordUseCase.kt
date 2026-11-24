package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class ResetPasswordUseCase
    @Inject
    constructor(
        private val repository: RecoveryRepository,
    ) {
        suspend operator fun invoke(
            code: String,
            password: String,
        ): AppResult<Unit> = repository.resetPassword(code, password)
    }
