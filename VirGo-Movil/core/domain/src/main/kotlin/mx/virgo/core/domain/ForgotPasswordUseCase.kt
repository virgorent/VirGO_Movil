package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class ForgotPasswordUseCase
    @Inject
    constructor(
        private val repository: RecoveryRepository,
    ) {
        suspend operator fun invoke(email: String): AppResult<Unit> = repository.forgotPassword(email)
    }
