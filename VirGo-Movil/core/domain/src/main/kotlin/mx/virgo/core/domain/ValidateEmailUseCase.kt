package mx.virgo.core.domain

import javax.inject.Inject

private val EmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

class ValidateEmailUseCase
    @Inject
    constructor() {
        operator fun invoke(email: String): Boolean = EmailPattern.matches(email.trim())
    }
