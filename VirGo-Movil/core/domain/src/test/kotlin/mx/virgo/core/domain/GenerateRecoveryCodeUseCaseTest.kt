package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateRecoveryCodeUseCaseTest {
    private val generateCode = GenerateRecoveryCodeUseCase()

    @Test
    fun generatesSixDigitNumericCodes() {
        repeat(50) {
            val code = generateCode()
            assertEquals(6, code.length)
            assertTrue(code.all(Char::isDigit))
            assertTrue(code.toInt() in 100_000..999_999)
        }
    }
}
