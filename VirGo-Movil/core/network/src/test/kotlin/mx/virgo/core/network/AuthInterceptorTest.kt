package mx.virgo.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun requestCarriesBearerTokenWhenThereIsOne() {
        val client = clientWithToken("token-123")

        callServer(client)

        assertEquals("Bearer token-123", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun requestOmitsAuthorizationWhenThereIsNoToken() {
        val client = clientWithToken(null)

        callServer(client)

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun requestOmitsAuthorizationWhenTheTokenIsBlank() {
        val client = clientWithToken("   ")

        callServer(client)

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    private fun clientWithToken(value: String?): OkHttpClient =
        createOkHttpClient(FakeAuthTokenProvider(value), isDebug = false)

    private fun callServer(client: OkHttpClient) {
        server.enqueue(MockResponse().setResponseCode(200))
        val request = Request.Builder().url(server.url("/health")).build()
        client.newCall(request).execute().close()
    }
}
