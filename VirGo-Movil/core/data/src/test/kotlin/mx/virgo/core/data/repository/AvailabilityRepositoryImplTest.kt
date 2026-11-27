package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.BlockReason
import mx.virgo.core.network.createAvailabilityApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/** Forma de `VenuesController.Availability`, `Block` y `DeleteBlock` en la cadena hacia `main` (2026-11-27). */
class AvailabilityRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: AvailabilityRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = AvailabilityRepositoryImpl(createAvailabilityApi(server.url("/").toString(), OkHttpClient()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun daysSendTheRangeAndReadBlocksWithoutId() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """{"data":[{"date":"2027-05-25","kind":"blocked","reason":"maintenance"},""" +
                        """{"date":"mal","kind":"blocked"}]}""",
                ),
            )
            val from = LocalDate.parse("2027-05-01")
            val to = LocalDate.parse("2027-05-31")
            val days = (repository.days(VENUE, from, to) as AppResult.Success).value
            assertEquals(1, days.size)
            assertEquals(BlockReason.MAINTENANCE, days.single().reason)
            assertNull(days.single().blockId)
            assertEquals("/api/v1/venues/$VENUE/availability?from=2027-05-01&to=2027-05-31", server.takeRequest().path)
        }

    @Test
    fun blockSendsDateReasonAndNoteAndReadsTheId() =
        runTest {
            server.enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .setBody(
                        """{"id":"b-1","date":"2027-05-26","kind":"blocked",""" +
                            """"reason":"own_event","note":"Boda familiar"}""",
                    ),
            )
            val date = LocalDate.parse("2027-05-26")
            val result = repository.block(VENUE, date, BlockReason.OWN_EVENT, "Boda familiar")
            assertEquals("b-1", (result as AppResult.Success).value.blockId)
            val sent = server.takeRequest()
            assertEquals("POST", sent.method)
            assertEquals("/api/v1/venues/$VENUE/blocks", sent.path)
            val json = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
            assertEquals("2027-05-26", json["date"]?.jsonPrimitive?.content)
            assertEquals("own_event", json["reason"]?.jsonPrimitive?.content)
        }

    @Test
    fun blockErrorsMapToAppErrors() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(409))
            server.enqueue(MockResponse().setResponseCode(400).setBody("""{"title":"El bloqueo es inválido."}"""))
            server.enqueue(MockResponse().setResponseCode(404))
            val date = LocalDate.parse("2027-05-26")
            assertEquals(AppResult.Failure(AppError.Conflict), repository.block(VENUE, date, BlockReason.OTHER, null))
            assertTrue(
                (
                    repository.block(
                        VENUE,
                        date,
                        BlockReason.OTHER,
                        null,
                    ) as AppResult.Failure
                ).error is AppError.Validation,
            )
            assertEquals(AppResult.Failure(AppError.NotFound), repository.block(VENUE, date, BlockReason.OTHER, null))
        }

    @Test
    fun unblockSendsDeleteAndAccepts204() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(204))
            server.enqueue(MockResponse().setResponseCode(404))
            assertEquals(AppResult.Success(Unit), repository.unblock(VENUE, "b-1"))
            val sent = server.takeRequest()
            assertEquals("DELETE", sent.method)
            assertEquals("/api/v1/venues/$VENUE/blocks/b-1", sent.path)
            assertEquals(AppResult.Failure(AppError.NotFound), repository.unblock(VENUE, "b-1"))
        }
}

private const val VENUE = "132a1f58-ebf9-4082-acdf-7241975f1811"
