package mx.virgo.core.data.repository

import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.BudgetCategory
import mx.virgo.core.domain.EventFinance
import mx.virgo.core.domain.EventFinanceRepository
import mx.virgo.core.domain.ExternalPayment
import mx.virgo.core.domain.PaymentStatus
import mx.virgo.core.network.api.EventsApi
import mx.virgo.core.network.dto.EventDto
import mx.virgo.core.network.dto.EventFinanceRequestDto
import mx.virgo.core.network.dto.ExternalPaymentDto
import java.math.BigDecimal
import java.time.ZoneOffset
import javax.inject.Inject

/** Plan y pagos del evento sobre `GET`/`PATCH /events/:id` (contrato §4). */
class EventFinanceRepositoryImpl
    @Inject
    constructor(
        private val api: EventsApi,
    ) : EventFinanceRepository {
        override suspend fun current(): AppResult<EventFinance?> =
            eventCall {
                val first = api.list().data.firstOrNull()
                AppResult.Success(first?.let { api.get(it.id).toFinance() })
            }

        override suspend fun savePlan(
            eventId: String,
            plan: Map<BudgetCategory, BigDecimal>,
        ): AppResult<Unit> =
            eventCall {
                api.updateFinance(
                    eventId,
                    EventFinanceRequestDto(
                        budgetPlan =
                            plan.entries.associate {
                                it.key.wire to
                                    it.value.toDouble()
                            },
                    ),
                )
                AppResult.Success(Unit)
            }

        override suspend fun savePayments(
            eventId: String,
            payments: List<ExternalPayment>,
        ): AppResult<Unit> =
            eventCall {
                api.updateFinance(eventId, EventFinanceRequestDto(externalPayments = payments.map { it.toDto() }))
                AppResult.Success(Unit)
            }
    }

internal fun EventDto.toFinance(): EventFinance =
    EventFinance(
        event = toEvent(),
        plan =
            budgetPlan
                .orEmpty()
                .mapNotNull { (key, value) -> BudgetCategory.fromWire(key)?.let { it to BigDecimal.valueOf(value) } }
                .toMap(),
        payments =
            externalPayments.orEmpty().map {
                ExternalPayment(
                    id = it.id,
                    concept = it.concept,
                    amount = BigDecimal.valueOf(it.amount),
                    status = PaymentStatus.fromWire(it.status),
                    paidAt = parseDate(it.paidAt?.take(DATE_LENGTH)),
                )
            },
    )

private fun ExternalPayment.toDto(): ExternalPaymentDto =
    ExternalPaymentDto(
        id = id,
        concept = concept.trim(),
        amount = amount.toDouble(),
        status = status.wire,
        paidAt = paidAt?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toString(),
    )

private const val DATE_LENGTH = 10
