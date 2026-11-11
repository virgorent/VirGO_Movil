package mx.virgo.core.domain

import javax.inject.Inject

const val PHONE_LENGTH = 10

private const val MIN_NAME_LENGTH = 3
private const val MIN_PASSWORD_LENGTH = 8

class ValidateRegisterFormUseCase
    @Inject
    constructor(
        private val validateEmail: ValidateEmailUseCase,
    ) {
        fun nameValid(name: String): Boolean = name.trim().length >= MIN_NAME_LENGTH

        fun emailValid(email: String): Boolean = validateEmail(email)

        fun phoneValid(phone: String): Boolean = phone.length == PHONE_LENGTH

        fun passwordValid(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

        operator fun invoke(
            name: String,
            email: String,
            phone: String,
            password: String,
        ): Boolean = nameValid(name) && emailValid(email) && phoneValid(phone) && passwordValid(password)
    }
