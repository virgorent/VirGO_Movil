package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateRecoveryUseCaseTest {
    private val validate = ValidateRecoveryUseCase()

    private val now = 1_700_000_000_000L
    private val code = "123456"
    private val password = "password1"

    @Test
    fun returnsNullWhenEverythingIsValid() {
        assertNull(validate(code, code, now - 1_000, now, password, password))
    }

    @Test
    fun expiresAfterFifteenMinutes() {
        val sentAt = now - 15 * 60 * 1_000 - 1
        assertEquals(RecoveryError.EXPIRED, validate(code, code, sentAt, now, password, password))
    }

    @Test
    fun stillValidAtExactlyFifteenMinutes() {
        val sentAt = now - 15 * 60 * 1_000
        assertNull(validate(code, code, sentAt, now, password, password))
    }

    @Test
    fun expiryTakesPrecedenceOverWrongCode() {
        val sentAt = now - 16 * 60 * 1_000
        assertEquals(
            RecoveryError.EXPIRED,
            validate(code, "999999", sentAt, now, password, password),
        )
    }

    @Test
    fun rejectsWrongCode() {
        assertEquals(
            RecoveryError.WRONG_CODE,
            validate(code, "999999", now - 1_000, now, password, password),
        )
    }

    @Test
    fun rejectsShortPassword() {
        assertEquals(
            RecoveryError.SHORT_PASSWORD,
            validate(code, code, now - 1_000, now, "pass123", "pass123"),
        )
    }

    @Test
    fun rejectsMismatchedConfirmation() {
        assertEquals(
            RecoveryError.MISMATCH,
            validate(code, code, now - 1_000, now, password, "password2"),
        )
    }
}
