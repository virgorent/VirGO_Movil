package mx.virgo.core.domain

import javax.inject.Inject

class ValidateLoginFormUseCase
    @Inject
    constructor(
        private val validateEmail: ValidateEmailUseCase,
    ) {
        fun emailValid(email: String): Boolean = validateEmail(email)

        fun passwordValid(password: String): Boolean = password.isNotEmpty()

        operator fun invoke(
            email: String,
            password: String,
        ): Boolean = emailValid(email) && passwordValid(password)
    }
