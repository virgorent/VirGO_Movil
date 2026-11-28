package mx.virgo.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.common.AuthTokenProvider
import mx.virgo.core.domain.QuoteAnswer
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.domain.QuoteRequest
import mx.virgo.core.domain.QuoteStatus
import mx.virgo.core.network.createOkHttpClient
import mx.virgo.core.network.createQuotesApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/** Forma de `Quote` serializada por `QuotesController` de `feat/api-mobile-priority` más el PR #41. */
class QuotesRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: QuotesRepositoryImpl

    private val request =
        QuoteRequest(
            "132a1f58-ebf9-4082-acdf-7241975f1811",
            QuoteEventType.QUINCEANERA,
            LocalDate.parse("2027-05-15"),
            120,
            "  ",
        )

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val tokens =
            object : AuthTokenProvider {
                override val token: Flow<String?> = flowOf("client.token")
            }
        val client =
            createOkHttpClient(tokens, isDebug = false)
                .newBuilder()
                .readTimeout(QUOTE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()
        repository = QuotesRepositoryImpl(createQuotesApi(server.url("/").toString(), client))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun createSendsTheContractBodyAndReadsTheSentQuote() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(201).setBody(QUOTE_BODY))

            val quote = (repository.create(request) as AppResult.Success).value

            assertEquals(QuoteStatus.SENT, quote.status)
            assertEquals("Salón Las Palmas", quote.venueName)
            assertEquals(QuoteEventType.QUINCEANERA, quote.eventType)
            assertNull(quote.total)
            val sent = server.takeRequest()
            assertEquals("POST", sent.method)
            assertEquals("/api/v1/quotes", sent.path)
            val json = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
            val target = json["target"]!!.jsonObject
            assertEquals("venue", target["type"]!!.jsonPrimitive.content)
            assertEquals(request.venueId, target["id"]!!.jsonPrimitive.content)
            assertFalse(target.containsKey("name"))
            assertEquals("XV años", json["eventType"]!!.jsonPrimitive.content)
            assertEquals("2027-05-15", json["eventDate"]!!.jsonPrimitive.content)
            assertEquals(120, json["guests"]!!.jsonPrimitive.content.toInt())
            assertFalse(json.containsKey("message"))
            assertFalse(json.containsKey("eventId"))
            assertFalse(json.containsKey("clientName"))
        }

    @Test
    fun messageIsTrimmedAndSentWhenPresent() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(201).setBody(QUOTE_BODY))
            repository.create(request.copy(message = " Somos 120 "))
            val json = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
            assertEquals("Somos 120", json["message"]!!.jsonPrimitive.content)
        }

    @Test
    fun listAndDetailReadTheServerResponse() =
        runTest {
            val answered =
                QUOTE_BODY
                    .replace("\"sent\"", "\"answered\"")
                    .replace(
                        "\"response\":null",
                        "\"response\":{\"total\":48000.0,\"deposit\":14400,\"message\":\"Listo\"}",
                    )
            server.enqueue(MockResponse().setBody("""{"data":[$QUOTE_BODY]}"""))
            server.enqueue(MockResponse().setBody(answered))

            assertEquals(QuoteStatus.SENT, (repository.list() as AppResult.Success).value.single().status)
            val detail = (repository.get("5b2e7c10-0000-4000-8000-000000000001") as AppResult.Success).value
            assertEquals(QuoteStatus.ANSWERED, detail.status)
            assertEquals(0, BigDecimal("48000").compareTo(detail.total))
            assertEquals(0, BigDecimal("14400").compareTo(detail.deposit))
            assertEquals("Listo", detail.responseMessage)
            server.takeRequest()
            assertEquals("/api/v1/quotes/5b2e7c10-0000-4000-8000-000000000001", server.takeRequest().path)
        }

    @Test
    fun respondSendsTheOwnerAnswerAndReadsTheAnsweredQuote() =
        runTest {
            val answered =
                QUOTE_BODY
                    .replace("\"sent\"", "\"answered\"")
                    .replace("\"response\":null", "\"response\":{\"total\":48000.5,\"deposit\":null,\"message\":null}")
            server.enqueue(MockResponse().setBody(answered))
            server.enqueue(MockResponse().setBody(answered))

            val quote = (repository.respond("q-1", QuoteAnswer(BigDecimal("48000.50"))) as AppResult.Success).value
            assertEquals(QuoteStatus.ANSWERED, quote.status)
            assertEquals("Ana", quote.clientName)
            val sent = server.takeRequest()
            assertEquals("/api/v1/quotes/q-1/response", sent.path)
            assertEquals("POST", sent.method)
            val json = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
            assertEquals(48000.5, json["total"]!!.jsonPrimitive.content.toDouble(), 0.0)
            assertFalse(json.containsKey("deposit"))
            assertFalse(json.containsKey("message"))

            repository.respond("q-1", QuoteAnswer(BigDecimal("100"), BigDecimal("30"), " Gracias "))
            val full = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
            assertEquals(30.0, full["deposit"]!!.jsonPrimitive.content.toDouble(), 0.0)
            assertEquals("Gracias", full["message"]!!.jsonPrimitive.content)
        }

    @Test
    fun respondConflictAndForbiddenAreMapped() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(409))
            assertEquals(
                AppResult.Failure(AppError.Conflict),
                repository.respond("q", QuoteAnswer(BigDecimal.ONE)),
            )
            server.enqueue(MockResponse().setResponseCode(403))
            assertEquals(
                AppResult.Failure(AppError.Forbidden),
                repository.respond("q", QuoteAnswer(BigDecimal.ONE)),
            )
        }

    @Test
    fun unknownStatusAndMissingFieldsDoNotFail() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[{"id":"q","status":"negotiating","extra":1}]}"""))
            val quote = (repository.list() as AppResult.Success).value.single()
            assertEquals(QuoteStatus.UNKNOWN, quote.status)
            assertNull(quote.eventDate)
            assertNull(quote.eventType)
            assertEquals(0, quote.guests)
        }

    @Test
    fun fieldErrorsAndOtherFailuresAreMapped() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(400)
                    .setHeader("Content-Type", "application/problem+json")
                    .setBody(
                        """{"title":"La solicitud de cotización es inválida.","status":400,""" +
                            """"errors":{"eventDate":["La fecha del evento no puede estar en el pasado."]}}""",
                    ),
            )
            val validation = (repository.create(request) as AppResult.Failure).error as AppError.Validation
            assertEquals(listOf("La fecha del evento no puede estar en el pasado."), validation.fields["eventDate"])
            server.enqueue(MockResponse().setResponseCode(400).setBody("""{"title":"inválida"}"""))
            assertEquals(AppError.Validation(emptyMap()), (repository.create(request) as AppResult.Failure).error)
            server.enqueue(MockResponse().setResponseCode(401))
            assertEquals(AppResult.Failure(AppError.Unauthorized), repository.list())
            server.enqueue(MockResponse().setResponseCode(404))
            assertEquals(AppResult.Failure(AppError.NotFound), repository.create(request))
            server.enqueue(MockResponse().setResponseCode(500))
            assertTrue((repository.get("x") as AppResult.Failure).error is AppError.Server)
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            assertEquals(AppResult.Failure(AppError.Timeout), repository.create(request))
        }
}

private const val QUOTE_TIMEOUT_MS = 300L

private const val QUOTE_BODY =
    """{"id":"5b2e7c10-0000-4000-8000-000000000001","clientId":"c1","clientName":"Ana",""" +
        """"targetType":"venue","targetId":"132a1f58-ebf9-4082-acdf-7241975f1811",""" +
        """"targetName":"Salón Las Palmas","eventId":null,"eventType":"XV años","eventDate":"2027-05-15",""" +
        """"guests":120,"requestedServices":[],"message":null,"response":null,"status":"sent",""" +
        """"createdAt":"2026-11-25T18:00:00Z","updatedAt":"2026-11-25T18:00:00Z"}"""
