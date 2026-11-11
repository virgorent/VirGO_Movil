package mx.virgo.core.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateRegisterFormUseCaseTest {
    private val validate = ValidateRegisterFormUseCase(ValidateEmailUseCase())

    private val validName = "Ana Lopez"
    private val validEmail = "ana@mail.com"
    private val validPhone = "5512345678"
    private val validPassword = "password1"

    @Test
    fun acceptsCompleteForm() {
        assertTrue(validate(validName, validEmail, validPhone, validPassword))
    }

    @Test
    fun rejectsShortName() {
        assertFalse(validate("An", validEmail, validPhone, validPassword))
    }

    @Test
    fun nameValidTrimsBeforeCheckingLength() {
        assertTrue(validate.nameValid("  Ana  "))
        assertFalse(validate.nameValid(" An "))
    }

    @Test
    fun rejectsInvalidEmail() {
        assertFalse(validate(validName, "ana@mail", validPhone, validPassword))
    }

    @Test
    fun rejectsPhoneShorterThanTenDigits() {
        assertFalse(validate(validName, validEmail, "551234567", validPassword))
    }

    @Test
    fun rejectsPasswordShorterThanEightChars() {
        assertFalse(validate(validName, validEmail, validPhone, "pass123"))
    }

    @Test
    fun phoneValidRequiresExactLength() {
        assertTrue(validate.phoneValid("5512345678"))
        assertFalse(validate.phoneValid("551234567"))
        assertFalse(validate.phoneValid("55123456789"))
    }

    @Test
    fun passwordValidRequiresEightCharacters() {
        assertTrue(validate.passwordValid("12345678"))
        assertFalse(validate.passwordValid("1234567"))
    }
}
