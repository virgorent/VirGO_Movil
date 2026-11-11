package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

class LoginUseCase
    @Inject
    constructor(
        private val repository: AuthRepository,
    ) {
        suspend operator fun invoke(
            email: String,
            password: String,
            device: String? = null,
        ): AppResult<AuthSession> = repository.login(email, password, device)
    }
