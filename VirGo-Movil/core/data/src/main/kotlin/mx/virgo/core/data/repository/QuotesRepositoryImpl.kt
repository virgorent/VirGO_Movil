package mx.virgo.core.data.repository

import kotlinx.coroutines.CancellationException
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.Quote
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.QuoteRequest
import mx.virgo.core.domain.QuoteStatus
import mx.virgo.core.domain.QuotesRepository
import mx.virgo.core.network.api.QuotesApi
import mx.virgo.core.network.dto.CreateQuoteRequestDto
import mx.virgo.core.network.dto.QuoteDto
import mx.virgo.core.network.dto.QuoteTargetDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

class QuotesRepositoryImpl
    @Inject
    constructor(
        private val api: QuotesApi,
    ) : QuotesRepository {
        override suspend fun create(request: QuoteRequest): AppResult<Quote> =
            safeCall {
                val body =
                    CreateQuoteRequestDto(
                        target = QuoteTargetDto(type = "venue", id = request.venueId),
                        eventType = requireNotNull(request.eventType).wire,
                        eventDate = requireNotNull(request.eventDate).toString(),
                        guests = requireNotNull(request.guests),
                        message = request.message.trim().ifEmpty { null },
                    )
                AppResult.Success(api.create(body).toQuote())
            }

        override suspend fun list(): AppResult<List<Quote>> =
            safeCall { AppResult.Success(api.list().data.map { it.toQuote() }) }

        override suspend fun get(id: String): AppResult<Quote> = safeCall { AppResult.Success(api.get(id).toQuote()) }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun <T> safeCall(block: suspend () -> AppResult<T>): AppResult<T> =
            try {
                block()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                AppResult.Failure(error.toAppError())
            }
    }

internal fun QuoteDto.toQuote(): Quote =
    Quote(
        id = id,
        venueId = targetId,
        venueName = targetName.orEmpty(),
        eventType = QuoteEventType.fromWire(eventType),
        eventDate = eventDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        guests = guests ?: 0,
        message = message,
        status = QuoteStatus.fromWire(status),
        total = response?.total?.let(BigDecimal::valueOf),
        deposit = response?.deposit?.let(BigDecimal::valueOf),
        responseMessage = response?.message,
        createdAt = createdAt,
    )
