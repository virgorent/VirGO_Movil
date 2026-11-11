package mx.virgo.core.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateEmailUseCaseTest {
    private val validateEmail = ValidateEmailUseCase()

    @Test
    fun acceptsValidEmail() {
        assertTrue(validateEmail("user@mail.com"))
    }

    @Test
    fun trimsSurroundingSpaces() {
        assertTrue(validateEmail("  user@mail.com  "))
    }

    @Test
    fun rejectsMissingAtSign() {
        assertFalse(validateEmail("usermail.com"))
    }

    @Test
    fun rejectsMissingDomainDot() {
        assertFalse(validateEmail("user@mail"))
    }

    @Test
    fun rejectsBlankEmail() {
        assertFalse(validateEmail("   "))
    }
}
