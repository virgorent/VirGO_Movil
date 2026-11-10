package mx.virgo.core.network

import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class OkHttpClientConfigurationTest {
    @Test
    fun debugClientLogsWithBasicLevel() {
        val client = createOkHttpClient(FakeAuthTokenProvider(null), isDebug = true)

        val logging = client.interceptors.filterIsInstance<HttpLoggingInterceptor>().single()
        assertEquals(HttpLoggingInterceptor.Level.BASIC, logging.level)
    }

    @Test
    fun releaseClientDoesNotLog() {
        val client = createOkHttpClient(FakeAuthTokenProvider(null), isDebug = false)

        assertFalse(client.interceptors.any { it is HttpLoggingInterceptor })
    }

    @Test
    fun clientKeepsTenAndTwentySecondTimeouts() {
        val client = createOkHttpClient(FakeAuthTokenProvider(null), isDebug = false)

        assertEquals(10_000, client.connectTimeoutMillis)
        assertEquals(20_000, client.readTimeoutMillis)
    }

    @Test
    fun clientAlwaysAddsCorrelationAndAuthInterceptors() {
        val client = createOkHttpClient(FakeAuthTokenProvider(null), isDebug = false)

        assertEquals(2, client.interceptors.size)
    }
}
