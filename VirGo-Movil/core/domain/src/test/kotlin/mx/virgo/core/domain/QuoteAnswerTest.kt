package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class QuoteAnswerTest {
    @Test
    fun validAnswerIsBuilt() {
        val (answer, errors) = QuoteAnswerValidator.validate("48,000.50", "14400", "Incluye limpieza")
        assertTrue(errors.isEmpty())
        assertEquals(0, BigDecimal("48000.50").compareTo(answer!!.total))
        assertEquals(0, BigDecimal("14400").compareTo(answer.deposit))
        assertEquals("Incluye limpieza", answer.message)
        assertNull(QuoteAnswerValidator.validate("1", "", "").first!!.deposit)
        assertTrue(QuoteAnswerValidator.validate("100", "100", "a".repeat(2000)).second.isEmpty())
        assertTrue(QuoteAnswerValidator.validate("100", "0", "").second.isEmpty())
    }

    @Test
    fun totalIsRequiredAndPositive() {
        assertEquals(AnswerProblem.REQUIRED, QuoteAnswerValidator.validate(" ", "", "").second[AnswerField.TOTAL])
        assertEquals(AnswerProblem.INVALID, QuoteAnswerValidator.validate("0", "", "").second[AnswerField.TOTAL])
        assertEquals(AnswerProblem.INVALID, QuoteAnswerValidator.validate("-5", "", "").second[AnswerField.TOTAL])
        assertEquals(AnswerProblem.INVALID, QuoteAnswerValidator.validate("abc", "", "").second[AnswerField.TOTAL])
        assertEquals(AnswerProblem.INVALID, QuoteAnswerValidator.validate("10.555", "", "").second[AnswerField.TOTAL])
        assertNull(QuoteAnswerValidator.validate("0", "", "").first)
    }

    @Test
    fun depositAndMessageAreChecked() {
        assertEquals(
            AnswerProblem.ABOVE_TOTAL,
            QuoteAnswerValidator.validate("100", "100.01", "").second[AnswerField.DEPOSIT],
        )
        assertEquals(AnswerProblem.INVALID, QuoteAnswerValidator.validate("100", "-1", "").second[AnswerField.DEPOSIT])
        assertEquals(AnswerProblem.INVALID, QuoteAnswerValidator.validate("100", "x", "").second[AnswerField.DEPOSIT])
        assertEquals(
            AnswerProblem.TOO_LONG,
            QuoteAnswerValidator.validate("100", "", "a".repeat(2001)).second[AnswerField.MESSAGE],
        )
        assertNull(QuoteAnswerValidator.validate("x", "5", "").second[AnswerField.DEPOSIT])
    }

    @Test
    fun respondDelegates() =
        runTest {
            val seen = mutableListOf<String>()
            val repository =
                object : QuotesRepository {
                    override suspend fun create(request: QuoteRequest) = AppResult.Failure(AppError.NotFound)

                    override suspend fun list() = AppResult.Failure(AppError.NotFound)

                    override suspend fun get(id: String) = AppResult.Failure(AppError.NotFound)

                    override suspend fun respond(
                        id: String,
                        answer: QuoteAnswer,
                    ): AppResult<Quote> {
                        seen += id
                        return AppResult.Failure(AppError.Conflict)
                    }
                }
            val result = RespondQuoteUseCase(repository)("q1", QuoteAnswer(BigDecimal.TEN))
            assertEquals(AppResult.Failure(AppError.Conflict), result)
            assertEquals(listOf("q1"), seen)
        }
}
