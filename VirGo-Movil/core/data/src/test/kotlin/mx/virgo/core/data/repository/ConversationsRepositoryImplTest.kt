package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.network.createConversationsApi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Contrato §3 (backend `084`–`088`). */
class ConversationsRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: ConversationsRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = ConversationsRepositoryImpl(createConversationsApi(server.url("/").toString(), OkHttpClient()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun listIsNewestFirstAndMessagesOldestFirst() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """{"data":[{"id":"a","target":{"id":"v1","name":"Uno"},"lastAt":"2026-11-20T10:00:00Z"},""" +
                        """{"id":"b","target":{"id":"v2","name":"Dos"},"lastAt":"2026-11-27T10:00:00Z","unread":2}]}""",
                ),
            )
            server.enqueue(
                MockResponse().setBody(
                    """{"data":[{"id":"2","text":"Sí","sentAt":"2026-11-27T10:05:00Z"},""" +
                        """{"id":"1","text":"Hola","sentAt":"2026-11-27T10:00:00Z","mine":true}]}""",
                ),
            )
            assertEquals(listOf("b", "a"), (repository.list() as AppResult.Success).value.map { it.id })
            val messages = (repository.messages("b") as AppResult.Success).value
            assertEquals(listOf("Hola", "Sí"), messages.map { it.text })
            assertTrue(messages.first().mine)
            server.takeRequest()
            assertEquals("/api/v1/conversations/b/messages", server.takeRequest().path)
        }

    @Test
    fun startSendsTheVenueTargetAndText() =
        runTest {
            server.enqueue(
                MockResponse().setResponseCode(201).setBody("""{"id":"c","target":{"id":"v1","name":"Uno"}}"""),
            )
            repository.start("v1", "¿Tienen fecha?")
            val body = Json.parseToJsonElement(server.takeRequest().body.readUtf8()).jsonObject
            assertEquals(
                "venue",
                body["target"]
                    ?.jsonObject
                    ?.get("type")
                    ?.jsonPrimitive
                    ?.content,
            )
            assertEquals(
                "v1",
                body["target"]
                    ?.jsonObject
                    ?.get("id")
                    ?.jsonPrimitive
                    ?.content,
            )
            assertEquals("¿Tienen fecha?", body["text"]?.jsonPrimitive?.content)
        }

    @Test
    fun missingEndpointIsNotFound() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(501))
            assertEquals(AppResult.Failure(AppError.NotFound), repository.list())
        }
}
