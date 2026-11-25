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
import mx.virgo.core.domain.BookingRequest
import mx.virgo.core.domain.BookingStatus
import mx.virgo.core.network.createBookingsApi
import mx.virgo.core.network.createOkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/** Forma del `Booking` serializado por `BookingsController` de `feat/api-mobile-priority` (`7cf6124`). */
class BookingsRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: BookingsRepositoryImpl

    private val request = BookingRequest("132a1f58-ebf9-4082-acdf-7241975f1811", LocalDate.parse("2027-05-15"), 5)

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
                .readTimeout(BOOKING_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()
        repository = BookingsRepositoryImpl(createBookingsApi(server.url("/").toString(), client))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun createSendsKeyAndNoDepositAndReadsTheServerBooking() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(201).setBody(BOOKING_BODY))

            val booking = (repository.create(request, "key-123") as AppResult.Success).value

            assertEquals("VG-2027-0001", booking.folio)
            assertEquals(BookingStatus.PENDING_PAYMENT, booking.status)
            assertEquals(0, BigDecimal("7500").compareTo(booking.total))
            assertEquals(0, BigDecimal("2250").compareTo(booking.deposit))
            assertEquals(LocalDate.parse("2027-05-15"), booking.date)
            val sent = server.takeRequest()
            assertEquals("POST", sent.method)
            assertEquals("key-123", sent.getHeader("Idempotency-Key"))
            val json = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
            assertEquals("2027-05-15", json["date"]!!.jsonPrimitive.content)
            assertEquals(5, json["hours"]!!.jsonPrimitive.content.toInt())
            assertFalse(json.containsKey("deposit"))
            assertFalse(json.containsKey("quoteId"))
            assertFalse(json.containsKey("eventId"))
        }

    @Test
    fun quoteIdIsSentWhenPresent() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(201).setBody(BOOKING_BODY))

            repository.create(request.copy(quoteId = "q-1"), "k")

            val json = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
            assertEquals("q-1", json["quoteId"]!!.jsonPrimitive.content)
        }

    @Test
    fun retryUsesTheSameKey() =
        runTest {
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            server.enqueue(MockResponse().setResponseCode(201).setBody(BOOKING_BODY))

            assertEquals(AppResult.Failure(AppError.Timeout), repository.create(request, "same-key"))
            assertTrue(repository.create(request, "same-key") is AppResult.Success)

            assertEquals("same-key", server.takeRequest().getHeader("Idempotency-Key"))
            assertEquals("same-key", server.takeRequest().getHeader("Idempotency-Key"))
        }

    @Test
    fun listAndGetReadTheServer() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"data":[$BOOKING_BODY]}"""))
            server.enqueue(MockResponse().setBody(BOOKING_BODY.replace("pending_payment", "refunded")))

            assertEquals("Salón Las Palmas", (repository.list() as AppResult.Success).value.single().venueName)
            val single = (repository.get("8a1d2c3b-0000-4000-8000-000000000001") as AppResult.Success).value
            assertEquals(BookingStatus.UNKNOWN, single.status)
            server.takeRequest()
            assertEquals("/api/v1/bookings/8a1d2c3b-0000-4000-8000-000000000001", server.takeRequest().path)
        }

    @Test
    fun errorsMapToAppErrors() =
        runTest {
            server.enqueue(
                MockResponse().setResponseCode(400).setBody("""{"title":"La solicitud de reservación es inválida."}"""),
            )
            assertTrue((repository.create(request, "k") as AppResult.Failure).error is AppError.Validation)
            server.enqueue(MockResponse().setResponseCode(401))
            assertEquals(AppResult.Failure(AppError.Unauthorized), repository.list())
            server.enqueue(MockResponse().setResponseCode(404))
            assertEquals(AppResult.Failure(AppError.NotFound), repository.create(request, "k"))
            server.enqueue(MockResponse().setResponseCode(500))
            assertTrue((repository.get("x") as AppResult.Failure).error is AppError.Server)
        }
}

private const val BOOKING_TIMEOUT_MS = 300L

private const val BOOKING_BODY =
    """{"id":"8a1d2c3b-0000-4000-8000-000000000001","clientId":"c1","idempotencyKey":"key-123",""" +
        """"clientName":"Ana","quoteId":null,"eventId":null,"venueId":"132a1f58-ebf9-4082-acdf-7241975f1811",""" +
        """"venueName":"Salón Las Palmas","date":"2027-05-15","hours":5,"total":7500.0,"deposit":2250.00,""" +
        """"folio":"VG-2027-0001","status":"pending_payment","payments":[],""" +
        """"createdAt":"2026-11-25T18:00:00Z","updatedAt":"2026-11-25T18:00:00Z"}"""
