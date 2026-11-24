package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.network.createNoticesApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoticesRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: NoticesRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = NoticesRepositoryImpl(createNoticesApi(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun noticesResponseMapsReadStateFromReadAt() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody(NOTICES_BODY))

            val result = repository.getNotices()

            val notices = (result as AppResult.Success).value
            assertEquals(2, notices.size)
            val unread = notices.first { it.id == "n-1" }
            assertEquals("booking", unread.type)
            assertEquals("Reserva confirmada", unread.title)
            assertEquals("Casa VirGo te espera", unread.body)
            assertFalse(unread.isRead)
            assertEquals("2026-11-18T10:00:00Z", unread.createdAt)
            assertTrue(notices.first { it.id == "n-2" }.isRead)
            assertEquals("/api/v1/notices", server.takeRequest().path)
        }

    @Test
    fun emptyNoticesReturnsEmptyList() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":[]}"""))

            val result = repository.getNotices()

            assertEquals(emptyList<Any>(), (result as AppResult.Success).value)
        }

    @Test
    fun notFoundResponseMapsToNotFound() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404).setBody("""{"title":"Not Found","status":404}"""))

            val result = repository.getNotices()

            assertEquals(AppError.NotFound, (result as AppResult.Failure).error)
        }

    @Test
    fun serverErrorResponseMapsToServerError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(500).setBody("""{"title":"Error","status":500}"""))

            val result = repository.getNotices()

            assertTrue((result as AppResult.Failure).error is AppError.Server)
        }

    @Test
    fun noConnectionMapsToNetworkError() =
        runTest {
            server.shutdown()

            val result = repository.getNotices()

            assertEquals(AppError.Network, (result as AppResult.Failure).error)
        }
}

private val NOTICES_BODY =
    """
    {"data":[
      {"id":"n-1","type":"booking","title":"Reserva confirmada","body":"Casa VirGo te espera",
       "createdAt":"2026-11-18T10:00:00Z"},
      {"id":"n-2","type":"payment","title":"Pago recibido","readAt":"2026-11-17T09:00:00Z"}
    ]}
    """.trimIndent()
