package mx.virgo.core.testing

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.EventFinance
import mx.virgo.core.domain.EventFinanceRepository
import mx.virgo.core.domain.ExternalPayment
import java.math.BigDecimal

/** Guarda lo enviado; [saveFailure] simula al servidor sin el campo o caído. */
class FakeEventFinanceRepository : EventFinanceRepository {
    var finance: EventFinance? = EventFinance(sampleEvent(), emptyMap(), emptyList())
    var loadFailure: AppResult.Failure? = null
    var saveFailure: AppResult.Failure? = null
    val savedPlans = mutableListOf<Map<BudgetCategory, BigDecimal>>()
    val savedPayments = mutableListOf<List<ExternalPayment>>()

    override suspend fun current(): AppResult<EventFinance?> = loadFailure ?: AppResult.Success(finance)

    override suspend fun savePlan(
        eventId: String,
        plan: Map<BudgetCategory, BigDecimal>,
    ): AppResult<Unit> {
        savedPlans += plan
        return saveFailure ?: AppResult.Success(Unit)
    }

    override suspend fun savePayments(
        eventId: String,
        payments: List<ExternalPayment>,
    ): AppResult<Unit> {
        savedPayments += payments
        return saveFailure ?: AppResult.Success(Unit)
    }
}
