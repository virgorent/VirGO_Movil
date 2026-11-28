package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.inject.Inject

/** Categorías del presupuesto (prototipo `providerTypes`); son las llaves de `events.budgetPlan`. */
enum class BudgetCategory(
    val wire: String,
) {
    VENUE("Salón"),
    CATERING("Catering"),
    DECORATION("Decoración"),
    MUSIC("Música"),
    PHOTOGRAPHY("Fotografía"),
    OTHER("Otro"),
    ;

    companion object {
        fun fromWire(value: String): BudgetCategory? = entries.firstOrNull { it.wire == value }
    }
}

/** Preferencias del presupuesto sugerido (`015` RF-05) con los ajustes del prototipo, en puntos porcentuales. */
@Suppress("MagicNumber") // Ajustes copiados del prototipo; cambiarlos es una decisión de producto.
enum class BudgetPreference(
    val deltas: Map<BudgetCategory, Int>,
) {
    PREMIUM_FOOD(mapOf(BudgetCategory.CATERING to 5, BudgetCategory.DECORATION to -3, BudgetCategory.OTHER to -2)),
    LOTS_OF_DECOR(mapOf(BudgetCategory.DECORATION to 6, BudgetCategory.VENUE to -3, BudgetCategory.OTHER to -3)),
    LIVE_MUSIC(mapOf(BudgetCategory.MUSIC to 6, BudgetCategory.CATERING to -3, BudgetCategory.OTHER to -3)),
    SAVE_ON_VENUE(mapOf(BudgetCategory.VENUE to -8, BudgetCategory.CATERING to 4, BudgetCategory.DECORATION to 4)),
}

object BudgetRules {
    private val baseSplit: Map<QuoteEventType, List<Int>> =
        mapOf(
            QuoteEventType.WEDDING to listOf(30, 35, 12, 8, 8, 7),
            QuoteEventType.QUINCEANERA to listOf(28, 32, 15, 12, 7, 6),
            QuoteEventType.BIRTHDAY to listOf(30, 35, 10, 15, 0, 10),
            QuoteEventType.CORPORATE to listOf(35, 40, 5, 5, 5, 10),
            QuoteEventType.OTHER to listOf(30, 35, 10, 10, 5, 10),
        )
    private val ROUND = BigDecimal(100)
    private const val WARN_PERCENT = 90
    private val HUNDRED = BigDecimal(100)

    /** Reparto sugerido (`015` RF-04 y RF-05), redondeado a centenas como el prototipo. */
    fun suggest(
        type: QuoteEventType?,
        budget: BigDecimal,
        preferences: Set<BudgetPreference>,
    ): Map<BudgetCategory, BigDecimal> {
        val percents =
            BudgetCategory.entries
                .zip(baseSplit.getValue(type ?: QuoteEventType.OTHER))
                .associate { (category, base) ->
                    category to maxOf(0, base + preferences.sumOf { it.deltas[category] ?: 0 })
                }
        val sum = percents.values.sum().coerceAtLeast(1)
        return percents.mapValues { (_, percent) ->
            budget
                .multiply(BigDecimal(percent))
                .divide(BigDecimal(sum).multiply(ROUND), 0, RoundingMode.HALF_UP)
                .multiply(ROUND)
        }
    }

    fun overPlanned(
        plan: Map<BudgetCategory, BigDecimal>,
        budget: BigDecimal,
    ): BigDecimal? =
        plan.values
            .fold(BigDecimal.ZERO, BigDecimal::add)
            .subtract(budget)
            .takeIf { it.signum() > 0 }

    /** Porcentaje pagado cuando llega al 90 % o más (`015` RF-03). */
    fun paidWarning(
        paid: BigDecimal,
        budget: BigDecimal,
    ): Int? {
        if (budget.signum() <= 0) return null
        val percent = paid.multiply(HUNDRED).divide(budget, 0, RoundingMode.DOWN).toInt()
        return percent.takeIf { it >= WARN_PERCENT }
    }
}

enum class PaymentStatus(
    val wire: String,
) {
    COMPLETED("completed"),
    PENDING("pending"),
    FAILED("failed"),
    REFUNDED("refunded"),
    ;

    companion object {
        fun fromWire(value: String?): PaymentStatus = entries.firstOrNull { it.wire == value } ?: PENDING
    }
}

/** Pago hecho por fuera de la app (`018`); la app no cobra. */
data class ExternalPayment(
    val id: String,
    val concept: String,
    val amount: BigDecimal,
    val status: PaymentStatus,
    val paidAt: LocalDate?,
)

enum class PaymentProblem {
    CONCEPT,
    AMOUNT,
    FUTURE_DATE,
}

object PaymentRules {
    const val CONCEPT_MAX = 150

    fun validate(
        concept: String,
        amount: BigDecimal?,
        date: LocalDate?,
        today: LocalDate,
    ): Set<PaymentProblem> =
        buildSet {
            if (concept.isBlank() || concept.trim().length > CONCEPT_MAX) add(PaymentProblem.CONCEPT)
            if (amount == null || amount.signum() <= 0 || amount.scale() > 2) add(PaymentProblem.AMOUNT)
            if (date != null && date.isAfter(today)) add(PaymentProblem.FUTURE_DATE)
        }

    fun paid(payments: List<ExternalPayment>): BigDecimal = total(payments, PaymentStatus.COMPLETED)

    fun pending(payments: List<ExternalPayment>): BigDecimal = total(payments, PaymentStatus.PENDING)

    private fun total(
        payments: List<ExternalPayment>,
        status: PaymentStatus,
    ): BigDecimal =
        payments.filter { it.status == status }.fold(BigDecimal.ZERO) { sum, payment -> sum + payment.amount }
}

/** Plan por categoría y pagos externos de un evento (`events.budgetPlan`, `events.externalPayments`). */
data class EventFinance(
    val event: ClientEvent,
    val plan: Map<BudgetCategory, BigDecimal>,
    val payments: List<ExternalPayment>,
)

interface EventFinanceRepository {
    /** El primer evento del cliente con su plan y pagos; `null` si no tiene eventos. */
    suspend fun current(): AppResult<EventFinance?>

    suspend fun savePlan(
        eventId: String,
        plan: Map<BudgetCategory, BigDecimal>,
    ): AppResult<Unit>

    suspend fun savePayments(
        eventId: String,
        payments: List<ExternalPayment>,
    ): AppResult<Unit>
}

class GetEventFinanceUseCase
    @Inject
    constructor(
        private val repository: EventFinanceRepository,
    ) {
        suspend operator fun invoke(): AppResult<EventFinance?> = repository.current()
    }
