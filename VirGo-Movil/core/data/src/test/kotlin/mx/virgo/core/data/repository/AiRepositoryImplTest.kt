package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.domain.ChatMessage
import mx.virgo.core.domain.IdeasRequest
import mx.virgo.core.domain.QuoteEventType
import mx.virgo.core.network.createAiApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/** Contrato de `docs/contrato-apis-pendientes.md` (§ IA). */
class AiRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: AiRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = AiRepositoryImpl(createAiApi(server.url("/").toString(), OkHttpClient()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun ideasSendTheEventDataAndReadTheList() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """{"data":[{"title":"Centros de eucalipto","description":"Verde","cost":6500},{"title":" "}]}""",
                ),
            )
            val request = IdeasRequest(QuoteEventType.WEDDING, 120, BigDecimal("150000"))
            val ideas = (repository.ideas(request) as AppResult.Success).value
            assertEquals(1, ideas.size)
            assertEquals(BigDecimal.valueOf(6500.0), ideas.single().cost)
            val sent = server.takeRequest()
            assertEquals("/api/v1/ai/ideas", sent.path)
            val json = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
            assertEquals("Boda", json["eventType"]?.jsonPrimitive?.content)
            assertEquals(120, json["guests"]?.jsonPrimitive?.int)
        }

    @Test
    fun missingEndpointAndRateLimitAreRecognized() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            server.enqueue(MockResponse().setResponseCode(501))
            server.enqueue(MockResponse().setResponseCode(429))
            server.enqueue(MockResponse().setResponseCode(503))
            val request = IdeasRequest(null, 50, null)
            assertEquals(AppResult.Failure(AppError.NotFound), repository.ideas(request))
            assertEquals(AppResult.Failure(AppError.NotFound), repository.ideas(request))
            assertEquals(AppResult.Failure(AppError.RateLimited), repository.ideas(request))
            assertTrue((repository.ideas(request) as AppResult.Failure).error is AppError.Server)
        }

    @Test
    fun askSendsMessageEventAndHistoryWithRoles() =
        runTest {
            server.enqueue(MockResponse().setBody("""{"reply":" Unos 4 meseros. ","uncertain":true}"""))
            val history = listOf(ChatMessage("Hola", fromUser = true), ChatMessage("¡Hola!", fromUser = false))
            val reply = (repository.ask("¿Meseros?", "event-1", history) as AppResult.Success).value
            assertEquals("Unos 4 meseros.", reply.reply)
            assertTrue(reply.uncertain)
            val sent = server.takeRequest()
            assertEquals("POST", sent.method)
            assertEquals("/api/v1/ai/assistant", sent.path)
            val json = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
            assertEquals("event-1", json["eventId"]?.jsonPrimitive?.content)
            val roles = json["history"]?.jsonArray?.map { it.jsonObject["role"]?.jsonPrimitive?.content }
            assertEquals(listOf("user", "assistant"), roles)
        }
}
