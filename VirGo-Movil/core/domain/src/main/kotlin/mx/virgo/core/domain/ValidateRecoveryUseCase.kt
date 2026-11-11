package mx.virgo.core.domain

import javax.inject.Inject

const val CODE_MINUTES = 15
const val CODE_LENGTH = 6

private const val MIN_PASSWORD_LENGTH = 8
private const val CODE_VALID_MILLIS = CODE_MINUTES * 60L * 1000L

class ValidateRecoveryUseCase
    @Inject
    constructor() {
        operator fun invoke(
            expectedCode: String,
            enteredCode: String,
            sentAtMillis: Long,
            nowMillis: Long,
            password: String,
            confirm: String,
        ): RecoveryError? =
            when {
                nowMillis - sentAtMillis > CODE_VALID_MILLIS -> RecoveryError.EXPIRED
                enteredCode != expectedCode -> RecoveryError.WRONG_CODE
                password.length < MIN_PASSWORD_LENGTH -> RecoveryError.SHORT_PASSWORD
                password != confirm -> RecoveryError.MISMATCH
                else -> null
            }
    }
