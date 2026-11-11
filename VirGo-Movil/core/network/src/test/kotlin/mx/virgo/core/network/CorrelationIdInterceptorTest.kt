package mx.virgo.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

class CorrelationIdInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = createOkHttpClient(FakeAuthTokenProvider(null), isDebug = false)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun everyRequestCarriesAUniqueCorrelationId() {
        callServer()
        callServer()

        val first = requireNotNull(server.takeRequest().getHeader("X-Correlation-Id"))
        val second = requireNotNull(server.takeRequest().getHeader("X-Correlation-Id"))

        assertNotNull(UUID.fromString(first))
        assertNotNull(UUID.fromString(second))
        assertNotEquals(first, second)
    }

    @Test
    fun correlationIdReplacesAValueSentByTheCaller() {
        server.enqueue(MockResponse().setResponseCode(200))
        val request =
            Request
                .Builder()
                .url(server.url("/health"))
                .header("X-Correlation-Id", "caller-value")
                .build()

        client.newCall(request).execute().close()

        val sent = requireNotNull(server.takeRequest().getHeader("X-Correlation-Id"))
        assertNotEquals("caller-value", sent)
        assertNotNull(UUID.fromString(sent))
    }

    private fun callServer() {
        server.enqueue(MockResponse().setResponseCode(200))
        val request = Request.Builder().url(server.url("/health")).build()
        client.newCall(request).execute().close()
    }
}
