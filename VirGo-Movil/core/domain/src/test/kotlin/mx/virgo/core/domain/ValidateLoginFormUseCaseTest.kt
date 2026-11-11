package mx.virgo.core.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateLoginFormUseCaseTest {
    private val validateLogin = ValidateLoginFormUseCase(ValidateEmailUseCase())

    @Test
    fun acceptsEmailAndPassword() {
        assertTrue(validateLogin("user@mail.com", "secret1"))
    }

    @Test
    fun rejectsInvalidEmail() {
        assertFalse(validateLogin("not-an-email", "secret1"))
    }

    @Test
    fun rejectsEmptyPassword() {
        assertFalse(validateLogin("user@mail.com", ""))
    }

    @Test
    fun rejectsBothFieldsEmpty() {
        assertFalse(validateLogin("", ""))
    }

    @Test
    fun passwordValidRequiresNonEmptyValue() {
        assertTrue(validateLogin.passwordValid("x"))
        assertFalse(validateLogin.passwordValid(""))
    }

    @Test
    fun emailValidDelegatesToEmailRule() {
        assertTrue(validateLogin.emailValid("user@mail.com"))
        assertFalse(validateLogin.emailValid("user@mail"))
    }
}
