package mx.virgo.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class EventFinanceTest {
    private val budget = BigDecimal("150000")
    private val today = LocalDate.parse("2026-11-27")

    @Test
    fun weddingSplitFollowsThePrototype() {
        val plan = BudgetRules.suggest(QuoteEventType.WEDDING, budget, emptySet())
        assertEquals(BigDecimal("52500"), plan[BudgetCategory.CATERING])
        assertEquals(BigDecimal("45000"), plan[BudgetCategory.VENUE])
        assertEquals(BudgetCategory.entries.toSet(), plan.keys)
    }

    @Test
    fun premiumFoodMovesCateringUpAndDecorationDown() {
        val base = BudgetRules.suggest(QuoteEventType.WEDDING, budget, emptySet())
        val food = BudgetRules.suggest(QuoteEventType.WEDDING, budget, setOf(BudgetPreference.PREMIUM_FOOD))
        assertTrue(food.getValue(BudgetCategory.CATERING) > base.getValue(BudgetCategory.CATERING))
        assertTrue(food.getValue(BudgetCategory.DECORATION) < base.getValue(BudgetCategory.DECORATION))
    }

    @Test
    fun alertsWhenThePlanExceedsOrPaymentsReachNinetyPercent() {
        assertEquals(
            BigDecimal("10000"),
            BudgetRules.overPlanned(mapOf(BudgetCategory.VENUE to BigDecimal("160000")), budget),
        )
        assertNull(BudgetRules.overPlanned(mapOf(BudgetCategory.VENUE to BigDecimal("1000")), budget))
        assertEquals(90, BudgetRules.paidWarning(BigDecimal("135000"), budget))
        assertNull(BudgetRules.paidWarning(BigDecimal("100000"), budget))
    }

    @Test
    fun paymentsAreValidatedAndSummed() {
        assertEquals(
            setOf(PaymentProblem.CONCEPT, PaymentProblem.AMOUNT),
            PaymentRules.validate(" ", BigDecimal.ZERO, null, today),
        )
        assertEquals(setOf(PaymentProblem.AMOUNT), PaymentRules.validate("DJ", BigDecimal("1.234"), null, today))
        assertEquals(
            setOf(PaymentProblem.FUTURE_DATE),
            PaymentRules.validate("DJ", BigDecimal("10"), today.plusDays(1), today),
        )
        val payments =
            listOf(
                ExternalPayment("1", "Anticipo", BigDecimal("3000"), PaymentStatus.COMPLETED, today),
                ExternalPayment("2", "DJ", BigDecimal("5000"), PaymentStatus.COMPLETED, today),
                ExternalPayment("3", "Pastel", BigDecimal("2000"), PaymentStatus.PENDING, null),
            )
        assertEquals(BigDecimal("8000"), PaymentRules.paid(payments))
        assertEquals(BigDecimal("2000"), PaymentRules.pending(payments))
    }
}
