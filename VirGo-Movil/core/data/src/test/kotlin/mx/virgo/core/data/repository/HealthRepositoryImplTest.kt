package mx.virgo.core.data.repository

import kotlinx.coroutines.test.runTest
import mx.virgo.core.common.AppError
import mx.virgo.core.common.AppResult
import mx.virgo.core.network.createHealthApi
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HealthRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: HealthRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = HealthRepositoryImpl(createHealthApi(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun healthyResponseReturnsStatusAndLatency() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"Healthy"}"""))

            val result = repository.checkHealth()

            val health = (result as AppResult.Success).value
            assertEquals("Healthy", health.status)
            assertTrue(health.latencyMs >= 0)
            assertEquals("/health", server.takeRequest().path)
        }

    @Test
    fun unhealthyResponseMapsToServerError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(503).setBody("""{"status":"Unhealthy"}"""))

            val result = repository.checkHealth()

            assertTrue((result as AppResult.Failure).error is AppError.Server)
        }

    @Test
    fun noConnectionMapsToNetworkError() =
        runTest {
            server.shutdown()

            val result = repository.checkHealth()

            assertEquals(AppError.Network, (result as AppResult.Failure).error)
        }

    @Test
    fun malformedResponseMapsToUnknownError() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(200).setBody("not-json"))

            val result = repository.checkHealth()

            assertEquals(AppError.Unknown, (result as AppResult.Failure).error)
        }
}
