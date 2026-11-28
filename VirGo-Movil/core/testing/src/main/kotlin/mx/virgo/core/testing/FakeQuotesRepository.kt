package mx.virgo.core.testing

import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.QuoteAnswer
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.QuoteRequest
import mx.virgo.core.domain.QuoteStatus
import mx.virgo.core.domain.QuotesRepository
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Como el servidor: cada POST crea una cotización «Enviada». `createFailure` simula el error
 * y `landsDespiteFailure` una petición que llegó aunque el cliente no vio la respuesta.
 */
class FakeQuotesRepository : QuotesRepository {
    val quotes = mutableListOf<Quote>()
    val requests = mutableListOf<QuoteRequest>()
    var createFailure: AppResult.Failure? = null
    var landsDespiteFailure = false
    var listFailure: AppResult.Failure? = null
    var respondFailure: AppResult.Failure? = null
    val answers = mutableListOf<Pair<String, QuoteAnswer>>()

    override suspend fun create(request: QuoteRequest): AppResult<Quote> {
        requests += request
        val failure = createFailure
        if (failure != null && !landsDespiteFailure) return failure
        val quote =
            sampleQuote().copy(
                id = "quote-${quotes.size + 1}",
                venueId = request.venueId,
                eventType = request.eventType,
                eventDate = request.eventDate,
                guests = request.guests ?: 0,
                message = request.message.trim().ifEmpty { null },
                createdAt = "2026-11-25T18:%02d:00Z".format(quotes.size),
            )
        quotes += quote
        return failure ?: AppResult.Success(quote)
    }

    override suspend fun list(): AppResult<List<Quote>> = listFailure ?: AppResult.Success(quotes.toList())

    override suspend fun get(id: String): AppResult<Quote> =
        quotes.firstOrNull { it.id == id }?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.NotFound)

    /** Como el servidor: solo desde «Enviada»; otro estado es `409`. */
    override suspend fun respond(
        id: String,
        answer: QuoteAnswer,
    ): AppResult<Quote> {
        answers += id to answer
        val index = quotes.indexOfFirst { it.id == id }
        val failure = respondFailure
        return when {
            failure != null -> failure
            index < 0 -> AppResult.Failure(AppError.NotFound)
            quotes[index].status != QuoteStatus.SENT -> AppResult.Failure(AppError.Conflict)
            else -> {
                val answered =
                    quotes[index].copy(
                        status = QuoteStatus.ANSWERED,
                        total = answer.total,
                        deposit = answer.deposit,
                        responseMessage = answer.message.trim().ifEmpty { null },
                    )
                quotes[index] = answered
                AppResult.Success(answered)
            }
        }
    }
}

fun sampleQuote(): Quote =
    Quote(
        id = "5b2e7c10-0000-4000-8000-000000000001",
        venueId = "132a1f58-ebf9-4082-acdf-7241975f1811",
        venueName = "Salón Las Palmas",
        eventType = QuoteEventType.WEDDING,
        eventDate = LocalDate.parse("2027-05-15"),
        guests = 120,
        message = null,
        status = QuoteStatus.SENT,
        total = null,
        deposit = null,
        responseMessage = null,
        createdAt = "2026-11-25T18:00:00Z",
        clientName = "Ana López",
    )

/** La misma cotización respondida por el propietario con un total. */
fun sampleAnsweredQuote(): Quote =
    sampleQuote().copy(
        status = QuoteStatus.ANSWERED,
        total = BigDecimal("48000"),
        deposit = BigDecimal("14400"),
        responseMessage = "Incluye mobiliario y limpieza.",
    )
