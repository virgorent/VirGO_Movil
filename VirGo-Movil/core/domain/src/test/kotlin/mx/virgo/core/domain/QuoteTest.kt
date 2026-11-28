package mx.virgo.core.domain

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class QuoteTest {
    private val today = LocalDate.parse("2026-11-25")

    private fun request(
        type: QuoteEventType? = QuoteEventType.WEDDING,
        date: LocalDate? = today,
        guests: Int? = 1,
        message: String = "",
    ) = QuoteRequest("v1", type, date, guests, message)

    @Test
    fun validRequestHasNoProblems() {
        assertTrue(QuoteValidator.validate(request(), today).isEmpty())
        assertTrue(QuoteValidator.validate(request(message = "a".repeat(500)), today).isEmpty())
    }

    @Test
    fun eachFieldIsChecked() {
        val errors = QuoteValidator.validate(request(null, today.minusDays(1), 0, "a".repeat(501)), today)
        assertEquals(QuoteProblem.REQUIRED, errors[QuoteField.EVENT_TYPE])
        assertEquals(QuoteProblem.PAST_DATE, errors[QuoteField.DATE])
        assertEquals(QuoteProblem.REQUIRED, errors[QuoteField.GUESTS])
        assertEquals(QuoteProblem.TOO_LONG, errors[QuoteField.MESSAGE])
        val missing = QuoteValidator.validate(request(date = null, guests = null), today)
        assertEquals(QuoteProblem.REQUIRED, missing[QuoteField.DATE])
        assertEquals(QuoteProblem.REQUIRED, missing[QuoteField.GUESTS])
    }

    @Test
    fun capacityWarningOnlyWithAKnownCapacity() {
        assertTrue(QuoteValidator.exceedsCapacity(151, 150))
        assertFalse(QuoteValidator.exceedsCapacity(150, 150))
        assertFalse(QuoteValidator.exceedsCapacity(500, 0))
        assertFalse(QuoteValidator.exceedsCapacity(500, null))
        assertFalse(QuoteValidator.exceedsCapacity(null, 10))
    }

    @Test
    fun statusesAndTypesFromTheServer() {
        assertEquals(QuoteStatus.SENT, QuoteStatus.fromWire("sent"))
        assertEquals(QuoteStatus.ANSWERED, QuoteStatus.fromWire("answered"))
        assertEquals(QuoteStatus.ANSWERED, QuoteStatus.fromWire("responded"))
        assertEquals(QuoteStatus.ACCEPTED, QuoteStatus.fromWire("accepted"))
        assertEquals(QuoteStatus.REJECTED, QuoteStatus.fromWire("rejected"))
        assertEquals(QuoteStatus.EXPIRED, QuoteStatus.fromWire("expired"))
        assertEquals(QuoteStatus.UNKNOWN, QuoteStatus.fromWire("negotiating"))
        assertEquals(QuoteStatus.UNKNOWN, QuoteStatus.fromWire(null))
        assertEquals(QuoteEventType.QUINCEANERA, QuoteEventType.fromWire("XV años"))
        assertNull(QuoteEventType.fromWire("Fiesta"))
        assertEquals(QuoteField.DATE, QuoteField.fromWire("eventDate"))
        assertEquals(QuoteField.EVENT_TYPE, QuoteField.fromWire("eventType"))
        assertEquals(QuoteField.GUESTS, QuoteField.fromWire("guests"))
        assertEquals(QuoteField.MESSAGE, QuoteField.fromWire("message"))
        assertNull(QuoteField.fromWire("target.id"))
    }

    @Test
    fun onlyAnAnsweredQuoteWithVenueCanBeBooked() {
        assertTrue(quote("a", QuoteStatus.ANSWERED).canBook)
        assertFalse(quote("a", QuoteStatus.SENT).canBook)
        assertFalse(quote("a", QuoteStatus.ACCEPTED).canBook)
        assertFalse(quote("a", QuoteStatus.ANSWERED).copy(venueId = null).canBook)
    }

    @Test
    fun listIsNewestFirstAndDetailDelegates() =
        runTest {
            val repository = Repo(listOf(quote("old", createdAt = "2026-11-20T00:00:00Z"), quote("new")))
            assertEquals(
                listOf("new", "old"),
                (GetQuotesUseCase(repository)() as AppResult.Success).value.map { it.id },
            )
            assertEquals(AppResult.Failure(AppError.NotFound), GetQuoteUseCase(repository)("x"))
            repository.listResult = AppResult.Failure(AppError.Network)
            assertEquals(AppResult.Failure(AppError.Network), GetQuotesUseCase(repository)())
        }

    @Test
    fun aFirstAttemptAlwaysPosts() =
        runTest {
            val repository = Repo(listOf(quote("same")))
            CreateQuoteUseCase(repository)(request(guests = 120, date = today.plusDays(9)))
            assertEquals(1, repository.created)
        }

    @Test
    fun retryAfterAnUncertainFailureReusesTheIdenticalSentQuote() =
        runTest {
            val repository = Repo(listOf(quote("landed")))
            val result =
                CreateQuoteUseCase(repository)(
                    request(guests = 120, date = today.plusDays(9), message = " Hola "),
                    afterUncertainFailure = true,
                )
            assertEquals("landed", (result as AppResult.Success).value.id)
            assertEquals(0, repository.created)
        }

    @Test
    fun retryPostsWhenNothingMatchesOrTheListFails() =
        runTest {
            val repository =
                Repo(
                    listOf(
                        quote("answered", QuoteStatus.ANSWERED),
                        quote("other-venue").copy(venueId = "v2"),
                        quote("other-type").copy(eventType = QuoteEventType.BIRTHDAY),
                        quote("other-date").copy(eventDate = today),
                        quote("other-guests").copy(guests = 3),
                        quote("other-message").copy(message = "Adiós"),
                    ),
                )
            val useCase = CreateQuoteUseCase(repository)
            useCase(request(guests = 120, date = today.plusDays(9), message = "Hola"), afterUncertainFailure = true)
            assertEquals(1, repository.created)
            repository.listResult = AppResult.Failure(AppError.Network)
            useCase(request(guests = 120, date = today.plusDays(9), message = "Hola"), afterUncertainFailure = true)
            assertEquals(2, repository.created)
        }

    private fun quote(
        id: String,
        status: QuoteStatus = QuoteStatus.SENT,
        createdAt: String = "2026-11-24T00:00:00Z",
    ) = Quote(
        id = id,
        venueId = "v1",
        venueName = "Salón",
        eventType = QuoteEventType.WEDDING,
        eventDate = today.plusDays(9),
        guests = 120,
        message = "Hola",
        status = status,
        total = null,
        deposit = null,
        responseMessage = null,
        createdAt = createdAt,
    )

    private class Repo(
        quotes: List<Quote>,
    ) : QuotesRepository {
        var listResult: AppResult<List<Quote>> = AppResult.Success(quotes)
        var created = 0

        override suspend fun create(request: QuoteRequest): AppResult<Quote> {
            created++
            return AppResult.Failure(AppError.Server(null))
        }

        override suspend fun list(): AppResult<List<Quote>> = listResult

        override suspend fun get(id: String): AppResult<Quote> = AppResult.Failure(AppError.NotFound)

        override suspend fun respond(
            id: String,
            answer: QuoteAnswer,
        ): AppResult<Quote> = AppResult.Failure(AppError.NotFound)
    }
}
