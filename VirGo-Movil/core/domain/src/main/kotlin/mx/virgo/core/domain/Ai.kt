package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

/** Distribuciones de mesas del prototipo (`012` RF-02): personas por mesa y porcentaje del aforo. */
enum class TableLayout(
    val perTable: Int,
    val capacityPercent: Int,
) {
    CENTER_DANCE_FLOOR(perTable = 10, capacityPercent = 85),
    MAX_CAPACITY(perTable = 10, capacityPercent = 100),
    IMPERIAL(perTable = 12, capacityPercent = 75),
    ;

    fun tables(guests: Int): Int = (guests + perTable - 1) / perTable

    fun capacity(venueCapacity: Int): Int = venueCapacity * capacityPercent / PERCENT

    private companion object {
        const val PERCENT = 100
    }
}

/** Personal sugerido (`012` RF-04): una persona por cada [per] invitados, con un mínimo. */
enum class StaffRole(
    val per: Int,
    val minimum: Int,
) {
    WAITERS(per = 30, minimum = 2),
    SECURITY(per = 100, minimum = 1),
    CLEANING(per = 80, minimum = 1),
    ;

    fun suggested(guests: Int): Int = maxOf(minimum, (guests + per - 1) / per)
}

object EventPlanningRules {
    const val MIN_GUESTS = 1
    const val MAX_GUESTS = 2000

    /** El prototipo reserva el 12 % del presupuesto para decoración (`012` RF-07). */
    fun decorationBudget(budget: BigDecimal): BigDecimal =
        budget.multiply(BigDecimal("0.12")).setScale(0, RoundingMode.HALF_UP)

    fun staff(guests: Int): Map<StaffRole, Int> = StaffRole.entries.associateWith { it.suggested(guests) }
}

/** Idea de decoración que devuelve la IA del servidor. */
data class DecorIdea(
    val title: String,
    val description: String?,
    val cost: BigDecimal,
)

data class IdeasRequest(
    val eventType: QuoteEventType?,
    val guests: Int,
    val budget: BigDecimal?,
)

/** Mensaje del chat; [fromUser] distingue la pregunta de la respuesta. */
data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
    val uncertain: Boolean = false,
)

data class AssistantReply(
    val reply: String,
    val uncertain: Boolean,
)

/**
 * IA del backend (contrato `docs/contrato-apis-pendientes.md`). `404`/`501` llegan como `NotFound`:
 * el endpoint todavía no existe; `429` como `RateLimited`.
 */
interface AiRepository {
    suspend fun ideas(request: IdeasRequest): AppResult<List<DecorIdea>>

    suspend fun ask(
        message: String,
        eventId: String?,
        history: List<ChatMessage>,
    ): AppResult<AssistantReply>
}

class GetDecorIdeasUseCase
    @Inject
    constructor(
        private val repository: AiRepository,
    ) {
        suspend operator fun invoke(request: IdeasRequest): AppResult<List<DecorIdea>> = repository.ideas(request)
    }

class AskAssistantUseCase
    @Inject
    constructor(
        private val repository: AiRepository,
    ) {
        /** Manda solo los últimos [HISTORY] mensajes (`013` RF-04). */
        suspend operator fun invoke(
            message: String,
            eventId: String?,
            history: List<ChatMessage>,
        ): AppResult<AssistantReply> = repository.ask(message.trim(), eventId, history.takeLast(HISTORY))

        companion object {
            const val HISTORY = 10
            const val MAX_LENGTH = 500
        }
    }
