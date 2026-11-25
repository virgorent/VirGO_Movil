package mx.virgo.core.domain

import mx.virgo.core.common.AppResult
import javax.inject.Inject

/**
 * Envía la solicitud. El servidor no tiene `Idempotency-Key` para cotizaciones: si el intento
 * anterior quedó sin respuesta (`afterUncertainFailure`), antes de reenviar se busca en
 * «Mis cotizaciones» una solicitud idéntica todavía «Enviada» y se reutiliza (008 CA-04).
 */
class CreateQuoteUseCase
    @Inject
    constructor(
        private val repository: QuotesRepository,
    ) {
        suspend operator fun invoke(
            request: QuoteRequest,
            afterUncertainFailure: Boolean = false,
        ): AppResult<Quote> {
            val existing =
                if (afterUncertainFailure) {
                    (repository.list() as? AppResult.Success)?.value?.firstOrNull { it.matches(request) }
                } else {
                    null
                }
            return existing?.let { AppResult.Success(it) } ?: repository.create(request)
        }

        private fun Quote.matches(request: QuoteRequest): Boolean =
            status == QuoteStatus.SENT &&
                venueId == request.venueId &&
                eventType == request.eventType &&
                eventDate == request.eventDate &&
                guests == request.guests &&
                message.orEmpty() == request.message.trim()
    }

/** Lista más reciente primero (008 RF-07). */
class GetQuotesUseCase
    @Inject
    constructor(
        private val repository: QuotesRepository,
    ) {
        suspend operator fun invoke(): AppResult<List<Quote>> =
            when (val result = repository.list()) {
                is AppResult.Failure -> result
                is AppResult.Success -> AppResult.Success(result.value.sortedByDescending { it.createdAt.orEmpty() })
            }
    }

class RespondQuoteUseCase
    @Inject
    constructor(
        private val repository: QuotesRepository,
    ) {
        suspend operator fun invoke(
            id: String,
            answer: QuoteAnswer,
        ): AppResult<Quote> = repository.respond(id, answer)
    }

class GetQuoteUseCase
    @Inject
    constructor(
        private val repository: QuotesRepository,
    ) {
        suspend operator fun invoke(id: String): AppResult<Quote> = repository.get(id)
    }
