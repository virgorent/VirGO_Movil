package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import java.math.BigDecimal
import java.time.LocalDate

/** Estado de la cotización tal como lo decide el servidor; uno nuevo no rompe la app (008 ADR-01). */
enum class QuoteStatus {
    SENT,
    ANSWERED,
    ACCEPTED,
    REJECTED,
    EXPIRED,
    UNKNOWN,
    ;

    companion object {
        fun fromWire(value: String?): QuoteStatus =
            when (value) {
                "sent" -> SENT
                // El backend usa `answered`; `responded` era el nombre del spec v2.0.0.
                "answered", "responded" -> ANSWERED
                "accepted" -> ACCEPTED
                "rejected" -> REJECTED
                "expired" -> EXPIRED
                else -> UNKNOWN
            }
    }
}

/** Tipos de evento que acepta el servidor; `wire` es el valor exacto del contrato. */
enum class QuoteEventType(
    val wire: String,
) {
    WEDDING("Boda"),
    QUINCEANERA("XV años"),
    BIRTHDAY("Cumpleaños"),
    CORPORATE("Corporativo"),
    OTHER("Otro"),
    ;

    companion object {
        fun fromWire(value: String?): QuoteEventType? = entries.firstOrNull { it.wire == value }
    }
}

/** Cotización del servidor; los montos solo existen si el salón respondió (008 ADR-03). */
data class Quote(
    val id: String,
    val venueId: String?,
    val venueName: String,
    val eventType: QuoteEventType?,
    val eventDate: LocalDate?,
    val guests: Int,
    val message: String?,
    val status: QuoteStatus,
    val total: BigDecimal?,
    val deposit: BigDecimal?,
    val responseMessage: String?,
    val createdAt: String?,
) {
    /** Solo una cotización respondida con salón conocido lleva a reservar (008 RF-11). */
    val canBook: Boolean get() = status == QuoteStatus.ANSWERED && venueId != null
}

data class QuoteRequest(
    val venueId: String,
    val eventType: QuoteEventType?,
    val eventDate: LocalDate?,
    val guests: Int?,
    val message: String = "",
)

enum class QuoteField {
    EVENT_TYPE,
    DATE,
    GUESTS,
    MESSAGE,
    ;

    companion object {
        /** Nombre del campo en el Problem Details del servidor. */
        fun fromWire(name: String): QuoteField? =
            when (name) {
                "eventType" -> EVENT_TYPE
                "eventDate" -> DATE
                "guests" -> GUESTS
                "message" -> MESSAGE
                else -> null
            }
    }
}

enum class QuoteProblem {
    REQUIRED,
    PAST_DATE,
    TOO_LONG,
}

/** Tipo, fecha no pasada, invitados ≥ 1 y mensaje ≤ 500 (008 RF-02). */
object QuoteValidator {
    const val MAX_MESSAGE = 500

    fun validate(
        request: QuoteRequest,
        today: LocalDate,
    ): Map<QuoteField, QuoteProblem> =
        buildMap {
            if (request.eventType == null) put(QuoteField.EVENT_TYPE, QuoteProblem.REQUIRED)
            when {
                request.eventDate == null -> put(QuoteField.DATE, QuoteProblem.REQUIRED)
                request.eventDate.isBefore(today) -> put(QuoteField.DATE, QuoteProblem.PAST_DATE)
            }
            if ((request.guests ?: 0) < 1) put(QuoteField.GUESTS, QuoteProblem.REQUIRED)
            if (request.message.length > MAX_MESSAGE) put(QuoteField.MESSAGE, QuoteProblem.TOO_LONG)
        }

    /** Invitados por encima de la capacidad conocida: se avisa sin bloquear (008 RF-04). */
    fun exceedsCapacity(
        guests: Int?,
        capacity: Int?,
    ): Boolean = guests != null && capacity != null && capacity > 0 && guests > capacity
}

interface QuotesRepository {
    suspend fun create(request: QuoteRequest): AppResult<Quote>

    suspend fun list(): AppResult<List<Quote>>

    suspend fun get(id: String): AppResult<Quote>
}
