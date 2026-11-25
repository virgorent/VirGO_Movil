package mx.virgo.core.domain

import java.math.BigDecimal

/** Respuesta del propietario: lo único que acepta el servidor (024 RF-04). */
data class QuoteAnswer(
    val total: BigDecimal,
    val deposit: BigDecimal? = null,
    val message: String = "",
)

enum class AnswerField {
    TOTAL,
    DEPOSIT,
    MESSAGE,
}

enum class AnswerProblem {
    REQUIRED,
    INVALID,
    ABOVE_TOTAL,
    TOO_LONG,
}

/** Convierte lo capturado en una respuesta válida o en errores por campo. */
object QuoteAnswerValidator {
    const val MAX_MESSAGE = 2000
    private const val MAX_DECIMALS = 2

    fun validate(
        total: String,
        deposit: String,
        message: String,
    ): Pair<QuoteAnswer?, Map<AnswerField, AnswerProblem>> {
        val errors = mutableMapOf<AnswerField, AnswerProblem>()
        val totalValue = parseAmount(total)
        when {
            total.isBlank() -> errors[AnswerField.TOTAL] = AnswerProblem.REQUIRED
            totalValue == null || totalValue.signum() <= 0 -> errors[AnswerField.TOTAL] = AnswerProblem.INVALID
        }
        val depositValue = if (deposit.isBlank()) null else parseAmount(deposit)
        when {
            deposit.isBlank() -> Unit
            depositValue == null || depositValue.signum() < 0 -> errors[AnswerField.DEPOSIT] = AnswerProblem.INVALID
            totalValue != null && depositValue > totalValue -> errors[AnswerField.DEPOSIT] = AnswerProblem.ABOVE_TOTAL
        }
        if (message.length > MAX_MESSAGE) errors[AnswerField.MESSAGE] = AnswerProblem.TOO_LONG
        val answer =
            if (errors.isEmpty() &&
                totalValue != null
            ) {
                QuoteAnswer(totalValue, depositValue, message)
            } else {
                null
            }
        return answer to errors
    }

    /** Montos en pesos con punto decimal y a lo más dos decimales; acepta comas de miles. */
    fun parseAmount(text: String): BigDecimal? =
        text
            .trim()
            .replace(",", "")
            .toBigDecimalOrNull()
            ?.takeIf { it.scale() <= MAX_DECIMALS }
}
